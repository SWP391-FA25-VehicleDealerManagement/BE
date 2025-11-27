package com.example.evm.service.inventory;

import com.example.evm.dto.inventory.AllocationRequest;
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
import com.example.evm.repository.vehicle.VehicleVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final VehicleVariantRepository variantRepository;

    /**
     * Phân bổ xe từ kho tổng cho dealer - Hỗ trợ nhiều variant/màu/số lượng
     * 
     * @param requestId ID request cần allocate
     * @param dealerId  ID dealer
     * @param items     Danh sách items cần allocate (variant, color, quantity)
     * @return AllocationResponse tổng hợp
     */
    @Transactional
    public AllocationResponse allocateVehiclesToDealer(Long requestId, Long dealerId, 
            List<AllocationRequest.AllocationItem> items) {
        
        log.info("Allocating {} items to dealer {} for request ID: {}", items.size(), dealerId, requestId);

        // 1. Kiểm tra dealer
        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));

        // 2. Kiểm tra tồn kho cho TẤT CẢ items trước khi allocate
        List<String> shortageMessages = new ArrayList<>();
        
        for (AllocationRequest.AllocationItem item : items) {
            List<Vehicle> availableVehicles = vehicleRepository
                    .findAvailableInManufacturerStock(item.getVariantId(), item.getColor());
            
            int availableCount = availableVehicles.size();
            
            if (availableCount < item.getQuantity()) {
                // Lấy thông tin variant
                String variantInfo;
                try {
                    if (!availableVehicles.isEmpty()) {
                        Vehicle firstVehicle = availableVehicles.get(0);
                        String modelName = firstVehicle.getVariant().getModel().getName();
                        String variantName = firstVehicle.getVariant().getName();
                        variantInfo = String.format("%s - %s (màu %s)", modelName, variantName, item.getColor());
                    } else {
                        // Query trực tiếp từ VehicleVariantRepository
                        variantInfo = variantRepository.findById(item.getVariantId())
                                .map(variant -> String.format("%s - %s (màu %s)", 
                                        variant.getModel().getName(), 
                                        variant.getName(), 
                                        item.getColor()))
                                .orElse("Unknown variant");
                    }
                } catch (Exception e) {
                    log.warn("Could not fetch variant info for variantId: {}", item.getVariantId(), e);
                    variantInfo = "Unknown variant";
                }
                
                int shortage = item.getQuantity() - availableCount;
                String message = String.format(
                    "• %s: Yêu cầu %d xe, kho có %d xe, thiếu %d xe",
                    variantInfo, item.getQuantity(), availableCount, shortage
                );
                shortageMessages.add(message);
            }
        }
        
        // Nếu có bất kỳ item nào thiếu xe, throw exception với thông tin đầy đủ
        if (!shortageMessages.isEmpty()) {
            String errorMessage = "Không đủ xe để phân bổ!\n\n" + String.join("\n", shortageMessages);
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
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

        // 4. Allocate từng item (đã đảm bảo đủ xe)
        List<Long> allVehicleIds = new ArrayList<>();
        int totalAllocated = 0;
        StringBuilder messageBuilder = new StringBuilder();

        for (AllocationRequest.AllocationItem item : items) {
            AllocationResponse itemResponse = allocateSingleItem(
                    requestId, dealerId, item.getVariantId(), item.getColor(), 
                    item.getQuantity(), dealer, dealerStock);
            
            allVehicleIds.addAll(itemResponse.getVehicleIds());
            totalAllocated += itemResponse.getQuantity();
            messageBuilder.append(itemResponse.getMessage()).append("\n");
        }

        // 5. Lưu vehicleIds vào notes của DealerRequest để recall đúng xe
        DealerRequest request = dealerRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
        
        String vehicleIdsStr = allVehicleIds.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(","));
        
        String existingNotes = request.getNotes() != null ? request.getNotes() : "";
        String newNotes = existingNotes.isEmpty() 
                ? "ALLOCATED_VEHICLES:" + vehicleIdsStr
                : existingNotes + " | ALLOCATED_VEHICLES:" + vehicleIdsStr;
        request.setNotes(newNotes);
        dealerRequestRepository.save(request);
        
        log.info("Saved {} vehicle IDs to request {} notes: {}", allVehicleIds.size(), requestId, vehicleIdsStr);

        // 6. Update DealerRequest status
        updateDealerRequestStatus(requestId, dealerId, null, null);

        // 6. Build tổng hợp response
        return AllocationResponse.builder()
                .message(String.format("Đã phân bổ tổng cộng %d xe cho %s", 
                        totalAllocated, dealer.getDealerName()))
                .quantity(totalAllocated)
                .vehicleIds(allVehicleIds)
                .dealerId(dealerId)
                .requestId(requestId)
                .build();
    }

    /**
     * Allocate một loại xe cụ thể (single variant/color/quantity)
     */
    private AllocationResponse allocateSingleItem(Long requestId, Long dealerId, Long variantId, 
            String color, Integer quantity, Dealer dealer, InventoryStock dealerStock) {
        log.info("Allocating {} vehicles (variant: {}, color: {}) to dealer {}", quantity, variantId, color, dealerId);

        // 1. Tìm xe available trong kho tổng
        List<Vehicle> availableVehicles = vehicleRepository
                .findAvailableInManufacturerStock(variantId, color);

        // 2. Kiểm tra số lượng và throw exception nếu không đủ xe
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
                    "Không đủ xe để phân bổ!\n" +
                            "Xe yêu cầu: %s\n" +
                            "Số lượng yêu cầu: %d xe\n" +
                            "Số lượng trong kho: %d xe\n" +
                            " Thiếu: %d xe",
                    variantInfo, quantity, availableCount, shortage);

            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        // 3. Chuyển xe sang kho dealer (đủ số lượng yêu cầu)
        List<Vehicle> allocatedVehicles = availableVehicles.stream()
                .limit(quantity)
                .peek(vehicle -> {
                    vehicle.setManufacturerStock(null);
                    vehicle.setInventoryStock(dealerStock);
                    vehicle.setStatus("IN_DEALER_STOCK");
                })
                .toList();

        vehicleRepository.saveAll(allocatedVehicles);
        log.info("Allocated {} vehicles to dealer {}", allocatedVehicles.size(), dealer.getDealerName());

        // 4. Build response
        List<Long> vehicleIds = allocatedVehicles.stream()
                .map(Vehicle::getVehicleId)
                .toList();

        String message = String.format("Đã phân bổ %d xe %s cho %s",
                allocatedVehicles.size(), variantInfo, dealer.getDealerName());

        return AllocationResponse.builder()
                .message(message)
                .quantity(allocatedVehicles.size())
                .vehicleIds(vehicleIds)
                .dealerId(dealerId)
                .variantId(variantId)
                .color(color)
                .requestId(requestId)
                .build();
    }

    /**
     * Thu hồi xe từ dealer về kho tổng
     *  FIX: Recall đúng những xe đã được allocate từ request này (dựa trên vehicleIds lưu trong notes)
     * 
     * @param requestId ID request cần thu hồi
     * @param dealerId  ID dealer
     */
    @Transactional
    public void recallVehiclesFromDealer(Long requestId, Long dealerId) {
        log.info("Recalling vehicles from dealer {} for request ID: {}", dealerId, requestId);

        // 1. Tìm request để lấy vehicleIds đã allocate
        DealerRequest request = dealerRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (!"SHIPPED".equals(request.getStatus())) {
            throw new IllegalStateException("Chỉ có thể thu hồi xe từ request đang ở trạng thái SHIPPED");
        }

        // 2. Parse vehicleIds từ notes
        List<Long> allocatedVehicleIds = new ArrayList<>();
        String notes = request.getNotes();
        if (notes != null && notes.contains("ALLOCATED_VEHICLES:")) {
            try {
                String vehicleIdsPart = notes.substring(notes.indexOf("ALLOCATED_VEHICLES:") + "ALLOCATED_VEHICLES:".length());
                // Lấy phần đầu tiên (trước dấu | nếu có)
                if (vehicleIdsPart.contains(" | ")) {
                    vehicleIdsPart = vehicleIdsPart.substring(0, vehicleIdsPart.indexOf(" | "));
                }
                String[] vehicleIdStrs = vehicleIdsPart.trim().split(",");
                for (String idStr : vehicleIdStrs) {
                    try {
                        allocatedVehicleIds.add(Long.parseLong(idStr.trim()));
                    } catch (NumberFormatException e) {
                        log.warn("Invalid vehicle ID in notes: {}", idStr);
                    }
                }
                log.info("Found {} allocated vehicle IDs from request notes: {}", allocatedVehicleIds.size(), allocatedVehicleIds);
            } catch (Exception e) {
                log.warn("Failed to parse vehicle IDs from notes: {}", e.getMessage());
            }
        }

        // 3. Tìm xe theo vehicleIds đã allocate (nếu có), nếu không thì fallback về logic cũ
        List<Vehicle> dealerVehicles;
        
        if (!allocatedVehicleIds.isEmpty()) {
            //  FIX: Tìm đúng những xe đã được allocate từ request này
            dealerVehicles = allocatedVehicleIds.stream()
                    .map(vehicleId -> vehicleRepository.findById(vehicleId).orElse(null))
                    .filter(vehicle -> vehicle != null
                            && vehicle.getInventoryStock() != null
                            && vehicle.getInventoryStock().getDealer() != null
                            && vehicle.getInventoryStock().getDealer().getDealerId().equals(dealerId)
                            && !"SOLD".equalsIgnoreCase(vehicle.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
            
            log.info("Found {} vehicles to recall from allocated vehicle IDs", dealerVehicles.size());
        } else {
            // Fallback: Tìm xe theo variant và color (logic cũ)
            log.warn("No allocated vehicle IDs found in notes, falling back to variant/color matching");
            dealerVehicles = request.getRequestDetails().stream()
                    .flatMap(detail -> {
                        Long variantId = detail.getVehicleVariant().getVariantId();
                        String color = detail.getColor();
                        
                        // Tìm xe theo variant và color, chỉ lấy xe còn trong kho dealer
                        return vehicleRepository.findByDealerIdWithFullInfo(dealerId).stream()
                                .filter(v -> v.getVariant() != null
                                        && v.getVariant().getVariantId().equals(variantId)
                                        && v.getColor() != null
                                        && v.getColor().equalsIgnoreCase(color)
                                        && v.getInventoryStock() != null
                                        && !"SOLD".equalsIgnoreCase(v.getStatus()))
                                .limit(detail.getQuantity()); // Chỉ lấy đúng số lượng đã yêu cầu
                    })
                    .toList();
        }

        if (dealerVehicles.isEmpty()) {
            throw new IllegalStateException("Không tìm thấy xe nào để thu hồi. Có thể xe đã được bán hoặc đã thu hồi trước đó.");
        }

        // 4. Lấy kho tổng mặc định
        ManufacturerStock warehouse = manufacturerStockRepository
                .findByStatus("ACTIVE")
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No active warehouse found"));

        // 5. Chuyển xe về kho tổng
        dealerVehicles.forEach(vehicle -> {
            vehicle.setInventoryStock(null);
            vehicle.setManufacturerStock(warehouse);
            vehicle.setStatus("IN_MANUFACTURER_STOCK");
        });

        vehicleRepository.saveAll(dealerVehicles);
        log.info("Recalled {} vehicles (IDs: {}) from dealer {} to warehouse", 
                dealerVehicles.size(), 
                dealerVehicles.stream().map(Vehicle::getVehicleId).collect(java.util.stream.Collectors.toList()),
                dealerId);

        // 6. Xóa vehicleIds khỏi notes sau khi recall thành công
        if (notes != null && notes.contains("ALLOCATED_VEHICLES:")) {
            String updatedNotes = notes.replaceAll("\\|?\\s*ALLOCATED_VEHICLES:[^|]*", "").trim();
            if (updatedNotes.isEmpty()) {
                updatedNotes = null;
            }
            request.setNotes(updatedNotes);
            dealerRequestRepository.save(request);
            log.info("Removed allocated vehicle IDs from request notes");
        }

        // 7. Revert DealerRequest status (SHIPPED → APPROVED)
        if (requestId != null) {
            revertDealerRequestStatus(requestId, dealerId, null, null);
        }
    }

    /**
     * Update DealerRequest status: APPROVED → SHIPPED
     * Sử dụng: Nếu có requestId thì update request cụ thể, nếu không thì tìm request
     * mới nhất
     */
    private DealerRequest updateDealerRequestStatus(Long requestId, Long dealerId, Long variantId, String color) {

        // CASE 1: Có requestId cụ thể -> Xử lý đích danh
        if (requestId != null) {
            DealerRequest targetRequest = dealerRequestRepository.findById(requestId)
                    .orElse(null);

            if (targetRequest == null) {
                log.warn("Không tìm thấy DealerRequest với ID: {}", requestId);
                return null;
            }

            // Validate logic: Phải đúng Dealer
            if (!targetRequest.getDealer().getDealerId().equals(dealerId)) {
                log.warn("Request {} không thuộc về Dealer {}", requestId, dealerId);
                return null;
            }

            // Validate logic: Phải đang là APPROVED
            if (!"APPROVED".equals(targetRequest.getStatus())) {
                log.warn("Request {} có trạng thái '{}' (Yêu cầu: APPROVED)", requestId, targetRequest.getStatus());
                return null;
            }

            // Nếu variantId và color là null (allocate nhiều items), bỏ qua check variant
            // Chỉ check khi allocate single item
            if (variantId != null) {
                boolean hasMatchingVariant = targetRequest.getRequestDetails().stream()
                        .anyMatch(detail -> detail.getVehicleVariant() != null &&
                                detail.getVehicleVariant().getVariantId().equals(variantId) &&
                                (color == null || detail.getColor().equalsIgnoreCase(color)));

                if (!hasMatchingVariant) {
                    log.warn("Request {} không yêu cầu variant {} màu {}", requestId, variantId, color);
                    return null;
                }
            }

            // All checks passed -> Update
            targetRequest.setStatus("SHIPPED");
            targetRequest.setShippedDate(LocalDateTime.now());
            DealerRequest saved = dealerRequestRepository.save(targetRequest);
            log.info("[STRICT UPDATE] DealerRequest {} → SHIPPED", saved.getRequestId());
            return saved;
        }

        // CASE 2: Không có requestId -> Fallback tìm cái mới nhất (Logic cũ)
        log.info("Không có requestId, tìm request APPROVED mới nhất phù hợp...");

        List<DealerRequest> approvedRequests = dealerRequestRepository
                .findByDealerDealerIdAndStatus(dealerId, "APPROVED");

        DealerRequest latestMatchingRequest = approvedRequests.stream()
                .filter(request -> request.getRequestDetails().stream()
                        .anyMatch(detail -> detail.getVehicleVariant() != null &&
                                detail.getVehicleVariant().getVariantId().equals(variantId) &&
                                (color == null || detail.getColor().equalsIgnoreCase(color))))
                .max((r1, r2) -> {
                    LocalDateTime d1 = r1.getApprovedDate() != null ? r1.getApprovedDate() : r1.getRequestDate();
                    LocalDateTime d2 = r2.getApprovedDate() != null ? r2.getApprovedDate() : r2.getRequestDate();
                    return d2.compareTo(d1); // Mới nhất lên đầu
                })
                .orElse(null);

        if (latestMatchingRequest != null) {
            latestMatchingRequest.setStatus("SHIPPED");
            latestMatchingRequest.setShippedDate(LocalDateTime.now());
            DealerRequest saved = dealerRequestRepository.save(latestMatchingRequest);
            log.info("[AUTO UPDATE] DealerRequest {} → SHIPPED", saved.getRequestId());
            return saved;
        }

        log.warn("Không tìm thấy DealerRequest nào phù hợp để update.");
        return null;
    }

    /**
     * Revert DealerRequest status: SHIPPED → APPROVED
     * Chỉ revert khi có requestId cụ thể
     */
    private void revertDealerRequestStatus(Long requestId, Long dealerId, Long variantId, String color) {

        // CASE 1: Có requestId cụ thể -> Xử lý đích danh
        if (requestId != null) {
            DealerRequest targetRequest = dealerRequestRepository.findById(requestId)
                    .orElse(null);

            if (targetRequest == null) {
                log.warn("[RECALL] Không tìm thấy DealerRequest ID: {}", requestId);
                return;
            }

            if (!"SHIPPED".equals(targetRequest.getStatus())) {
                log.warn("[RECALL] Request {} đang là '{}' (Yêu cầu: SHIPPED để revert)", requestId,
                        targetRequest.getStatus());
                return;
            }

            // Revert ngay lập tức, không tìm kiếm lung tung
            targetRequest.setStatus("APPROVED");
            targetRequest.setShippedDate(null); // Xóa ngày ship
            dealerRequestRepository.save(targetRequest);
            log.info("[STRICT REVERT] DealerRequest {} reverted to APPROVED", requestId);
            return;
        }

        // CASE 2: Không có requestId -> Fallback logic cũ
        log.info("[RECALL] Không có requestId, tìm request SHIPPED mới nhất để revert...");

        List<DealerRequest> shippedRequests = dealerRequestRepository
                .findByDealerDealerIdAndStatus(dealerId, "SHIPPED");

        DealerRequest latestMatchingRequest = shippedRequests.stream()
                .filter(request -> request.getRequestDetails().stream()
                        .anyMatch(detail -> detail.getVehicleVariant() != null &&
                                detail.getVehicleVariant().getVariantId().equals(variantId) &&
                                (color == null || detail.getColor().equalsIgnoreCase(color))))
                .max((r1, r2) -> {
                    LocalDateTime d1 = r1.getShippedDate() != null ? r1.getShippedDate() : r1.getRequestDate();
                    LocalDateTime d2 = r2.getShippedDate() != null ? r2.getShippedDate() : r2.getRequestDate();
                    return d2.compareTo(d1);
                })
                .orElse(null);

        if (latestMatchingRequest != null) {
            latestMatchingRequest.setStatus("APPROVED");
            latestMatchingRequest.setShippedDate(null);
            dealerRequestRepository.save(latestMatchingRequest);
            log.info("[AUTO REVERT] DealerRequest {} reverted to APPROVED", latestMatchingRequest.getRequestId());
        } else {
            log.warn("Không tìm thấy DealerRequest SHIPPED nào phù hợp để revert.");
        }
    }

}
