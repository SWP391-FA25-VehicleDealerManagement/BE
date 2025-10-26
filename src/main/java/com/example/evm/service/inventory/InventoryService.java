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

        // 3. Kiểm tra số lượng và tạo message chi tiết nếu không đủ
        if (availableVehicles.size() < quantity) {
            // Lấy thông tin variant để tạo message chi tiết
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
            
            String errorMessage = String.format(
                "❌ Không đủ xe để phân bổ!\n" +
                "🚗 Xe yêu cầu: %s\n" +
                "📦 Số lượng yêu cầu: %d xe\n" +
                "📊 Số lượng trong kho: %d xe\n" +
                "⚠️ Thiếu: %d xe",
                variantInfo, quantity, availableVehicles.size(), (quantity - availableVehicles.size())
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

        // 5. Chuyển xe sang kho dealer
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
        DealerRequest updatedRequest = updateDealerRequestStatus(dealerId, variantId, color);

        // 7. Build response
        return buildAllocationResponse(allocatedVehicles, updatedRequest, dealer);
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

        // 1. Tìm xe của dealer
        List<Vehicle> dealerVehicles = vehicleRepository.findByDealerIdWithFullInfo(dealerId)
                .stream()
                .filter(v -> v.getVariant().getVariantId().equals(variantId) 
                          && v.getColor().equalsIgnoreCase(color)
                          && "IN_DEALER_STOCK".equals(v.getStatus()))
                .limit(quantity)
                .toList();

        // 2. Kiểm tra số lượng và tạo message chi tiết nếu không đủ
        if (dealerVehicles.size() < quantity) {
            // Lấy thông tin variant để tạo message chi tiết
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
                }
            } catch (Exception e) {
                log.warn("Could not fetch dealer/variant info", e);
            }
            
            String errorMessage = String.format(
                "❌ Không đủ xe để thu hồi!\n" +
                "🏢 Đại lý: %s\n" +
                "🚗 Xe yêu cầu thu hồi: %s\n" +
                "📦 Số lượng yêu cầu: %d xe\n" +
                "📊 Số lượng trong kho đại lý: %d xe\n" +
                "⚠️ Thiếu: %d xe",
                dealerName, variantInfo, quantity, dealerVehicles.size(), (quantity - dealerVehicles.size())
            );
            
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        // 3. Lấy kho tổng mặc định
        ManufacturerStock warehouse = manufacturerStockRepository
                .findByStatus("ACTIVE")
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No active warehouse found"));

        // 4. Chuyển xe về kho tổng
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
                        detail.getVehicleVariant().getVariantId().equals(variantId) &&
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
                        detail.getVehicleVariant().getVariantId().equals(variantId) &&
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
                                                        Dealer dealer) {
        List<Long> vehicleIds = vehicles.stream()
                .map(Vehicle::getVehicleId)
                .toList();
        
        return AllocationResponse.builder()
                .message(String.format("✅ Allocated %d vehicles to %s", 
                        vehicles.size(), dealer.getDealerName()))
                .quantity(vehicles.size())
                .vehicleIds(vehicleIds)
                .dealerId(dealer.getDealerId())
                .variantId(vehicles.isEmpty() ? null : vehicles.get(0).getVariant().getVariantId())
                .color(vehicles.isEmpty() ? null : vehicles.get(0).getColor())
                .build();
    }
}

