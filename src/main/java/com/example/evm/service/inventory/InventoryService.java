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
import java.time.format.DateTimeFormatter;
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

        if (availableVehicles.size() < quantity) {
            throw new IllegalStateException(
                String.format("Not enough vehicles. Requested: %d, Available: %d", 
                    quantity, availableVehicles.size()));
        }

        // 3. Lấy hoặc tạo kho dealer
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

        // 4. Chuyển xe sang kho dealer
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

        // 5. Update DealerRequest status (APPROVED → SHIPPED)
        DealerRequest updatedRequest = updateDealerRequestStatus(dealerId, variantId, color);

        // 6. Build response
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

        if (dealerVehicles.size() < quantity) {
            throw new IllegalStateException(
                String.format("Not enough vehicles to recall. Requested: %d, Available: %d", 
                    quantity, dealerVehicles.size()));
        }

        // 2. Lấy kho tổng mặc định
        ManufacturerStock warehouse = manufacturerStockRepository
                .findByStatus("ACTIVE")
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No active warehouse found"));

        // 3. Chuyển xe về kho tổng
        dealerVehicles.forEach(vehicle -> {
            vehicle.setInventoryStock(null);
            vehicle.setManufacturerStock(warehouse);
            vehicle.setStatus("IN_MANUFACTURER_STOCK");
        });

        vehicleRepository.saveAll(dealerVehicles);

        log.info("✅ Recalled {} vehicles from dealer {} to warehouse", dealerVehicles.size(), dealerId);

        // 4. Revert DealerRequest status (SHIPPED → APPROVED)
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

