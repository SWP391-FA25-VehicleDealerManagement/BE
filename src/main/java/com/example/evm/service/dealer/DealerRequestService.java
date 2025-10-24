package com.example.evm.service.dealer;

import com.example.evm.dto.dealer.DealerRequestDto;
import com.example.evm.dto.dealer.DealerRequestResponse;
import com.example.evm.dto.dealer.RequestDetailDto;
import com.example.evm.dto.dealer.RequestDetailResponse;
import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.dealer.DealerRequest;
import com.example.evm.entity.dealer.DealerRequestDetail;
import com.example.evm.entity.inventory.InventoryStock;
import com.example.evm.entity.user.User;
import com.example.evm.entity.vehicle.VehicleVariant;
import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.repository.dealer.DealerRepository;
import com.example.evm.repository.dealer.DealerRequestRepository;
import com.example.evm.repository.auth.UserRepository;
import com.example.evm.repository.vehicle.VehicleVariantRepository;
import com.example.evm.repository.inventory.InventoryStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DealerRequestService {

    private final DealerRequestRepository dealerRequestRepository;
    private final DealerRepository dealerRepository;
    private final UserRepository userRepository;
    private final VehicleVariantRepository variantRepository;
    private final InventoryStockRepository inventoryStockRepository;

    /**
     * Tạo request mới
     */
    @Transactional
    public DealerRequestResponse createRequest(DealerRequestDto dto) {
        log.info("Creating dealer request for dealer: {}", dto.getDealerId());

        // Validate dealer
        Dealer dealer = dealerRepository.findById(dto.getDealerId())
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found with id: " + dto.getDealerId()));

        // Validate user
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));

        // Create request
        DealerRequest request = new DealerRequest();
        request.setDealer(dealer);
        request.setCreatedBy(user);
        request.setRequestDate(LocalDateTime.now());
        request.setRequiredDate(dto.getRequiredDate());
        request.setPriority(dto.getPriority() != null ? dto.getPriority() : "NORMAL");
        request.setNotes(dto.getNotes());
        request.setStatus("PENDING");

        // Add details
        for (RequestDetailDto detailDto : dto.getRequestDetails()) {
            VehicleVariant variant = variantRepository.findById(detailDto.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + detailDto.getVariantId()));

            DealerRequestDetail detail = new DealerRequestDetail();
            detail.setDealerRequest(request);
            detail.setVehicleVariant(variant);
            detail.setColor(detailDto.getColor());
            detail.setQuantity(detailDto.getQuantity());
            detail.setUnitPrice(detailDto.getUnitPrice());
            detail.setNotes(detailDto.getNotes());

            request.addRequestDetail(detail);
        }

        // Calculate total
        request.setTotalAmount(request.calculateTotalAmount());

        DealerRequest savedRequest = dealerRequestRepository.save(request);
        log.info("Created request with ID: {}", savedRequest.getRequestId());

        return convertToResponseDto(savedRequest);
    }

    /**
     * Lấy tất cả requests
     */
    @Transactional(readOnly = true)
    public List<DealerRequestResponse> getAllRequests() {
        return dealerRequestRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy request theo ID
     */
    @Transactional(readOnly = true)
    public DealerRequestResponse getRequestById(Long id) {
        DealerRequest request = dealerRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));
        return convertToResponseDto(request);
    }

    /**
     * Lấy requests theo dealer ID
     */
    @Transactional(readOnly = true)
    public List<DealerRequestResponse> getRequestsByDealerId(Long dealerId) {
        return dealerRequestRepository.findByDealerDealerId(dealerId).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy requests theo status
     */
    @Transactional(readOnly = true)
    public List<DealerRequestResponse> getRequestsByStatus(String status) {
        return dealerRequestRepository.findByStatus(status).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật status của request
     */
    @Transactional
    public DealerRequestResponse updateRequestStatus(Long id, String status, String approvedBy) {
        log.info("Updating request {} to status: {}", id, status);

        DealerRequest request = dealerRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));

        String oldStatus = request.getStatus();
        request.setStatus(status);

        if ("APPROVED".equals(status)) {
            request.setApprovedDate(LocalDateTime.now());
            request.setApprovedBy(approvedBy);
            log.info("Request {} APPROVED by {}", id, approvedBy);
        } else if ("SHIPPED".equals(status)) {
            request.setShippedDate(LocalDateTime.now());
            log.info("Request {} SHIPPED", id);
        } else if ("DELIVERED".equals(status)) {
            request.setDeliveryDate(LocalDateTime.now());
            // Add vehicles to dealer stock
            addStockToDealerOnDelivery(request);
            log.info("Request {} DELIVERED - Added to dealer stock", id);
        }

        DealerRequest updated = dealerRequestRepository.save(request);
        log.info("Updated request status from {} to {}", oldStatus, status);

        return convertToResponseDto(updated);
    }

    /**
     * Thêm xe vào kho dealer khi giao hàng
     */
    private void addStockToDealerOnDelivery(DealerRequest request) {
        // Lấy hoặc tạo InventoryStock cho dealer
        inventoryStockRepository.findByDealerDealerId(request.getDealer().getDealerId())
                .orElseGet(() -> {
                    InventoryStock newStock = InventoryStock.builder()
                            .dealer(request.getDealer())
                            .status("ACTIVE")
                            .build();
                    return inventoryStockRepository.save(newStock);
                });

        log.info("✅ Dealer {} stock prepared for delivery", request.getDealer().getDealerId());
        
        // Note: Với schema mới (Vehicle-centric), việc cộng xe vào kho
        // sẽ được xử lý bởi InventoryService khi allocate vehicles
        // Ở đây chỉ đảm bảo dealer có warehouse
    }

    /**
     * Xóa request
     */
    @Transactional
    public void deleteRequest(Long id) {
        DealerRequest request = dealerRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));

        if (!"PENDING".equals(request.getStatus()) && !"REJECTED".equals(request.getStatus())) {
            throw new IllegalStateException("Cannot delete request with status: " + request.getStatus());
        }

        dealerRequestRepository.delete(request);
        log.info("Deleted request with ID: {}", id);
    }

    /**
     * Convert entity to response DTO
     */
    private DealerRequestResponse convertToResponseDto(DealerRequest request) {
        DealerRequestResponse response = new DealerRequestResponse();
        response.setRequestId(request.getRequestId());
        
        // Dealer & User info
        response.setDealerId(request.getDealer().getDealerId());
        response.setDealerName(request.getDealer().getDealerName());
        response.setUserId(request.getCreatedBy().getUserId());
        response.setUserFullName(request.getCreatedBy().getFullName());
        response.setUserRole(request.getCreatedBy().getRole());
        
        // Request info
        response.setRequestDate(request.getRequestDate());
        response.setRequiredDate(request.getRequiredDate());
        response.setStatus(request.getStatus());
        response.setPriority(request.getPriority());
        response.setNotes(request.getNotes());
        response.setTotalAmount(request.getTotalAmount());
        
        // Workflow tracking
        response.setApprovedDate(request.getApprovedDate());
        response.setApprovedBy(request.getApprovedBy());
        response.setShippedDate(request.getShippedDate());
        response.setDeliveryDate(request.getDeliveryDate());
        
        // Details
        response.setRequestDetails(getRequestDetails(request));
        
        return response;
    }

    /**
     * Convert request details to response DTOs
     */
    private List<RequestDetailResponse> getRequestDetails(DealerRequest request) {
        return request.getRequestDetails().stream()
                .map(detail -> {
                    RequestDetailResponse detailResponse = new RequestDetailResponse();
                    detailResponse.setDetailId(detail.getDetailId());
                    detailResponse.setVariantId(detail.getVehicleVariant().getVariantId());
                    detailResponse.setVariantName(detail.getVehicleVariant().getName());
                    detailResponse.setModelName(detail.getVehicleVariant().getModel().getName());
                    detailResponse.setColor(detail.getColor());
                    detailResponse.setQuantity(detail.getQuantity());
                    detailResponse.setUnitPrice(detail.getUnitPrice());
                    detailResponse.setLineTotal(detail.getLineTotal());
                    return detailResponse;
                })
                .collect(Collectors.toList());
    }
}

