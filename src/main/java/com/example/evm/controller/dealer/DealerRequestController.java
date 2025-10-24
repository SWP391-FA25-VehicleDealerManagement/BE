package com.example.evm.controller.dealer;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.dealer.DealerRequestDto;
import com.example.evm.dto.dealer.DealerRequestResponse;
import com.example.evm.service.dealer.DealerRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller quản lý Dealer Request (Yêu cầu đặt hàng của đại lý)
 */
@RestController
@RequestMapping("/api/dealer-requests")
@RequiredArgsConstructor
@Slf4j
public class DealerRequestController {

    private final DealerRequestService dealerRequestService;

    /**
     * Tạo request mới
     * POST /api/dealer-requests
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('DEALER_MANAGER', 'DEALER_STAFF', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<DealerRequestResponse>> createRequest(
            @Valid @RequestBody DealerRequestDto dto) {
        log.info("Creating new dealer request for dealer: {}", dto.getDealerId());
        DealerRequestResponse response = dealerRequestService.createRequest(dto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Request created successfully", response));
    }

    /**
     * Lấy tất cả requests
     * GET /api/dealer-requests
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<List<DealerRequestResponse>>> getAllRequests() {
        log.info("Fetching all dealer requests");
        List<DealerRequestResponse> requests = dealerRequestService.getAllRequests();
        return ResponseEntity.ok(new ApiResponse<>(true, "Requests retrieved successfully", requests));
    }

    /**
     * Lấy request theo ID
     * GET /api/dealer-requests/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_MANAGER', 'DEALER_STAFF')")
    public ResponseEntity<ApiResponse<DealerRequestResponse>> getRequestById(@PathVariable Long id) {
        log.info("Fetching request with id: {}", id);
        DealerRequestResponse response = dealerRequestService.getRequestById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Request retrieved successfully", response));
    }

    /**
     * Lấy requests theo dealer ID
     * GET /api/dealer-requests/dealer/{dealerId}
     */
    @GetMapping("/dealer/{dealerId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_MANAGER', 'DEALER_STAFF')")
    public ResponseEntity<ApiResponse<List<DealerRequestResponse>>> getRequestsByDealerId(
            @PathVariable Long dealerId) {
        log.info("Fetching requests for dealer: {}", dealerId);
        List<DealerRequestResponse> requests = dealerRequestService.getRequestsByDealerId(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dealer requests retrieved successfully", requests));
    }

    /**
     * Lấy requests theo status
     * GET /api/dealer-requests/status/{status}
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<List<DealerRequestResponse>>> getRequestsByStatus(
            @PathVariable String status) {
        log.info("Fetching requests with status: {}", status);
        List<DealerRequestResponse> requests = dealerRequestService.getRequestsByStatus(status);
        return ResponseEntity.ok(new ApiResponse<>(true, "Requests retrieved successfully", requests));
    }

    /**
     * Cập nhật status của request
     * PUT /api/dealer-requests/{id}/status
     * Body: { "status": "APPROVED", "approvedBy": "admin" }
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<DealerRequestResponse>> updateRequestStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {
        
        String status = statusUpdate.get("status");
        String approvedBy = statusUpdate.getOrDefault("approvedBy", "system");
        
        log.info("Updating request {} to status: {}", id, status);
        DealerRequestResponse response = dealerRequestService.updateRequestStatus(id, status, approvedBy);
        return ResponseEntity.ok(new ApiResponse<>(true, "Request status updated successfully", response));
    }

    /**
     * Approve request (shortcut for status update)
     * POST /api/dealer-requests/{id}/approve
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<DealerRequestResponse>> approveRequest(
            @PathVariable Long id,
            @RequestBody Map<String, String> approvalData) {
        
        String approvedBy = approvalData.getOrDefault("approvedBy", "system");
        log.info("Approving request {} by {}", id, approvedBy);
        
        DealerRequestResponse response = dealerRequestService.updateRequestStatus(id, "APPROVED", approvedBy);
        return ResponseEntity.ok(new ApiResponse<>(true, "Request approved successfully", response));
    }

    /**
     * Reject request
     * POST /api/dealer-requests/{id}/reject
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<DealerRequestResponse>> rejectRequest(@PathVariable Long id) {
        log.info("Rejecting request {}", id);
        DealerRequestResponse response = dealerRequestService.updateRequestStatus(id, "REJECTED", "system");
        return ResponseEntity.ok(new ApiResponse<>(true, "Request rejected successfully", response));
    }

    /**
     * Mark request as shipped
     * POST /api/dealer-requests/{id}/ship
     */
    @PostMapping("/{id}/ship")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<DealerRequestResponse>> shipRequest(@PathVariable Long id) {
        log.info("Marking request {} as shipped", id);
        DealerRequestResponse response = dealerRequestService.updateRequestStatus(id, "SHIPPED", "system");
        return ResponseEntity.ok(new ApiResponse<>(true, "Request marked as shipped", response));
    }

    /**
     * Mark request as delivered
     * POST /api/dealer-requests/{id}/deliver
     */
    @PostMapping("/{id}/deliver")
    @PreAuthorize("hasAnyAuthority('DEALER_MANAGER', 'DEALER_STAFF')")
    public ResponseEntity<ApiResponse<DealerRequestResponse>> deliverRequest(@PathVariable Long id) {
        log.info("Marking request {} as delivered", id);
        DealerRequestResponse response = dealerRequestService.updateRequestStatus(id, "DELIVERED", "system");
        return ResponseEntity.ok(new ApiResponse<>(true, "Request marked as delivered", response));
    }

    /**
     * Xóa request (chỉ cho PENDING hoặc REJECTED)
     * DELETE /api/dealer-requests/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteRequest(@PathVariable Long id) {
        log.info("Deleting request {}", id);
        dealerRequestService.deleteRequest(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Request deleted successfully", null));
    }
}

