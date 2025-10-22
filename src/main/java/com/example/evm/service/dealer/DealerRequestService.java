package com.example.evm.service.dealer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.evm.dto.dealer.DealerRequestDto;
import com.example.evm.dto.dealer.RequestDetailDto;
import com.example.evm.dto.dealer.DealerRequestResponse;
import com.example.evm.dto.dealer.RequestDetailResponse;
import com.example.evm.entity.dealer.DealerRequest;
import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.user.User;
import com.example.evm.entity.vehicle.VehicleVariant;
import com.example.evm.exception.ResourceNotFoundException;

import com.example.evm.repository.dealer.DealerRequestRepository;
import com.example.evm.entity.dealer.DealerRequestDetail;

import com.example.evm.repository.dealer.DealerRepository;
import com.example.evm.repository.auth.UserRepository;
import com.example.evm.repository.vehicle.VehicleVariantRepository;
import com.example.evm.repository.inventory.ManufacturerStockRepository;
import com.example.evm.repository.inventory.InventoryStockRepository;
import com.example.evm.entity.inventory.ManufacturerStock;
import com.example.evm.entity.inventory.InventoryStock;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealerRequestService {
    private final DealerRequestRepository dealerRequestRepository;  
    private final DealerRepository dealerRepository;
    private final UserRepository userRepository;
    private final VehicleVariantRepository vehicleVariantRepository;
    private final ManufacturerStockRepository manufacturerStockRepository;
    private final InventoryStockRepository inventoryStockRepository;

    // ✅ Trả về DTO để tránh lazy proxy errors
    public List<DealerRequestResponse> getAllRequests() {
        return dealerRequestRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public List<DealerRequestResponse> getRequestsByDealer(Long dealerId) {
        return dealerRequestRepository.findByDealerDealerId(dealerId).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public List<DealerRequestResponse> getRequestsByUser(Long userId) {
        return dealerRequestRepository.findByCreatedByUserId(userId).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public List<DealerRequestResponse> getRequestsByStatus(String status) {
        return dealerRequestRepository.findByStatus(status).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public List<DealerRequestResponse> getPendingRequests() {
        return dealerRequestRepository.findPendingRequests().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public DealerRequestResponse getRequestById(Long id) {
        DealerRequest request = getRequestEntityById(id);
        return convertToResponseDto(request);
    }
    
    // ✅ Internal method để lấy entity (dùng cho update/delete)
    private DealerRequest getRequestEntityById(Long id) {
        return dealerRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer request not found with id: " + id));
    }

    public List<RequestDetailResponse> getRequestDetails(Long requestId) {
        DealerRequest request = dealerRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer request not found with id: " + requestId));
        
        return request.getRequestDetails().stream()
                .map(detail -> {
                    RequestDetailResponse dto = new RequestDetailResponse();
                    dto.setRequestDetailId(detail.getRequestDetailId());
                    dto.setVariantId(detail.getVehicleVariant().getVariantId());  // ✅ Thêm variant_id
                    dto.setVariantName(detail.getVehicleVariant().getName());
                    dto.setModelName(detail.getVehicleVariant().getModel() != null ? 
                            detail.getVehicleVariant().getModel().getName() : null);
                    dto.setColor(detail.getColor());  // ✅ Thêm màu sắc
                    dto.setQuantity(detail.getQuantity());
                    dto.setUnitPrice(detail.getUnitPrice());
                    dto.setLineTotal(detail.getLineTotal());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // ✅ Method mới: Tạo request từ DTO (chỉ cần IDs)
    @Transactional
    public DealerRequestResponse createRequestFromDto(DealerRequestDto dto) {
        // 1. Validate và lookup entities chỉ bằng ID
        Dealer dealer = dealerRepository.findById(dto.getDealerId())
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found with ID: " + dto.getDealerId()));
        
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + dto.getUserId()));

        // 2. Tạo DealerRequest entity
        DealerRequest request = new DealerRequest();
        request.setDealer(dealer);
        request.setCreatedBy(user);
        request.setRequestDate(LocalDateTime.now());
        request.setRequiredDate(dto.getRequiredDate());
        request.setPriority(dto.getPriority());
        request.setNotes(dto.getNotes());
        request.setStatus("PENDING");

        // 3. Process request details
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (RequestDetailDto detailDto : dto.getRequestDetails()) {
            // Lookup variant chỉ bằng ID
            VehicleVariant variant = vehicleVariantRepository.findById(detailDto.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle variant not found with ID: " + detailDto.getVariantId()));
            
            // Tạo detail entity
            DealerRequestDetail detail = new DealerRequestDetail();
            detail.setVehicleVariant(variant);
            detail.setColor(detailDto.getColor());  // ✅ Set màu sắc
            detail.setQuantity(detailDto.getQuantity());
            detail.setUnitPrice(detailDto.getUnitPrice());
            detail.setNotes(detailDto.getNotes());
            detail.setDealerRequest(request);
            
            request.getRequestDetails().add(detail);
            
            // Calculate line total
            BigDecimal lineTotal = detailDto.getUnitPrice()
                    .multiply(BigDecimal.valueOf(detailDto.getQuantity()));
            totalAmount = totalAmount.add(lineTotal);
        }

        // 4. Set total amount
        request.setTotalAmount(totalAmount);
        
        // 5. Save
        DealerRequest savedRequest = dealerRequestRepository.save(request);
        
        log.info("Dealer request created: ID {} - Dealer: {} - Total: {}", 
                savedRequest.getRequestId(), dealer.getDealerName(), totalAmount);
        
        // 6. Convert to response DTO
        return convertToResponseDto(savedRequest);
    }

    // ✅ Method cũ: Giữ lại để backward compatible
    @Transactional
    public DealerRequest createRequest(DealerRequest request) {
        // ✅ Backend tự tạo IDs - Force null
        request.setRequestId(null);
        
        // Validate entities
        Dealer dealer = dealerRepository.findById(request.getDealer().getDealerId())
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));
        
        User user = userRepository.findById(request.getCreatedBy().getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        request.setDealer(dealer);
        request.setCreatedBy(user);
        request.setRequestDate(LocalDateTime.now());

        // ✅ Process request details and calculate total using BigDecimal
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (DealerRequestDetail detail : request.getRequestDetails()) {
            // ✅ Backend tự tạo detail IDs - Force null
            detail.setRequestDetailId(null);
            
            VehicleVariant variant = vehicleVariantRepository.findById(detail.getVehicleVariant().getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle variant not found"));
            
            detail.setVehicleVariant(variant);
            detail.setDealerRequest(request);
            
            // ✅ Calculate line total using BigDecimal
            BigDecimal lineTotal = detail.getUnitPrice()
                    .multiply(BigDecimal.valueOf(detail.getQuantity()));
            totalAmount = totalAmount.add(lineTotal);
        }

        // ✅ Set total amount
        request.setTotalAmount(totalAmount);
        DealerRequest savedRequest = dealerRequestRepository.save(request);
        
        log.info("Dealer request created: ID {} - Dealer: {} - Total: {}", 
                savedRequest.getRequestId(), dealer.getDealerName(), totalAmount);
        
        return savedRequest;
    }
    
    // ✅ Helper: Convert entity to response DTO (chỉ thông tin cần thiết)
    private DealerRequestResponse convertToResponseDto(DealerRequest request) {
        DealerRequestResponse response = new DealerRequestResponse();
        
        response.setRequestId(request.getRequestId());
        
        // Chỉ lấy thông tin tối thiểu
        response.setDealerId(request.getDealer().getDealerId());  // ✅ Thêm dealer_id
        response.setDealerName(request.getDealer().getDealerName());
        response.setUserFullName(request.getCreatedBy().getFullName());
        response.setUserRole(request.getCreatedBy().getRole());
        
        response.setRequestDate(request.getRequestDate());
        response.setRequiredDate(request.getRequiredDate());
        response.setStatus(request.getStatus());
        response.setPriority(request.getPriority());
        response.setNotes(request.getNotes());
        response.setTotalAmount(request.getTotalAmount());
        
        // Request details - chỉ thông tin cần thiết
        List<RequestDetailResponse> detailDtos = request.getRequestDetails().stream()
                .map(detail -> {
                    RequestDetailResponse detailDto = new RequestDetailResponse();
                    detailDto.setRequestDetailId(detail.getRequestDetailId());
                    detailDto.setVariantId(detail.getVehicleVariant().getVariantId());  // ✅ Thêm variant_id
                    detailDto.setVariantName(detail.getVehicleVariant().getName());
                    detailDto.setModelName(detail.getVehicleVariant().getModel() != null ? 
                            detail.getVehicleVariant().getModel().getName() : null);
                    detailDto.setColor(detail.getColor());  // ✅ Thêm màu sắc
                    detailDto.setQuantity(detail.getQuantity());
                    detailDto.setUnitPrice(detail.getUnitPrice());
                    detailDto.setLineTotal(detail.getLineTotal());
                    return detailDto;
                })
                .collect(Collectors.toList());
        
        response.setRequestDetails(detailDtos);
        
        return response;
    }

    /**
     * Cập nhật trạng thái của DealerRequest
     * 
     * ⚠️ QUAN TRỌNG: Khi status = "APPROVED", hệ thống sẽ:
     * 1. Kiểm tra ManufacturerStock có đủ xe không (theo variantId)
     * 2. Nếu không đủ → Throw exception, không approve
     * 3. Nếu đủ → Approve và TRỪ quantity từ ManufacturerStock
     * 
     * Logic tương tự như Vehicle.addVehicle() - kiểm tra stock trước
     * 
     * @param id Request ID cần cập nhật
     * @param status Trạng thái mới (PENDING, APPROVED, REJECTED, SHIPPED, DELIVERED)
     * @param approvedBy Người approve (EVM Staff)
     * @return DealerRequest đã cập nhật
     * @throws IllegalArgumentException nếu không đủ xe trong kho
     */
    @Transactional
    public DealerRequest updateRequestStatus(Long id, String status, String approvedBy) {
        DealerRequest request = getRequestEntityById(id);
        
        // ✅ XỬ LÝ THEO TỪNG STATUS
        if ("APPROVED".equals(status)) {
            // Chỉ kiểm tra có đủ hàng không, KHÔNG trừ kho (allocate sẽ trừ)
            for (DealerRequestDetail detail : request.getRequestDetails()) {
                Long variantId = detail.getVehicleVariant().getVariantId();
                String color = detail.getColor();
                Integer requestedQty = detail.getQuantity();
                
                // Tìm ManufacturerStock theo variant + color
                ManufacturerStock stock = manufacturerStockRepository.findAll().stream()
                        .filter(s -> s.getVariant() != null 
                                && s.getVariant().getVariantId().equals(variantId)
                                && s.getColor().equalsIgnoreCase(color))
                        .findFirst()
                        .orElse(null);
                
                int totalAvailable = (stock != null) ? stock.getQuantity() : 0;
                
                // Kiểm tra đủ hàng không
                if (totalAvailable < requestedQty) {
                    String variantName = detail.getVehicleVariant().getName();
                    throw new IllegalArgumentException(
                        String.format("❌ Không đủ xe trong kho! Variant '%s' - Màu '%s' - Yêu cầu: %d xe, Có sẵn: %d xe", 
                            variantName, color, requestedQty, totalAvailable)
                    );
                }
                
                log.info("✅ Variant {} - Color {} - Requested: {}, Available: {}", variantId, color, requestedQty, totalAvailable);
            }
            
            // Set approved info
            request.setApprovedDate(LocalDateTime.now());
            request.setApprovedBy(approvedBy);
            log.info("✅ Request {} APPROVED - Chờ phân bổ để trừ kho", id);
            
        } else if ("DELIVERED".equals(status)) {
            // ✅ DELIVERED: Tự động cộng xe vào kho đại lý
            addStockToDealerOnDelivery(request);
            request.setDeliveryDate(LocalDateTime.now());
            log.info("✅ Request {} DELIVERED - Đã cộng xe vào kho đại lý", id);
        }
        
        request.setStatus(status);
        DealerRequest updatedRequest = dealerRequestRepository.save(request);
        log.info("Dealer request {} status updated to: {}", id, status);
        
        return updatedRequest;
    }

    @Transactional
    public DealerRequest updateRequest(DealerRequest requestDetails) {
        DealerRequest request = getRequestEntityById(requestDetails.getRequestId());
        
        if (requestDetails.getRequiredDate() != null) {
            request.setRequiredDate(requestDetails.getRequiredDate());
        }
        if (requestDetails.getPriority() != null) {
            request.setPriority(requestDetails.getPriority());
        }
        if (requestDetails.getNotes() != null) {
            request.setNotes(requestDetails.getNotes());
        }
        
        return dealerRequestRepository.save(request);
    }

    public Long countRequestsByDealerAndStatus(Long dealerId, String status) {
        return dealerRequestRepository.countByDealerAndStatus(dealerId, status);
    }

    public Double getTotalSpentByDealer(Long dealerId) {
        Double total = dealerRequestRepository.getTotalSpentByDealer(dealerId);
        return total != null ? total : 0.0;
    }

    public Map<String, Object> getRequestStats(Long dealerId) {
        Map<String, Object> stats = new HashMap<>();
        
        List<Object[]> statusCounts = dealerRequestRepository.getRequestStatsByDealer(dealerId);
        Map<String, Long> statusMap = new HashMap<>();
        
        for (Object[] data : statusCounts) {
            statusMap.put((String) data[0], (Long) data[1]);
        }
        
        stats.put("byStatus", statusMap);
        stats.put("totalRequests", dealerRequestRepository.countByDealerAndStatus(dealerId, "APPROVED"));
        stats.put("totalSpent", getTotalSpentByDealer(dealerId));
        
        return stats;
    }

    @Transactional
    public void deleteRequest(Long id) {
        DealerRequest request = getRequestEntityById(id);
        
        // ✅ KHÔNG xóa VehicleVariant! 
        // Request details sẽ tự động xóa bởi CascadeType.ALL trong DealerRequest entity
        dealerRequestRepository.delete(request);
        
        log.info("Dealer request deleted: {} with {} details", id, request.getRequestDetails().size());
    }
    
    /**
     * Tự động cộng xe vào kho đại lý khi status = DELIVERED
     * Logic: Duyệt qua từng detail, cộng quantity vào InventoryStock
     * Nếu chưa có stock (variant + color + dealer), tạo mới
     */
    private void addStockToDealerOnDelivery(DealerRequest request) {
        Dealer dealer = request.getDealer();
        
        for (DealerRequestDetail detail : request.getRequestDetails()) {
            VehicleVariant variant = detail.getVehicleVariant();
            String color = detail.getColor();
            Integer quantity = detail.getQuantity();
            
            // Tìm hoặc tạo mới InventoryStock
            InventoryStock dealerStock = inventoryStockRepository
                    .findByVariantVariantIdAndColorAndDealerDealerId(
                            variant.getVariantId(),
                            color,
                            dealer.getDealerId()
                    )
                    .orElseGet(() -> {
                        InventoryStock newStock = new InventoryStock();
                        newStock.setVariant(variant);
                        newStock.setDealer(dealer);
                        newStock.setColor(color);
                        newStock.setQuantity(0);
                        newStock.setStatus("In Stock");
                        newStock.setListingPrice(variant.getMsrp());
                        return newStock;
                    });
            
            // Cộng số lượng xe
            dealerStock.setQuantity(dealerStock.getQuantity() + quantity);
            inventoryStockRepository.save(dealerStock);
            
            log.info("📦 Cộng {} xe vào kho đại lý {} (Variant: {}, Color: {}, Stock ID: {})", 
                    quantity, dealer.getDealerName(), variant.getName(), color, dealerStock.getStockId());
        }
    }
}
