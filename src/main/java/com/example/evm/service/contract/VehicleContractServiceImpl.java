package com.example.evm.service.contract;

import com.example.evm.dto.contract.VehicleContractRequest;
import com.example.evm.dto.contract.VehicleContractResponse;
import com.example.evm.entity.contract.VehicleContract;
import com.example.evm.entity.order.Order;
import com.example.evm.entity.order.OrderDetail;
import com.example.evm.entity.vehicle.Vehicle;
import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.repository.contract.VehicleContractRepository;
import com.example.evm.repository.order.OrderDetailRepository;
import com.example.evm.repository.salePrice.SalePriceRepository;
import com.example.evm.service.storage.FileStorageService;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleContractServiceImpl implements VehicleContractService {

    private final VehicleContractRepository vehicleContractRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final SalePriceRepository salePriceRepository;
    private final FileStorageService fileStorageService;

    /**
     * 🧾 Tạo hợp đồng mới (và tự động tạo file Word)
     */
    @Override
    @Transactional
    public VehicleContractResponse createContract(VehicleContractRequest request) {
        //  Kiểm tra OrderDetail

        OrderDetail orderDetail = orderDetailRepository.findById(request.getOrderDetailId())
                .orElseThrow(() -> new ResourceNotFoundException("OrderDetail not found with ID: " + request.getOrderDetailId()));

        if (vehicleContractRepository.existsByOrderDetail_OrderDetailId(request.getOrderDetailId())) {
            throw new IllegalStateException("A contract already exists for this OrderDetail.");
        }

        Order order = orderDetail.getOrder(); 
        if (order == null) {
            throw new ResourceNotFoundException("Order not found linked to OrderDetail ID: " + request.getOrderDetailId());
        }

        Vehicle vehicle = orderDetail.getVehicle();
        if (vehicle == null)
            throw new ResourceNotFoundException("No vehicle linked to OrderDetail ID: " + request.getOrderDetailId());

        // 🚫 Kiểm tra nếu xe là xe lái thử
        if ("TEST_DRIVE".equalsIgnoreCase(vehicle.getStatus())) {
            throw new IllegalStateException("🚫 Xe lái thử không thể được bán cho khách hàng.");
        }    

        Long dealerId = order.getDealer().getDealerId();

        //  Lấy giá bán từ SalePrice
        BigDecimal salePrice = salePriceRepository.findLatestPriceByDealerAndVariant(dealerId, vehicle.getVariant().getVariantId())
                .map(sp -> sp.getPrice())
                .orElse(BigDecimal.ZERO);

        //  Tạo hợp đồng
        VehicleContract contract = new VehicleContract();
        contract.setContractNumber("HD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        contract.setOrder(order);
        contract.setOrderDetail(orderDetail);
        contract.setDealer(order.getDealer());
        contract.setCustomer(order.getCustomer());
        contract.setVehicle(vehicle);
        contract.setSalePrice(salePrice);
        contract.setPaymentMethod(order.getPaymentMethod());
        contract.setNotes(request.getNotes());
        contract.setStatus("DRAFT"); //  trạng thái mặc định khi tạo

        VehicleContract saved = vehicleContractRepository.save(contract);
        log.info(" Created contract {} for order {}", saved.getContractNumber(), order.getOrderId());

        //  Sinh file Word hợp đồng
        String fileUrl = generateContractWord(saved);
        saved.setFileUrl(fileUrl);
        vehicleContractRepository.save(saved);

        return mapToResponse(saved);
    }

    /**
     * 🧾 Ký hợp đồng (DRAFT -> SIGNED)
     */
    @Override
    @Transactional
    public VehicleContractResponse signContract(Long id) {
        VehicleContract contract = vehicleContractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with ID: " + id));

        // Chỉ cho phép ký hợp đồng đang ở trạng thái DRAFT
        if (!"DRAFT".equalsIgnoreCase(contract.getStatus())) {
            throw new IllegalStateException("Chỉ có thể ký hợp đồng đang ở trạng thái 'DRAFT'. Trạng thái hiện tại: " + contract.getStatus());
        }

        contract.setStatus("SIGNED"); // Cập nhật trạng thái
        VehicleContract signedContract = vehicleContractRepository.save(contract);
        log.info(" Contract {} has been SIGNED.", signedContract.getContractNumber());

        return mapToResponse(signedContract);
    }

    /**
     * 🧾 Lấy tất cả hợp đồng
     */
    @Override
    public List<VehicleContractResponse> getAllContracts() {
        return vehicleContractRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /** 
     * 
    */
    public VehicleContract getContractEntityById(Long id) {
        return vehicleContractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(" Contract not found with ID: " + id));
    }


    /**
     *  Lấy hợp đồng theo ID
     */
    @Override
    public VehicleContractResponse getContractById(Long id) {
        VehicleContract contract = vehicleContractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with ID: " + id));
        return mapToResponse(contract);
    }

    /**
     * Lấy hơp đồng theo Dealer ID
     */
    @Override
    public List<VehicleContractResponse> getContractsByDealerId(Long dealerId) {
        List<VehicleContract> contracts = vehicleContractRepository.findByDealerDealerId(dealerId); 
        return contracts.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * 🧾 Xóa hợp đồng nháp (Chỉ xóa DRAFT)
     */
    @Override
    @Transactional
    public void deleteDraftContract(Long id) {
        VehicleContract contract = vehicleContractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with ID: " + id));

        // --- VALIDATION QUAN TRỌNG ---
        if (!"DRAFT".equalsIgnoreCase(contract.getStatus())) {
            throw new DataIntegrityViolationException(
                "Không thể xóa hợp đồng đã ký (SIGNED) hoặc đã xử lý. Chỉ có thể xóa hợp đồng 'DRAFT'."
            );
        }
        // --- HẾT VALIDATION ---

        log.warn(" Deleting DRAFT contract ID: {}, Number: {}", id, contract.getContractNumber());

        // 1. Xóa file Word liên quan
        if (contract.getFileUrl() != null && !contract.getFileUrl().isBlank()) {
            try {
                // Trích xuất tên file từ URL (ví dụ: "/api/contracts/files/Contract_2.docx")
                String filename = contract.getFileUrl().substring(contract.getFileUrl().lastIndexOf('/') + 1);
                String relativePath = "contracts/" + filename; // Đường dẫn tương đối
                
                fileStorageService.delete(relativePath); // Gọi hàm delete 1 tham số
                log.info("   - Deleted associated file: {}", relativePath);
            } catch (Exception e) {
                log.error("   - Failed to delete file for contract ID {}: {}. Continuing with DB deletion.", id, e.getMessage());
                // (Có thể chọn ném lỗi ở đây nếu bắt buộc phải xóa được file)
            }
        }
        
        // 2. Xóa bản ghi hợp đồng
        vehicleContractRepository.delete(contract);
        log.info("   - Deleted contract record from DB.");
    }

    /**
     * 📄 Helper — Sinh file Word hợp đồng
     */
    private String generateContractWord(VehicleContract contract) {
    try {
        // Đảm bảo ID có sẵn
        Long id = contract.getContractId() != null ? contract.getContractId() : 0L;
        String filename = "Contract_" + id + ".docx";

        Path dir = Paths.get("uploads/contracts/");
        Files.createDirectories(dir);
        Path filePath = dir.resolve(filename);

        // Tạo tài liệu mới
        try (XWPFDocument doc = new XWPFDocument()) {

            // ===== Quốc hiệu, Tiêu ngữ =====
            addStyledParagraph(doc, "CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM", 12, true, ParagraphAlignment.CENTER);
            addStyledParagraph(doc, "Độc lập - Tự do - Hạnh phúc", 12, true, ParagraphAlignment.CENTER);
            addStyledParagraph(doc, "________________________", 12, false, ParagraphAlignment.CENTER);
            doc.createParagraph(); // Dòng trống

            // ===== Tiêu đề =====
            addStyledParagraph(doc, "HỢP ĐỒNG MUA BÁN Ô TÔ", 16, true, ParagraphAlignment.CENTER);
            addStyledParagraph(doc, "Số: " + getSafeString(contract.getContractNumber(), "........."), 12, false, ParagraphAlignment.CENTER);
            
            // Dòng "Hợp đồng... ký ngày..." đã được BỎ theo template Python mới
            doc.createParagraph(); // Dòng trống

            // ===== Bố cục 2 cột cho Bên Bán và Bên Mua (Dùng bảng ẩn) =====
            XWPFTable partyTable = doc.createTable(1, 2);
            setTableBorders(partyTable, STBorder.NONE); // Ẩn viền bảng
            setTableColumnWidth(partyTable, 0, 4500); // ~3.1 inch
            setTableColumnWidth(partyTable, 1, 4500);

            XWPFTableCell sellerCell = partyTable.getRow(0).getCell(0);
            XWPFTableCell buyerCell = partyTable.getRow(0).getCell(1);
            
            // Xóa đoạn văn mặc định
            sellerCell.removeParagraph(0);
            buyerCell.removeParagraph(0);

            // --- Cột Bên Bán ---
            addCellParagraph(sellerCell, "BÊN BÁN (ĐẠI LÝ)", 12, true, ParagraphAlignment.LEFT, false);
            if (contract.getDealer() != null) {
                addCellParagraph(sellerCell, "Tên đơn vị: " + getSafeString(contract.getDealer().getDealerName()), 12, false, ParagraphAlignment.LEFT, false);
                addCellParagraph(sellerCell, "Địa chỉ: " + getSafeString(contract.getDealer().getAddress()), 12, false, ParagraphAlignment.LEFT, false);
                addCellParagraph(sellerCell, "Hotline: " + getSafeString(contract.getDealer().getPhone()), 12, false, ParagraphAlignment.LEFT, false);
            }

            // --- Cột Bên Mua ---
            addCellParagraph(buyerCell, "BÊN MUA (KHÁCH HÀNG)", 12, true, ParagraphAlignment.LEFT, false);
            if (contract.getCustomer() != null) {
                addCellParagraph(buyerCell, "Họ và tên: " + getSafeString(contract.getCustomer().getCustomerName()), 12, false, ParagraphAlignment.LEFT, false);
                addCellParagraph(buyerCell, "Số điện thoại: " + getSafeString(contract.getCustomer().getPhone()), 12, false, ParagraphAlignment.LEFT, false);
                addCellParagraph(buyerCell, "Email: " + getSafeString(contract.getCustomer().getEmail()), 12, false, ParagraphAlignment.LEFT, false);
            }
            
            doc.createParagraph(); // Dòng trống

            // ===== ĐIỀU 1. THÔNG TIN XE =====
            addStyledParagraph(doc, "ĐIỀU 1. TÊN HÀNG – PHIÊN BẢN – MÀU XE – GIÁ TRỊ XE", 12, true, ParagraphAlignment.LEFT);
            doc.createParagraph();

            // --- Bảng thông tin xe (4 cột) ---
            XWPFTable itemTable = doc.createTable(2, 4); // 1 hàng tiêu đề, 1 hàng dữ liệu
            itemTable.setWidth("100%");

            // Hàng tiêu đề
            setTableCellText(itemTable.getRow(0).getCell(0), "Tên hàng", true);
            setTableCellText(itemTable.getRow(0).getCell(1), "Phiên bản", true);
            setTableCellText(itemTable.getRow(0).getCell(2), "Màu xe", true);
            setTableCellText(itemTable.getRow(0).getCell(3), "Giá bán (VND)", true);

            // Hàng dữ liệu
            Vehicle v = contract.getVehicle();
            String modelName = (v != null && v.getVariant() != null && v.getVariant().getModel() != null) ? v.getVariant().getModel().getName() : "N/A";
            String variantName = (v != null && v.getVariant() != null) ? v.getVariant().getName() : "N/A";
            String color = (v != null) ? v.getColor() : "N/A";
            String price = (contract.getSalePrice() != null) ? contract.getSalePrice().toString() : "N/A";
            
            setTableCellText(itemTable.getRow(1).getCell(0), getSafeString(modelName), false);
            setTableCellText(itemTable.getRow(1).getCell(1), getSafeString(variantName), false);
            setTableCellText(itemTable.getRow(1).getCell(2), getSafeString(color), false);
            setTableCellText(itemTable.getRow(1).getCell(3), getSafeString(price), false);

            doc.createParagraph(); // Dòng trống

            // ===== ĐIỀU 2. PHƯƠNG THỨC THANH TOÁN =====
            addStyledParagraph(doc, "ĐIỀU 2. PHƯƠNG THỨC THANH TOÁN", 12, true, ParagraphAlignment.LEFT);
            addStyledParagraph(doc, getSafeString(contract.getPaymentMethod(), "Chưa cập nhật"), 12, false, ParagraphAlignment.LEFT);
            doc.createParagraph();

            // ===== ĐIỀU 3. CAM KẾT CHUNG =====
            addStyledParagraph(doc, "ĐIỀU 3. CAM KẾT CHUNG", 12, true, ParagraphAlignment.LEFT);
            addStyledParagraph(doc, "Hai bên cam kết thực hiện đúng các điều khoản của Hợp đồng.", 12, false, ParagraphAlignment.LEFT);
            doc.createParagraph();

            // ===== Bố cục 2 cột cho Ký tên (Dùng bảng ẩn) =====
            XWPFTable sigTable = doc.createTable(1, 2);
            setTableBorders(sigTable, STBorder.NONE);
            setTableColumnWidth(sigTable, 0, 4500);
            setTableColumnWidth(sigTable, 1, 4500);

            XWPFTableCell sellerSigCell = sigTable.getRow(0).getCell(0);
            XWPFTableCell buyerSigCell = sigTable.getRow(0).getCell(1);
            sellerSigCell.removeParagraph(0);
            buyerSigCell.removeParagraph(0);

            // --- Cột ký tên Bên Bán ---
            addCellParagraph(sellerSigCell, "ĐẠI DIỆN BÊN BÁN", 12, true, ParagraphAlignment.CENTER, false);
            addCellParagraph(sellerSigCell, "(ký, ghi rõ họ tên, đóng dấu)", 12, false, ParagraphAlignment.CENTER, true);
            addCellParagraph(sellerSigCell, "\n\n\n\n", 12, false, ParagraphAlignment.CENTER, false); // Khoảng trống
            
            // --- Cột ký tên Bên Mua ---
            addCellParagraph(buyerSigCell, "ĐẠI DIỆN BÊN MUA", 12, true, ParagraphAlignment.CENTER, false);
            addCellParagraph(buyerSigCell, "(ký, ghi rõ họ tên)", 12, false, ParagraphAlignment.CENTER, true);
            addCellParagraph(buyerSigCell, "\n\n\n\n", 12, false, ParagraphAlignment.CENTER, false); // Khoảng trống

            doc.createParagraph();
            addStyledParagraph(doc, "Ngày tạo hợp đồng: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 12, false, ParagraphAlignment.LEFT);

            // ===== Ghi file =====
            try (OutputStream out = Files.newOutputStream(filePath,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                doc.write(out);
            }
        }

        log.info(" Contract file generated successfully: {}", filePath.toAbsolutePath());
        return "/api/contracts/files/" + filename;

    } catch (Exception e) {
        log.error(" Error generating contract file: {}", e.getMessage(), e);
        throw new RuntimeException("Error generating contract file", e);
    }
}

    /**
     * Hàm helper để thêm đoạn văn bản (paragraph) an toàn
     */
    private void addStyledParagraph(XWPFDocument doc, String text, int fontSize, boolean bold, ParagraphAlignment align) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(align);
        XWPFRun run = p.createRun();
        run.setFontSize(fontSize);
        run.setBold(bold);
        run.setText(text);
    }

    /**
     * Hàm helper để thêm paragraph vào một ô (Cell) của bảng
     */
    private void addCellParagraph(XWPFTableCell cell, String text, int fontSize, boolean bold, ParagraphAlignment align, boolean italic) {
        XWPFParagraph p = cell.addParagraph();
        p.setAlignment(align);
        XWPFRun run = p.createRun();
        run.setFontSize(fontSize);
        run.setBold(bold);
        run.setItalic(italic);
        run.setText(text);
    }

    /**
     * Hàm helper để set text cho ô (Cell) của bảng (ghi đè paragraph đầu tiên)
     */
    private void setTableCellText(XWPFTableCell cell, String text, boolean bold) {
        XWPFParagraph p = cell.getParagraphArray(0) != null ? cell.getParagraphArray(0) : cell.addParagraph();
        p.setAlignment(ParagraphAlignment.LEFT);
        
        // Xóa các run cũ nếu có
        while(p.getRuns().size() > 0) {
            p.removeRun(0);
        }
        
        XWPFRun run = p.createRun();
        run.setFontSize(12);
        run.setBold(bold);
        run.setText(text);
    }

    /**
     * Hàm helper để ẩn viền bảng
     */
    private void setTableBorders(XWPFTable table, STBorder.Enum borderType) {
        try {
            CTTblBorders borders = table.getCTTbl().getTblPr().addNewTblBorders();
            borders.addNewTop().setVal(borderType);
            borders.addNewBottom().setVal(borderType);
            borders.addNewLeft().setVal(borderType);
            borders.addNewRight().setVal(borderType);
            borders.addNewInsideH().setVal(borderType);
            borders.addNewInsideV().setVal(borderType);
        } catch (Exception e) {
            // Handle exception
        }
    }

    /**
     * Hàm helper để set độ rộng cột (tính bằng twips, 1 inch = 1440)
     */
    private void setTableColumnWidth(XWPFTable table, int colIndex, int width) {
        BigInteger w = BigInteger.valueOf(width);
        table.getRow(0).getCell(colIndex).getCTTc().addNewTcPr().addNewTcW().setW(w);
    }

    /**
     * Hàm helper để lấy chuỗi an toàn (tránh "null" trong file word)
     */
    private String getSafeString(String str, String defaultVal) {
        return (str != null && !str.isEmpty()) ? str : defaultVal;
    }

    private String getSafeString(String str) {
        return getSafeString(str, "N/A");
    }

    /**
     * 🔄 Map Entity → DTO Response
     */
    private VehicleContractResponse mapToResponse(VehicleContract c) {
        return VehicleContractResponse.builder()
                .contractId(c.getContractId())
                .contractNumber(c.getContractNumber())
                .orderId(c.getOrder().getOrderId())
                .orderDetailId(c.getOrderDetail().getOrderDetailId())
                .dealerId(c.getDealer().getDealerId())
                .dealerName(c.getDealer().getDealerName())
                .customerId(c.getCustomer().getCustomerId())
                .customerName(c.getCustomer().getCustomerName())
                .vehicleId(c.getVehicle().getVehicleId())
                .vinNumber(c.getVehicle().getVinNumber())
                .color(c.getVehicle().getColor())
                .variantName(c.getVehicle().getVariant().getName())
                .modelName(c.getVehicle().getVariant().getModel().getName())
                .salePrice(c.getSalePrice())
                .paymentMethod(c.getPaymentMethod())
                .contractDate(c.getContractDate())
                .status(c.getStatus())
                .notes(c.getNotes())
                .fileUrl(c.getFileUrl())
                .build();
    }
}
