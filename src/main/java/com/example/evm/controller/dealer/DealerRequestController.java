package com.example.evm.controller.dealer;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.dealer.DealerRequestDto;
import com.example.evm.dto.dealer.DealerRequestResponse;
import com.example.evm.dto.dealer.RequestDetailResponse;
import com.example.evm.entity.dealer.DealerRequest;
import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.service.dealer.DealerRequestService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/dealer-requests")
@RequiredArgsConstructor
@Slf4j
public class DealerRequestController {

    private final DealerRequestService dealerRequestService;

    // ==================== CREATE ====================

    /**
     * [POST] /api/dealer-requests
     * ➤ Tạo yêu cầu mới của đại lý (DealerRequest)
     * Input: DealerRequestDto (dealerId, userId, variantId, quantity, unitPrice)
     * Output: DealerRequestResponse chứa thông tin request đã tạo
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<DealerRequestResponse>> createRequest(
            @Valid @RequestBody DealerRequestDto dto) {
        try {
            DealerRequestResponse response = dealerRequestService.createRequestFromDto(dto);
            return ResponseEntity.ok(new ApiResponse<>(true, "Request created successfully", response));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error creating dealer request", e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to create request", null));
        }
    }

    /**
     * [POST] /api/dealer-requests/full
     * ➤ Tạo yêu cầu bằng dữ liệu đầy đủ (legacy API – vẫn hỗ trợ)
     * Input: DealerRequest (entity đầy đủ)
     * Output: DealerRequest đã tạo
     */
    @PostMapping("/full")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    @Deprecated
    public ResponseEntity<ApiResponse<DealerRequest>> createRequestFull(
            @Valid @RequestBody DealerRequest request) {
        try {
            DealerRequest createdRequest = dealerRequestService.createRequest(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Dealer request created successfully", createdRequest));
        } catch (Exception e) {
            log.error("Error creating dealer request", e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to create dealer request: " + e.getMessage(), null));
        }
    }

    // ==================== READ ====================

    /**
     * [GET] /api/dealer-requests
     * ➤ Lấy tất cả yêu cầu (cho admin & staff)
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<DealerRequestResponse>>> getAllRequests() {
        List<DealerRequestResponse> requests = dealerRequestService.getAllRequests();
        return ResponseEntity.ok(new ApiResponse<>(true, "Requests retrieved successfully", requests));
    }

    /**
     * [GET] /api/dealer-requests/dealer/{dealerId}
     * ➤ Lấy danh sách yêu cầu theo ID đại lý
     */
    @GetMapping("/dealer/{dealerId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<DealerRequestResponse>>> getRequestsByDealer(
            @PathVariable Long dealerId) {
        List<DealerRequestResponse> requests = dealerRequestService.getRequestsByDealer(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Requests retrieved successfully", requests));
    }

    /**
     * [GET] /api/dealer-requests/user/{userId}
     * ➤ Lấy danh sách yêu cầu theo ID người dùng
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<DealerRequestResponse>>> getRequestsByUser(
            @PathVariable Long userId) {
        List<DealerRequestResponse> requests = dealerRequestService.getRequestsByUser(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Requests retrieved successfully", requests));
    }

    /**
     * [GET] /api/dealer-requests/status/{status}
     * ➤ Lọc yêu cầu theo trạng thái (e.g. PENDING, APPROVED, REJECTED)
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<DealerRequestResponse>>> getRequestsByStatus(
            @PathVariable String status) {
        List<DealerRequestResponse> requests = dealerRequestService.getRequestsByStatus(status);
        return ResponseEntity.ok(new ApiResponse<>(true, "Requests retrieved successfully", requests));
    }

    /**
     * [GET] /api/dealer-requests/pending
     * ➤ Lấy các yêu cầu đang ở trạng thái “Pending”
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<DealerRequestResponse>>> getPendingRequests() {
        List<DealerRequestResponse> requests = dealerRequestService.getPendingRequests();
        return ResponseEntity.ok(new ApiResponse<>(true, "Requests retrieved successfully", requests));
    }

    /**
     * [GET] /api/dealer-requests/{id}
     * ➤ Lấy thông tin chi tiết một yêu cầu theo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<DealerRequestResponse>> getRequestById(@PathVariable Long id) {
        DealerRequestResponse request = dealerRequestService.getRequestById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Request retrieved successfully", request));
    }

    /**
     * [GET] /api/dealer-requests/{id}/details
     * ➤ Lấy danh sách chi tiết sản phẩm trong yêu cầu
     */
    @GetMapping("/{id}/details")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<RequestDetailResponse>>> getRequestDetails(
            @PathVariable Long id) {
        List<RequestDetailResponse> details = dealerRequestService.getRequestDetails(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Details retrieved successfully", details));
    }

    /**
     * [GET] /api/dealer-requests/dealer/{dealerId}/date-range
     * ➤ Lọc các yêu cầu theo khoảng thời gian (chưa implement)
     */
    @GetMapping("/dealer/{dealerId}/date-range")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<DealerRequest>>> getRequestsByDateRange(
            @PathVariable Long dealerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Not implemented", null));
    }

    // ==================== UPDATE ====================

    /**
     * [PUT] /api/dealer-requests/{id}/status
     * ➤ Cập nhật trạng thái của yêu cầu (VD: từ “PENDING” sang “APPROVED”)
     * Input: status, approvedBy (optional)
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<DealerRequest>> updateRequestStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String approvedBy) {
        DealerRequest updatedRequest = dealerRequestService.updateRequestStatus(id, status, approvedBy);
        return ResponseEntity.ok(new ApiResponse<>(true, "Request status updated successfully", updatedRequest));
    }

    /**
     * [PUT] /api/dealer-requests/{id}
     * ➤ Cập nhật thông tin của yêu cầu (VD: quantity, variant, v.v.)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<DealerRequest>> updateRequest(
            @PathVariable Long id,
            @Valid @RequestBody DealerRequest requestDetails) {
        requestDetails.setRequestId(id);
        DealerRequest updatedRequest = dealerRequestService.updateRequest(requestDetails);
        return ResponseEntity.ok(new ApiResponse<>(true, "Request updated successfully", updatedRequest));
    }

    // ==================== DELETE ====================

    /**
     * [DELETE] /api/dealer-requests/{id}
     * ➤ Xóa yêu cầu theo ID
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteRequest(@PathVariable Long id) {
        dealerRequestService.deleteRequest(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Request deleted successfully", null));
    }

    // ==================== STATISTICS ====================

    /**
     * [GET] /api/dealer-requests/dealer/{dealerId}/stats
     * ➤ Lấy thống kê tổng quan về yêu cầu của 1 đại lý (VD: tổng số, số pending, số approved,...)
     */
    @GetMapping("/dealer/{dealerId}/stats")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRequestStats(
            @PathVariable Long dealerId) {
        Map<String, Object> stats = dealerRequestService.getRequestStats(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Request statistics retrieved successfully", stats));
    }

    /**
     * [GET] /api/dealer-requests/dealer/{dealerId}/count?status={status}
     * ➤ Đếm số lượng yêu cầu của đại lý theo trạng thái
     */
    @GetMapping("/dealer/{dealerId}/count")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Long>> countRequestsByStatus(
            @PathVariable Long dealerId,
            @RequestParam String status) {
        Long count = dealerRequestService.countRequestsByDealerAndStatus(dealerId, status);
        return ResponseEntity.ok(new ApiResponse<>(true, "Request count retrieved successfully", count));
    }

    /**
     * [GET] /api/dealer-requests/dealer/{dealerId}/total-spent
     * ➤ Lấy tổng chi tiêu (totalSpent) của 1 đại lý dựa trên tất cả request đã được duyệt
     */
    @GetMapping("/dealer/{dealerId}/total-spent")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Double>> getTotalSpent(
            @PathVariable Long dealerId) {
        Double total = dealerRequestService.getTotalSpentByDealer(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Total spent retrieved successfully", total));
    }
}
