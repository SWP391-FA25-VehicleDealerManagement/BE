package com.example.evm.service.inventory;

import com.example.evm.dto.inventory.AllocationResponse;
import com.example.evm.dto.inventory.InventoryResponse;
import com.example.evm.dto.inventory.ManufacturerStockResponse;
import com.example.evm.dto.inventory.AllocationRequest; 
import com.example.evm.dto.inventory.StockRequest; 
import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.dealer.DealerRequest;
import com.example.evm.entity.inventory.InventoryStock;
import com.example.evm.entity.inventory.ManufacturerStock; 
import com.example.evm.entity.vehicle.VehicleVariant;
import com.example.evm.exception.ResourceNotFoundException; 
import com.example.evm.repository.dealer.DealerRepository;
import com.example.evm.repository.dealer.DealerRequestRepository;
import com.example.evm.repository.inventory.InventoryStockRepository;
import com.example.evm.repository.inventory.ManufacturerStockRepository; 
import com.example.evm.repository.vehicle.VehicleVariantRepository; 
import com.example.evm.dto.inventory.UpdateStockRequest;
import com.example.evm.dto.inventory.UpdateManufacturerStockRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryStockRepository inventoryRepository;
    private final ManufacturerStockRepository manufacturerStockRepo;
    private final VehicleVariantRepository variantRepository;
    private final DealerRepository dealerRepository;
    private final DealerRequestRepository dealerRequestRepository;

    // --- 1. KHO ĐẠI LÝ ---

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAllDealerStock() {
        return inventoryRepository.findAllWithRelations()
                .stream()
                .map(this::mapToDealerResponse) 
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getDealerStockByDealerId(Long dealerId) {
        return inventoryRepository.findByDealerIdWithRelations(dealerId)
                .stream()
                .map(this::mapToDealerResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InventoryResponse createDealerStock(StockRequest request) {
        VehicleVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found"));
        Dealer dealer = dealerRepository.findById(request.getDealerId())
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));

        InventoryStock stock = inventoryRepository
                .findByVariantVariantIdAndColorAndDealerDealerId(
                        request.getVariantId(),
                        request.getColor(),
                        request.getDealerId()
                )
                .map(existingStock -> {
                    existingStock.setQuantity(request.getQuantity()); 
                    
                    existingStock.setListingPrice(request.getListingPrice());
                    existingStock.setStatus(request.getStatus());
                    return existingStock;
                })
                .orElseGet(() -> {
                    InventoryStock newStock = new InventoryStock();
                    newStock.setVariant(variant);
                    newStock.setDealer(dealer);
                    newStock.setColor(request.getColor());
                    newStock.setQuantity(request.getQuantity());
                    newStock.setListingPrice(request.getListingPrice());
                    newStock.setStatus(request.getStatus());
                    return newStock;
                });

        InventoryStock savedStock = inventoryRepository.save(stock);
        return mapToDealerResponse(savedStock);
    }

    @Override
    @Transactional
    public InventoryResponse updateDealerStock(Long id, UpdateStockRequest request) {
        // 1. Tìm bản ghi theo ID
        InventoryStock existingStock = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer stock not found with id: " + id));

        // 2. Cập nhật các trường từ DTO mới (Partial Update)
        existingStock.setQuantity(request.getQuantity());
        if (request.getListingPrice() != null) {
            existingStock.setListingPrice(request.getListingPrice());
        }
        if (request.getStatus() != null) {
            existingStock.setStatus(request.getStatus());
        }

        InventoryStock savedStock = inventoryRepository.save(existingStock);
        return mapToDealerResponse(savedStock);
    }
    
    @Override
    public InventoryResponse updateStockStatus(Long id, String status) {
        InventoryStock existing = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dealer stock not found"));
        existing.setStatus(status);
        return mapToDealerResponse(inventoryRepository.save(existing));
    }

    @Override
    public void deleteStock(Long id) {
        inventoryRepository.deleteById(id);
    }

    // --- 2. KHO TỔNG ---

    @Override
    @Transactional(readOnly = true)
    public List<ManufacturerStockResponse> getAllManufacturerStock() {
        return manufacturerStockRepo.findAllWithRelations()
                .stream()
                .map(this::mapToManufacturerResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ManufacturerStockResponse createManufacturerStock(StockRequest request) {
        VehicleVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found"));

        ManufacturerStock stock = manufacturerStockRepo
                .findByVariantVariantIdAndColor(request.getVariantId(), request.getColor())
                .map(existingStock -> {
                    existingStock.setQuantity(request.getQuantity()); 
                    
                    existingStock.setStatus(request.getStatus());
                    return existingStock;
                })
                .orElseGet(() -> {
                    ManufacturerStock newStock = new ManufacturerStock();
                    newStock.setVariant(variant);
                    newStock.setColor(request.getColor());
                    newStock.setQuantity(request.getQuantity());
                    newStock.setStatus(request.getStatus()); 
                    return newStock;
                });
        
        ManufacturerStock savedStock = manufacturerStockRepo.save(stock);
        return mapToManufacturerResponse(savedStock); 
    }

    @Override
    @Transactional
    public ManufacturerStockResponse updateManufacturerStock(Long id, UpdateManufacturerStockRequest request) {
        // 1. Tìm theo ID
        ManufacturerStock existingStock = manufacturerStockRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manufacturer stock not found with id: " + id));

        // 2. Cập nhật (Partial Update)
        existingStock.setQuantity(request.getQuantity());
        if (request.getStatus() != null) {
            existingStock.setStatus(request.getStatus());
        }

        ManufacturerStock savedStock = manufacturerStockRepo.save(existingStock);
        return mapToManufacturerResponse(savedStock);
    }

    @Override
    @Transactional
    public ManufacturerStockResponse updateManufacturerStockStatus(Long id, String status) {
        ManufacturerStock existing = manufacturerStockRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Manufacturer stock item not found"));
        existing.setStatus(status);
        ManufacturerStock savedStock = manufacturerStockRepo.save(existing);
        return mapToManufacturerResponse(savedStock);
    }

    // --- 3. ĐIỀU PHỐI ---

    @Override
    @Transactional
    public AllocationResponse allocateStockToDealer(AllocationRequest request) {
        
        Dealer dealer = dealerRepository.findById(request.getDealerId())
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));
        VehicleVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found"));

        // 1. TRỪ KHO TỔNG
        ManufacturerStock centralStock = manufacturerStockRepo
                .findByVariantVariantIdAndColor(request.getVariantId(), request.getColor())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in central stock"));

        if (centralStock.getQuantity() < request.getQuantity()) {
            throw new RuntimeException("Not enough stock in central warehouse");
        }
        centralStock.setQuantity(centralStock.getQuantity() - request.getQuantity());
        manufacturerStockRepo.save(centralStock);

        // 2. CỘNG KHO ĐẠI LÝ
        InventoryStock dealerStock = inventoryRepository
                .findByVariantVariantIdAndColorAndDealerDealerId(
                        request.getVariantId(),
                        request.getColor(),
                        request.getDealerId()
                )
                .orElseGet(() -> {
                    InventoryStock newStock = new InventoryStock();
                    newStock.setVariant(variant);
                    newStock.setDealer(dealer);
                    newStock.setColor(request.getColor());
                    newStock.setQuantity(0); 
                    newStock.setStatus("In Stock");
                    newStock.setListingPrice(variant.getMsrp()); 
                    return newStock;
                });
        
        dealerStock.setQuantity(dealerStock.getQuantity() + request.getQuantity());
        InventoryStock savedDealerStock = inventoryRepository.save(dealerStock);
        
        // 3. ✅ TỰ ĐỘNG UPDATE STATUS CỦA DEALER REQUEST (APPROVED → SHIPPED)
        DealerRequest updatedRequest = updateRelatedDealerRequestStatus(
                request.getDealerId(), 
                request.getVariantId(), 
                request.getColor(), 
                request.getQuantity()
        );
        
        // 4. ✅ BUILD RESPONSE DTO
        return buildAllocationResponse(savedDealerStock, updatedRequest, dealer.getDealerName());
    }
    
    /**
     * Tìm và update status của DealerRequest liên quan khi allocate xe
     * Logic: Tìm request đã APPROVED, có chứa variant + color tương ứng
     * → Đổi status thành "SHIPPED" 
     * 
     * @return DealerRequest đã được update, hoặc null nếu không tìm thấy
     */
    private DealerRequest updateRelatedDealerRequestStatus(Long dealerId, Long variantId, String color, Integer allocatedQty) {
        // Tìm các request đã APPROVED của dealer này
        List<DealerRequest> approvedRequests = dealerRequestRepository
                .findByDealerDealerIdAndStatus(dealerId, "APPROVED");
        
        if (approvedRequests.isEmpty()) {
            log.info("⚠️ Không tìm thấy DealerRequest APPROVED nào cho dealer {} để update status", dealerId);
            return null;
        }
        
        // Tìm request có chứa variant + color phù hợp
        for (DealerRequest request : approvedRequests) {
            boolean hasMatchingDetail = request.getRequestDetails().stream()
                    .anyMatch(detail -> 
                        detail.getVehicleVariant().getVariantId().equals(variantId) &&
                        detail.getColor().equalsIgnoreCase(color)
                    );
            
            if (hasMatchingDetail) {
                // Đổi status sang SHIPPED
                request.setStatus("SHIPPED");
                request.setShippedDate(LocalDateTime.now());
                DealerRequest savedRequest = dealerRequestRepository.save(request);
                
                log.info("✅ Đã update DealerRequest {} → status = SHIPPED (Dealer: {}, Variant: {}, Color: {})", 
                        request.getRequestId(), dealerId, variantId, color);
                
                // Trả về request đầu tiên tìm thấy
                return savedRequest;
            }
        }
        
        return null;
    }
    
    /**
     * Build AllocationResponse DTO với đầy đủ thông tin cho FE
     */
    private AllocationResponse buildAllocationResponse(InventoryStock dealerStock, 
                                                        DealerRequest updatedRequest, 
                                                        String dealerName) {
        AllocationResponse response = new AllocationResponse();
        
        // 1. Thông tin kho đại lý
        response.setDealerStock(mapToDealerResponse(dealerStock));
        
        // 2. Thông tin request đã update (nếu có)
        if (updatedRequest != null) {
            AllocationResponse.DealerRequestInfo requestInfo = new AllocationResponse.DealerRequestInfo();
            requestInfo.setRequestId(updatedRequest.getRequestId());
            requestInfo.setStatus(updatedRequest.getStatus());
            requestInfo.setDealerName(dealerName);
            
            // Format ngày giờ
            if (updatedRequest.getShippedDate() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                requestInfo.setShippedDate(updatedRequest.getShippedDate().format(formatter));
            }
            
            response.setUpdatedRequest(requestInfo);
            response.setMessage("✅ Phân bổ thành công! Request #" + updatedRequest.getRequestId() + 
                              " đã chuyển sang trạng thái 'Đang vận chuyển'");
        } else {
            response.setMessage("✅ Phân bổ thành công! (Không tìm thấy request liên quan để cập nhật)");
        }
        
        return response;
    }

    @Override
    @Transactional
    public String recallStockFromDealer(AllocationRequest request) {

        // 1. TRỪ KHO ĐẠI LÝ
        InventoryStock dealerStock = inventoryRepository
                .findByVariantVariantIdAndColorAndDealerDealerId(
                        request.getVariantId(),
                        request.getColor(),
                        request.getDealerId()
                )
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in dealer stock"));

        if (dealerStock.getQuantity() < request.getQuantity()) {
            throw new RuntimeException("Not enough stock at dealer to recall");
        }
        dealerStock.setQuantity(dealerStock.getQuantity() - request.getQuantity());
        inventoryRepository.save(dealerStock);

        // 2. CỘNG KHO TỔNG
        ManufacturerStock centralStock = manufacturerStockRepo
                .findByVariantVariantIdAndColor(request.getVariantId(), request.getColor())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in central stock (cannot recall)"));

        centralStock.setQuantity(centralStock.getQuantity() + request.getQuantity());
        manufacturerStockRepo.save(centralStock);

        // 3. ✅ TỰ ĐỘNG UPDATE STATUS CỦA DEALER REQUEST (SHIPPED → APPROVED)
        revertShippedRequestStatus(
                request.getDealerId(), 
                request.getVariantId(), 
                request.getColor()
        );

        return "Recalled " + request.getQuantity() + " items to central warehouse.";
    }
    
    /**
     * Thu hồi xe → đổi status của DealerRequest từ SHIPPED về APPROVED
     * Logic: Tìm request đang SHIPPED, có chứa variant + color tương ứng
     * → Đổi status về "APPROVED" và xóa shippedDate
     * 
     * @param dealerId ID của dealer
     * @param variantId ID của variant
     * @param color Màu xe
     */
    private void revertShippedRequestStatus(Long dealerId, Long variantId, String color) {
        // Tìm các request đang SHIPPED của dealer này
        List<DealerRequest> shippedRequests = dealerRequestRepository
                .findByDealerDealerIdAndStatus(dealerId, "SHIPPED");
        
        if (shippedRequests.isEmpty()) {
            log.info("⚠️ Không tìm thấy DealerRequest SHIPPED nào cho dealer {} để hoàn trạng thái", dealerId);
            return;
        }
        
        // Tìm request có chứa variant + color phù hợp
        for (DealerRequest request : shippedRequests) {
            boolean hasMatchingDetail = request.getRequestDetails().stream()
                    .anyMatch(detail -> 
                        detail.getVehicleVariant().getVariantId().equals(variantId) &&
                        detail.getColor().equalsIgnoreCase(color)
                    );
            
            if (hasMatchingDetail) {
                // Đổi status về APPROVED và xóa shippedDate
                request.setStatus("APPROVED");
                request.setShippedDate(null); // Reset ngày giao hàng
                dealerRequestRepository.save(request);
                
                log.info("✅ Đã hoàn trạng thái DealerRequest {} → status = APPROVED (Thu hồi xe: Dealer: {}, Variant: {}, Color: {})", 
                        request.getRequestId(), dealerId, variantId, color);
                
                // Chỉ update request đầu tiên tìm thấy
                return;
            }
        }
        
        log.info("⚠️ Không tìm thấy DealerRequest SHIPPED nào phù hợp (Dealer: {}, Variant: {}, Color: {})", 
                dealerId, variantId, color);
    }


    // --- HÀM PRIVATE MAPPER ---

    private InventoryResponse mapToDealerResponse(InventoryStock stock) {
        return new InventoryResponse(stock); 
    }

    private ManufacturerStockResponse mapToManufacturerResponse(ManufacturerStock stock) { 
        ManufacturerStockResponse res = new ManufacturerStockResponse();
        
        res.setId(stock.getManufacturerStockId());
        res.setColor(stock.getColor());
        res.setQuantity(stock.getQuantity());
        res.setStatus(stock.getStatus());
        
        if (stock.getVariant() != null) {
            res.setVariantId(stock.getVariant().getVariantId());  // ✅ Thêm variant_id
            res.setVariantName(stock.getVariant().getName());
            if (stock.getVariant().getModel() != null) {
                res.setModelName(stock.getVariant().getModel().getName());
            }
        }
        
        return res;
    }
}