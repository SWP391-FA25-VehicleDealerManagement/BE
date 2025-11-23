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
     * @param dealerId ID dealer
     * @param variantId ID variant
     * @param color Màu xe
     * @param quantity Số lượng
     * @return AllocationResponse
     */
    @Transactional
    public AllocationResponse allocateVehiclesToDealer(Long dealerId, Long variantId, String color, Integer quantity) {
        log.info("Allocating {} vehicles (variant: {}, color: {}) to dealer {}", quantity, variantId, color, dealerId);

        // 1. Kiểm tra dealer
        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));

        // 2. Tìm xe available trong kho tổng
        List<Vehicle> availableVehicles = vehicleRepository
                .findAvailableInManufacturerStock(variantId, color);

        // 3. ✅ Sửa: Cho phép phân bổ ngay cả khi không đủ xe - chỉ phân bổ những xe có sẵn
        int availableCount = availableVehicles.size();
        int quantityToAllocate = Math.min(availableCount, quantity);
        
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
        
        // Log warning nếu không đủ xe, nhưng vẫn tiếp tục phân bổ
        if (availableCount < quantity) {
            int shortage = quantity - availableCount;
            log.warn("⚠️ Không đủ xe để phân bổ đầy đủ!\n" +
                    "🚗 Xe yêu cầu: {}\n" +
                    "📦 Số lượng yêu cầu: {} xe\n" +
                    "📊 Số lượng trong kho: {} xe\n" +
                    "⚠️ Thiếu: {} xe\n" +
                    "✅ Sẽ phân bổ {} xe có sẵn. Phần còn lại sẽ được giao sau.",
                    variantInfo, quantity, availableCount, shortage, quantityToAllocate);
        }
        
        // Nếu không có xe nào, vẫn cho phép nhưng log warning
        if (availableCount == 0) {
            log.warn("⚠️ Không có xe nào trong kho cho variant {} (màu {}). " +
                    "Phân bổ sẽ được thực hiện khi có xe. Hãng sẽ giao sau.", variantInfo, color);
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

        // 5. Chuyển xe sang kho dealer (chỉ những xe có sẵn)
        List<Vehicle> allocatedVehicles = availableVehicles.stream()
                .limit(quantityToAllocate)
                .peek(vehicle -> {
                    vehicle.setManufacturerStock(null);
                    vehicle.setInventoryStock(dealerStock);
                    vehicle.setStatus("IN_DEALER_STOCK");
                })
                .toList();

        // Chỉ save nếu có xe để phân bổ
        if (!allocatedVehicles.isEmpty()) {
            vehicleRepository.saveAll(allocatedVehicles);
            log.info("✅ Allocated {} vehicles to dealer {} (requested: {}, available: {})", 
                    allocatedVehicles.size(), dealer.getDealerName(), quantity, availableCount);
        } else {
            log.info("✅ No vehicles to allocate at the moment. Allocation will be processed when vehicles become available.");
        }

        // 6. Update DealerRequest status (APPROVED → SHIPPED) - chỉ khi có xe được phân bổ
        DealerRequest updatedRequest = null;
        if (!allocatedVehicles.isEmpty()) {
            updatedRequest = updateDealerRequestStatus(dealerId, variantId, color);
        }

        // 7. Build response với thông tin về số lượng yêu cầu và số lượng thiếu
        return buildAllocationResponse(allocatedVehicles, updatedRequest, dealer, quantity, availableCount, variantId, color);
    }

    /**
     * Thu hồi xe từ dealer về kho tổng
     * 
     * @param dealerId ID dealer
     * @param variantId ID variant
     * @param color Màu xe
     * @param quantity Số lượng
     */
    @Transactional
    public void recallVehiclesFromDealer(Long dealerId, Long variantId, String color, Integer quantity) {
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
        
        // 3. ✅ Yêu cầu: Phải thu hồi đủ số lượng, nếu không đủ thì throw exception
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
        revertDealerRequestStatus(dealerId, variantId, color);
    }

    /**
     * Update DealerRequest status: APPROVED → SHIPPED
     */
    private DealerRequest updateDealerRequestStatus(Long dealerId, Long variantId, String color) {
        List<DealerRequest> approvedRequests = dealerRequestRepository
                .findByDealerDealerIdAndStatus(dealerId, "APPROVED");

        for (DealerRequest request : approvedRequests) {
            boolean hasMatch = request.getRequestDetails().stream()
                    .anyMatch(detail -> 
                        detail.getVehicleVariant() != null &&
                        detail.getVehicleVariant().getVariantId().equals(variantId) &&
                        detail.getColor() != null &&
                        detail.getColor().equalsIgnoreCase(color)
                    );

            if (hasMatch) {
                request.setStatus("SHIPPED");
                request.setShippedDate(LocalDateTime.now());
                DealerRequest saved = dealerRequestRepository.save(request);
                
                log.info("✅ Updated DealerRequest {} → SHIPPED", request.getRequestId());
                return saved;
            }
        }

        log.warn("⚠️ No matching DealerRequest found for dealer {} (variant: {}, color: {})", 
                dealerId, variantId, color);
        return null;
    }

    /**
     * Revert DealerRequest status: SHIPPED → APPROVED
     */
    private void revertDealerRequestStatus(Long dealerId, Long variantId, String color) {
        List<DealerRequest> shippedRequests = dealerRequestRepository
                .findByDealerDealerIdAndStatus(dealerId, "SHIPPED");

        for (DealerRequest request : shippedRequests) {
            boolean hasMatch = request.getRequestDetails().stream()
                    .anyMatch(detail -> 
                        detail.getVehicleVariant() != null &&
                        detail.getVehicleVariant().getVariantId().equals(variantId) &&
                        detail.getColor() != null &&
                        detail.getColor().equalsIgnoreCase(color)
                    );

            if (hasMatch) {
                request.setStatus("APPROVED");
                request.setShippedDate(null);
                dealerRequestRepository.save(request);
                
                log.info("✅ Reverted DealerRequest {} → APPROVED", request.getRequestId());
                return;
            }
        }

        log.warn("⚠️ No matching SHIPPED DealerRequest found for dealer {}", dealerId);
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
        String message;
        if (vehicles.isEmpty()) {
            message = String.format("✅ Đã ghi nhận yêu cầu phân bổ %d xe. " +
                    "Hiện tại không có xe trong kho. Hãng sẽ giao sau khi có xe.", requestedQuantity);
        } else if (vehicles.size() < requestedQuantity) {
            int shortage = requestedQuantity - vehicles.size();
            message = String.format("✅ Đã phân bổ %d/%d xe cho %s. " +
                    "Thiếu %d xe, hãng sẽ giao sau.", 
                    vehicles.size(), requestedQuantity, dealer.getDealerName(), shortage);
        } else {
            message = String.format("✅ Đã phân bổ đầy đủ %d xe cho %s", 
                    vehicles.size(), dealer.getDealerName());
        }
        
        // ✅ Luôn trả về variantId và color từ request, kể cả khi không có xe
        return AllocationResponse.builder()
                .message(message)
                .quantity(vehicles.size())
                .vehicleIds(vehicleIds)
                .dealerId(dealer.getDealerId())
                .variantId(variantId) // Luôn dùng từ request
                .color(color) // Luôn dùng từ request
                .build();
    }
}

