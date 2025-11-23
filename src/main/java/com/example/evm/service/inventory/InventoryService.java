package com.example.evm.service.inventory;

import com.example.evm.dto.inventory.AllocationResponse;
import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.dealer.DealerRequest;
import com.example.evm.entity.inventory.InventoryStock;
import com.example.evm.entity.inventory.ManufacturerStock;
import com.example.evm.entity.vehicle.Vehicle;
import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.repository.dealer.DealerRepository;
import com.example.evm.repository.dealer.DealerRequestRepository;
import com.example.evm.repository.inventory.InventoryStockRepository;
import com.example.evm.repository.inventory.ManufacturerStockRepository;
import com.example.evm.repository.vehicle.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service InventoryServiceNew - Logic allocate/recall xe với architecture mới
 * 
 * Architecture mới:
 * - ManufacturerStock/InventoryStock chỉ lưu warehouse info
 * - Vehicle có manufacturer_stock_id hoặc inventory_stock_id
 * - Allocate = chuyển Vehicle từ kho tổng sang kho dealer
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final VehicleRepository vehicleRepository;
    private final ManufacturerStockRepository manufacturerStockRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final DealerRepository dealerRepository;
    private final DealerRequestRepository dealerRequestRepository;

    /**
     * Phân bổ xe từ kho tổng cho dealer
     * 
     * @param requestId ID request (optional - nếu có thì update request cụ thể, nếu không thì tìm request mới nhất)
     * @param dealerId ID dealer
     * @param variantId ID variant
     * @param color Màu xe
     * @param quantity Số lượng
     * @return AllocationResponse
     */
    @Transactional
    public AllocationResponse allocateVehiclesToDealer(Long requestId, Long dealerId, Long variantId, String color, Integer quantity) {
        log.info("Allocating {} vehicles (variant: {}, color: {}) to dealer {}", quantity, variantId, color, dealerId);

        // 1. Kiểm tra dealer
        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));

        // 2. Tìm xe available trong kho tổng
        List<Vehicle> availableVehicles = vehicleRepository
                .findAvailableInManufacturerStock(variantId, color);

        // 3. ✅ Revert: Kiểm tra số lượng và throw exception nếu không đủ xe
        int availableCount = availableVehicles.size();
        
        // Lấy thông tin variant để tạo message
        String variantInfo = "Unknown variant";
        if (!availableVehicles.isEmpty()) {
            Vehicle firstVehicle = availableVehicles.get(0);
            String modelName = firstVehicle.getVariant().getModel().getName();
            String variantName = firstVehicle.getVariant().getName();
            variantInfo = String.format("%s - %s (màu %s)", modelName, variantName, color);
        } else {
            // Nếu không có xe nào, query variant từ DB
            try {
                Vehicle anyVehicle = vehicleRepository
                        .findAvailableInManufacturerStock(variantId, null)
                        .stream()
                        .findFirst()
                        .orElse(null);
                
                if (anyVehicle != null) {
                    String modelName = anyVehicle.getVariant().getModel().getName();
                    String variantName = anyVehicle.getVariant().getName();
                    variantInfo = String.format("%s - %s (màu %s)", modelName, variantName, color);
                }
            } catch (Exception e) {
                log.warn("Could not fetch variant info", e);
            }
        }
        
        // Kiểm tra số lượng và throw exception nếu không đủ
        if (availableCount < quantity) {
            int shortage = quantity - availableCount;
            String errorMessage = String.format(
                "❌ Không đủ xe để phân bổ!\n" +
                "🚗 Xe yêu cầu: %s\n" +
                "📦 Số lượng yêu cầu: %d xe\n" +
                "📊 Số lượng trong kho: %d xe\n" +
                "⚠️ Thiếu: %d xe",
                variantInfo, quantity, availableCount, shortage
            );
            
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        // 4. Lấy hoặc tạo kho dealer
        InventoryStock dealerStock = inventoryStockRepository
                .findByDealerDealerId(dealerId)
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    InventoryStock newStock = new InventoryStock();
                    newStock.setDealer(dealer);
                    newStock.setStatus("ACTIVE");
                    return inventoryStockRepository.save(newStock);
                });

        // 5. Chuyển xe sang kho dealer (đủ số lượng yêu cầu)
        List<Vehicle> allocatedVehicles = availableVehicles.stream()
                .limit(quantity)
                .peek(vehicle -> {
                    vehicle.setManufacturerStock(null);
                    vehicle.setInventoryStock(dealerStock);
                    vehicle.setStatus("IN_DEALER_STOCK");
                })
                .toList();

        vehicleRepository.saveAll(allocatedVehicles);
        log.info("✅ Allocated {} vehicles to dealer {}", allocatedVehicles.size(), dealer.getDealerName());

        // 6. Update DealerRequest status (APPROVED → SHIPPED)
        DealerRequest updatedRequest = updateDealerRequestStatus(requestId, dealerId, variantId, color);

        // 7. Build response
        return buildAllocationResponse(allocatedVehicles, updatedRequest, dealer, quantity, availableCount, variantId, color);
    }

    /**
     * Thu hồi xe từ dealer về kho tổng
     * 
     * @param requestId ID request (optional - nếu có thì revert request cụ thể, nếu không thì tìm request mới nhất)
     * @param dealerId ID dealer
     * @param variantId ID variant
     * @param color Màu xe
     * @param quantity Số lượng
     */
    @Transactional
    public void recallVehiclesFromDealer(Long requestId, Long dealerId, Long variantId, String color, Integer quantity) {
        log.info("Recalling {} vehicles (variant: {}, color: {}) from dealer {}", quantity, variantId, color, dealerId);

        // 1. ✅ Sửa: Tìm TẤT CẢ xe của dealer theo variant+color mà vẫn còn trong kho dealer
        // (inventoryStock != null), bất kể status (IN_DEALER_STOCK, IN_TRANSIT, TEST_DRIVE, etc.)
        // Loại trừ những xe đã bán (status = "SOLD" hoặc inventoryStock = null)
        List<Vehicle> dealerVehicles = vehicleRepository.findByDealerIdWithFullInfo(dealerId)
                .stream()
                .filter(v -> v.getVariant() != null 
                          && v.getVariant().getVariantId().equals(variantId) 
                          && v.getColor() != null
                          && v.getColor().equalsIgnoreCase(color)
                          && v.getInventoryStock() != null  // Vẫn còn trong kho dealer
                          && !"SOLD".equalsIgnoreCase(v.getStatus()))  // Chưa bán
                .limit(quantity)
                .toList();

        // 2. Lấy thông tin variant và dealer để tạo message
        String variantInfo = "Unknown variant";
        String dealerName = "Unknown dealer";
        
        try {
            Dealer dealer = dealerRepository.findById(dealerId)
                    .orElse(null);
            if (dealer != null) {
                dealerName = dealer.getDealerName();
            }
            
            if (!dealerVehicles.isEmpty()) {
                Vehicle firstVehicle = dealerVehicles.get(0);
                String modelName = firstVehicle.getVariant().getModel().getName();
                String variantName = firstVehicle.getVariant().getName();
                variantInfo = String.format("%s - %s (màu %s)", modelName, variantName, color);
            } else {
                // Nếu không có xe nào, query variant từ DB
                try {
                    Vehicle anyVehicle = vehicleRepository
                            .findByDealerIdWithFullInfo(dealerId)
                            .stream()
                            .filter(v -> v.getVariant() != null 
                                      && v.getVariant().getVariantId().equals(variantId))
                            .findFirst()
                            .orElse(null);
                    
                    if (anyVehicle != null) {
                        String modelName = anyVehicle.getVariant().getModel().getName();
                        String variantName = anyVehicle.getVariant().getName();
                        variantInfo = String.format("%s - %s (màu %s)", modelName, variantName, color);
                    }
                } catch (Exception e) {
                    log.warn("Could not fetch variant info", e);
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch dealer/variant info", e);
        }
        
        // 3. ✅ Revert: Yêu cầu phải thu hồi đủ số lượng, nếu không đủ thì throw exception
        int availableCount = dealerVehicles.size();
        
        if (availableCount < quantity) {
            // Không đủ xe để thu hồi - throw exception với thông tin chi tiết
            int shortage = quantity - availableCount;
            
            // Đếm tổng số xe (kể cả đã bán) để thông báo rõ ràng
            long totalVehicles = vehicleRepository.findByDealerIdWithFullInfo(dealerId)
                    .stream()
                    .filter(v -> v.getVariant() != null 
                              && v.getVariant().getVariantId().equals(variantId) 
                              && v.getColor() != null
                              && v.getColor().equalsIgnoreCase(color))
                    .count();
            
            long soldCount = totalVehicles - availableCount;
            
            String errorMessage = String.format(
                "❌ Không đủ xe để thu hồi!\n" +
                "🏢 Đại lý: %s\n" +
                "🚗 Xe yêu cầu thu hồi: %s\n" +
                "📦 Số lượng yêu cầu: %d xe\n" +
                "📊 Số lượng có thể thu hồi: %d xe\n" +
                "⚠️ Thiếu: %d xe\n" +
                "ℹ️ Tổng số xe đã phân bổ: %d xe (trong đó %d xe đã bán, không thể thu hồi)",
                dealerName, variantInfo, quantity, availableCount, shortage, totalVehicles, soldCount
            );
            
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        // 4. Lấy kho tổng mặc định
        ManufacturerStock warehouse = manufacturerStockRepository
                .findByStatus("ACTIVE")
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No active warehouse found"));

        // 5. Chuyển xe về kho tổng (đủ số lượng yêu cầu)
        dealerVehicles.forEach(vehicle -> {
            vehicle.setInventoryStock(null);
            vehicle.setManufacturerStock(warehouse);
            vehicle.setStatus("IN_MANUFACTURER_STOCK");
        });

        vehicleRepository.saveAll(dealerVehicles);
        log.info("✅ Recalled {} vehicles from dealer {} to warehouse", dealerVehicles.size(), dealerId);

        // 5. Revert DealerRequest status (SHIPPED → APPROVED)
        revertDealerRequestStatus(requestId, dealerId, variantId, color);
    }

    /**
     * Update DealerRequest status: APPROVED → SHIPPED
     * ✅ Sửa: Nếu có requestId thì update request cụ thể, nếu không thì tìm request mới nhất
     */
    private DealerRequest updateDealerRequestStatus(Long requestId, Long dealerId, Long variantId, String color) {
        DealerRequest requestToUpdate = null;
        
        // Nếu có requestId, tìm request cụ thể
        if (requestId != null) {
            requestToUpdate = dealerRequestRepository.findById(requestId)
                    .filter(r -> "APPROVED".equals(r.getStatus()))
                    .filter(r -> r.getDealer().getDealerId().equals(dealerId))
                    .filter(r -> r.getRequestDetails().stream()
                            .anyMatch(detail -> 
                                detail.getVehicleVariant() != null &&
                                detail.getVehicleVariant().getVariantId().equals(variantId) &&
                                detail.getColor() != null &&
                                detail.getColor().equalsIgnoreCase(color)
                            ))
                    .orElse(null);
            
            if (requestToUpdate != null) {
                requestToUpdate.setStatus("SHIPPED");
                requestToUpdate.setShippedDate(LocalDateTime.now());
                DealerRequest saved = dealerRequestRepository.save(requestToUpdate);
                log.info("✅ Updated DealerRequest {} → SHIPPED (specific request)", saved.getRequestId());
                return saved;
            } else {
                log.warn("⚠️ DealerRequest {} not found or not match criteria (dealer: {}, variant: {}, color: {})", 
                        requestId, dealerId, variantId, color);
            }
        }
        
        // Nếu không có requestId hoặc không tìm thấy, tìm request mới nhất
        List<DealerRequest> approvedRequests = dealerRequestRepository
                .findByDealerDealerIdAndStatus(dealerId, "APPROVED");

        DealerRequest latestMatchingRequest = approvedRequests.stream()
                .filter(request -> {
                    boolean hasMatch = request.getRequestDetails().stream()
                            .anyMatch(detail -> 
                                detail.getVehicleVariant() != null &&
                                detail.getVehicleVariant().getVariantId().equals(variantId) &&
                                detail.getColor() != null &&
                                detail.getColor().equalsIgnoreCase(color)
                            );
                    return hasMatch;
                })
                .max((r1, r2) -> {
                    // So sánh theo approvedDate (nếu có), nếu không thì theo requestDate
                    LocalDateTime date1 = r1.getApprovedDate() != null ? r1.getApprovedDate() : r1.getRequestDate();
                    LocalDateTime date2 = r2.getApprovedDate() != null ? r2.getApprovedDate() : r2.getRequestDate();
                    
                    if (date1 == null && date2 == null) return 0;
                    if (date1 == null) return -1;
                    if (date2 == null) return 1;
                    
                    return date2.compareTo(date1); // Mới nhất trước (DESC - date2 so với date1)
                })
                .orElse(null);

        if (latestMatchingRequest != null) {
            latestMatchingRequest.setStatus("SHIPPED");
            latestMatchingRequest.setShippedDate(LocalDateTime.now());
            DealerRequest saved = dealerRequestRepository.save(latestMatchingRequest);
            
            log.info("✅ Updated DealerRequest {} → SHIPPED (latest matching request)", saved.getRequestId());
            return saved;
        }

        log.warn("⚠️ No matching APPROVED DealerRequest found for dealer {} (variant: {}, color: {})", 
                dealerId, variantId, color);
        return null;
    }

    /**
     * Revert DealerRequest status: SHIPPED → APPROVED
     * ✅ Sửa: Nếu có requestId thì revert request cụ thể, nếu không thì tìm request mới nhất
     */
    private void revertDealerRequestStatus(Long requestId, Long dealerId, Long variantId, String color) {
        DealerRequest requestToRevert = null;
        
        // Nếu có requestId, tìm request cụ thể
        if (requestId != null) {
            requestToRevert = dealerRequestRepository.findById(requestId)
                    .filter(r -> "SHIPPED".equals(r.getStatus()))
                    .filter(r -> r.getDealer().getDealerId().equals(dealerId))
                    .filter(r -> r.getRequestDetails().stream()
                            .anyMatch(detail -> 
                                detail.getVehicleVariant() != null &&
                                detail.getVehicleVariant().getVariantId().equals(variantId) &&
                                detail.getColor() != null &&
                                detail.getColor().equalsIgnoreCase(color)
                            ))
                    .orElse(null);
            
            if (requestToRevert != null) {
                requestToRevert.setStatus("APPROVED");
                requestToRevert.setShippedDate(null);
                dealerRequestRepository.save(requestToRevert);
                log.info("✅ Reverted DealerRequest {} → APPROVED (specific request)", requestToRevert.getRequestId());
                return;
            } else {
                log.warn("⚠️ DealerRequest {} not found or not match criteria (dealer: {}, variant: {}, color: {})", 
                        requestId, dealerId, variantId, color);
            }
        }
        
        // Nếu không có requestId hoặc không tìm thấy, tìm request mới nhất
        List<DealerRequest> shippedRequests = dealerRequestRepository
                .findByDealerDealerIdAndStatus(dealerId, "SHIPPED");

        DealerRequest latestMatchingRequest = shippedRequests.stream()
                .filter(request -> {
                    boolean hasMatch = request.getRequestDetails().stream()
                            .anyMatch(detail -> 
                                detail.getVehicleVariant() != null &&
                                detail.getVehicleVariant().getVariantId().equals(variantId) &&
                                detail.getColor() != null &&
                                detail.getColor().equalsIgnoreCase(color)
                            );
                    return hasMatch;
                })
                .max((r1, r2) -> {
                    // So sánh theo shippedDate (nếu có), nếu không thì theo requestDate
                    LocalDateTime date1 = r1.getShippedDate() != null ? r1.getShippedDate() : r1.getRequestDate();
                    LocalDateTime date2 = r2.getShippedDate() != null ? r2.getShippedDate() : r2.getRequestDate();
                    
                    if (date1 == null && date2 == null) return 0;
                    if (date1 == null) return -1;
                    if (date2 == null) return 1;
                    
                    return date2.compareTo(date1); // Mới nhất trước (DESC - date2 so với date1)
                })
                .orElse(null);

        if (latestMatchingRequest != null) {
            latestMatchingRequest.setStatus("APPROVED");
            latestMatchingRequest.setShippedDate(null);
            dealerRequestRepository.save(latestMatchingRequest);
            
            log.info("✅ Reverted DealerRequest {} → APPROVED (latest matching request)", latestMatchingRequest.getRequestId());
        } else {
            log.warn("⚠️ No matching SHIPPED DealerRequest found for dealer {} (variant: {}, color: {})", 
                    dealerId, variantId, color);
        }
    }

    /**
     * Build AllocationResponse
     */
    private AllocationResponse buildAllocationResponse(List<Vehicle> vehicles, 
                                                        DealerRequest request, 
                                                        Dealer dealer,
                                                        int requestedQuantity,
                                                        int availableQuantity,
                                                        Long variantId,
                                                        String color) {
        List<Long> vehicleIds = vehicles.stream()
                .map(Vehicle::getVehicleId)
                .toList();
        
        // Tạo message chi tiết
        String message = String.format("✅ Đã phân bổ đầy đủ %d xe cho %s", 
                vehicles.size(), dealer.getDealerName());
        
        // ✅ Luôn trả về variantId và color từ request, kể cả khi không có xe
        return AllocationResponse.builder()
                .message(message)
                .quantity(vehicles.size())
                .vehicleIds(vehicleIds)
                .dealerId(dealer.getDealerId())
                .variantId(variantId) // Luôn dùng từ request
                .color(color) // Luôn dùng từ request
                .requestId(request != null ? request.getRequestId() : null) // Thêm requestId vào response
                .build();
    }
}

