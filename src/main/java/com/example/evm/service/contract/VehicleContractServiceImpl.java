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
import com.example.evm.repository.order.OrderRepository;
import com.example.evm.repository.salePrice.SalePriceRepository;
import com.example.evm.service.storage.FileStorageService;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleContractServiceImpl implements VehicleContractService {

    private final VehicleContractRepository vehicleContractRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final SalePriceRepository salePriceRepository;
    private final FileStorageService fileStorageService;

    /**
     * 🧾 Tạo hợp đồng mới (và tự động tạo file Word)
     */
    @Override
    @Transactional
    public VehicleContractResponse createContract(VehicleContractRequest request) {
        // 1️⃣ Kiểm tra OrderDetail

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

        // 2️⃣ Lấy giá bán từ SalePrice
        BigDecimal salePrice = salePriceRepository.findLatestPriceByDealerAndVariant(dealerId, vehicle.getVariant().getVariantId())
                .map(sp -> sp.getPrice())
                .orElse(BigDecimal.ZERO);

        // 3️⃣ Tạo hợp đồng
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
        contract.setStatus("DRAFT"); // ✅ trạng thái mặc định khi tạo

        VehicleContract saved = vehicleContractRepository.save(contract);
        log.info("✅ Created contract {} for order {}", saved.getContractNumber(), order.getOrderId());

        // 4️⃣ Sinh file Word hợp đồng
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
        log.info("✅ Contract {} has been SIGNED.", signedContract.getContractNumber());

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
                .orElseThrow(() -> new RuntimeException("❌ Contract not found with ID: " + id));
    }


    /**
     * 🔍 Lấy hợp đồng theo ID
     */
    @Override
    public VehicleContractResponse getContractById(Long id) {
        VehicleContract contract = vehicleContractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found with ID: " + id));
        return mapToResponse(contract);
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

        log.warn("🔥 Deleting DRAFT contract ID: {}, Number: {}", id, contract.getContractNumber());

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

            // ===== Tiêu đề =====
            XWPFParagraph title = doc.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("HỢP ĐỒNG MUA BÁN XE Ô TÔ");
            titleRun.setBold(true);
            titleRun.setFontSize(16);

            // ===== Dòng trống =====
            doc.createParagraph();

            // ===== Thông tin cơ bản =====
            addParagraph(doc, "Số hợp đồng: " + contract.getContractNumber());
            addParagraph(doc, "Ngày lập hợp đồng: " +
                    contract.getContractDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            doc.createParagraph();

            // ===== Các bên =====
            addParagraph(doc, "BÊN BÁN (Đại lý): " +
            (contract.getDealer() != null ? contract.getDealer().getDealerName() : "N/A"));
            addParagraph(doc, "BÊN MUA (Khách hàng): " + 
            (contract.getCustomer() != null ? contract.getCustomer().getCustomerName() : "N/A"));
            doc.createParagraph();

            // ===== Thông tin xe =====
            addParagraph(doc, "Thông tin xe:");
            if (contract.getVehicle() != null) {
                Vehicle v = contract.getVehicle();
                addParagraph(doc, "- Số VIN: " + (v.getVinNumber() != null ? v.getVinNumber() : "N/A"));
                addParagraph(doc, "- Màu sắc: " + (v.getColor() != null ? v.getColor() : "N/A"));
                
                if (v.getVariant() != null) {
                    addParagraph(doc, "- Phiên bản: " + (v.getVariant().getName() != null ? v.getVariant().getName() : "N/A"));
                    if (v.getVariant().getModel() != null) {
                        addParagraph(doc, "- Mẫu xe: " + (v.getVariant().getModel().getName() != null ? v.getVariant().getModel().getName() : "N/A"));
                    } else {
                        addParagraph(doc, "- Mẫu xe: N/A");
                    }
                } else {
                    addParagraph(doc, "- Phiên bản: N/A");
                    addParagraph(doc, "- Mẫu xe: N/A");
                }
            } else {
                addParagraph(doc, "N/A - Không có thông tin xe");
            }
            doc.createParagraph();

            // ===== Giá & thanh toán =====
            addParagraph(doc, "Giá bán: " + 
                (contract.getSalePrice() != null ? contract.getSalePrice().toString() + " VND" : "N/A"));
            addParagraph(doc, "Phương thức thanh toán: " + 
                (contract.getPaymentMethod() != null ? contract.getPaymentMethod() : "N/A"));
            doc.createParagraph();

            // ===== Ghi chú & Ký tên =====
            if (contract.getNotes() != null && !contract.getNotes().isEmpty()) {
                addParagraph(doc, "Ghi chú: " + contract.getNotes());
                doc.createParagraph();
             }
             addParagraph(doc, "ĐẠI DIỆN BÊN BÁN: ______________________");
             addParagraph(doc, "ĐẠI DIỆN BÊN MUA: ______________________");

            // ✅ Ghi file ra đĩa bằng Files.newOutputStream
            try (OutputStream out = Files.newOutputStream(filePath,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                doc.write(out);
            }
        }

        log.info("📝 Contract file generated successfully: {}", filePath.toAbsolutePath());
        return "/api/contracts/files/" + filename;

    } catch (Exception e) {
        log.error("❌ Error generating contract file: {}", e.getMessage(), e);
        throw new RuntimeException("Error generating contract file", e);
    }
}

    /**
     * Hàm helper để thêm đoạn văn bản (paragraph) an toàn
     */
    private void addParagraph(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun run = p.createRun();
        run.setFontSize(12);
        run.setText(text);
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
