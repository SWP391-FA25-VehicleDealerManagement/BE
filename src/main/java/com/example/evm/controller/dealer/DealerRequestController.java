package com.example.evm.controller.dealer;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.dealer.DealerRequestDto;
import com.example.evm.dto.dealer.DealerRequestResponse;
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

import com.example.evm.entity.dealer.DealerRequestDetail;

@RestController
@RequestMapping("/api/dealer-requests")
@RequiredArgsConstructor
@Slf4j
public class DealerRequestController {
    private final DealerRequestService dealerRequestService;

    // ==================== CREATE OPERATIONS ====================
    
    /**
     * ✅ Tạo request - chỉ cần truyền IDs
     * Frontend: dealerId, userId, variantId, quantity, unitPrice
     * Backend: Tự lookup và trả về fullName, role, dealerName
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
     * ⚠️ API cũ: Deprecated - Dùng cho backward compatibility
     */
    @PostMapping("/full")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    @Deprecated
    public ResponseEntity<ApiResponse<DealerRequest>> createRequestFull(
            @Valid @RequestBody DealerRequest request) {
        try {
            DealerRequest createdRequest = dealerRequestService.createRequest(request);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, 
                    "Dealer request created successfully", 
                    createdRequest
            ));
        } catch (Exception e) {
            log.error("Error creating dealer request", e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false, 
                    "Failed to create dealer request: " + e.getMessage(), 
                    null
            ));
        }
    }

    // ==================== READ OPERATIONS ====================
    
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<DealerRequest>>> getAllRequests() {
        List<DealerRequest> requests = dealerRequestService.getAllRequests();
        return ResponseEntity.ok(new ApiResponse<>(
                true, 
                "All dealer requests retrieved successfully", 
                requests
        ));
    }

    @GetMapping("/dealer/{dealerId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<DealerRequest>>> getRequestsByDealer(
            @PathVariable Long dealerId) {
        List<DealerRequest> requests = dealerRequestService.getRequestsByDealer(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(
                true, 
                "Dealer requests retrieved successfully", 
                requests
        ));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<DealerRequest>>> getRequestsByUser(
            @PathVariable Long userId) {
        List<DealerRequest> requests = dealerRequestService.getRequestsByUser(userId);
        return ResponseEntity.ok(new ApiResponse<>(
                true, 
                "User requests retrieved successfully", 
                requests
        ));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<DealerRequest>>> getRequestsByStatus(
            @PathVariable String status) {
        List<DealerRequest> requests = dealerRequestService.getRequestsByStatus(status);
        return ResponseEntity.ok(new ApiResponse<>(
                true, 
                "Requests by status retrieved successfully", 
                requests
        ));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<DealerRequest>>> getPendingRequests() {
        List<DealerRequest> requests = dealerRequestService.getPendingRequests();
        return ResponseEntity.ok(new ApiResponse<>(
                true, 
                "Pending requests retrieved successfully", 
                requests
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<DealerRequest>> getRequestById(@PathVariable Long id) {
        DealerRequest request = dealerRequestService.getRequestById(id);
        return ResponseEntity.ok(new ApiResponse<>(
                true, 
                "Dealer request retrieved successfully", 
                request
        ));
    }

    @GetMapping("/{id}/details")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<DealerRequestDetail>>> getRequestDetails(
            @PathVariable Long id) {
        List<DealerRequestDetail> details = dealerRequestService.getRequestDetails(id);
        return ResponseEntity.ok(new ApiResponse<>(
                true, 
                "Request details retrieved successfully", 
                details
        ));
    }

    @GetMapping("/dealer/{dealerId}/date-range")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<DealerRequest>>> getRequestsByDateRange(
            @PathVariable Long dealerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        // Implementation would go here
        return ResponseEntity.ok(new ApiResponse<>(true, "Not implemented", null));
    }

    // ==================== UPDATE OPERATIONS ====================
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<DealerRequest>> updateRequestStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String approvedBy) {
        DealerRequest updatedRequest = dealerRequestService.updateRequestStatus(id, status, approvedBy);
        return ResponseEntity.ok(new ApiResponse<>(
                true, 
                "Request status updated successfully", 
                updatedRequest
        ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<DealerRequest>> updateRequest(
            @PathVariable Long id,
            @Valid @RequestBody DealerRequest requestDetails) {
        requestDetails.setRequestId(id);
        DealerRequest updatedRequest = dealerRequestService.updateRequest(requestDetails);
        return ResponseEntity.ok(new ApiResponse<>(
                true, 
                "Request updated successfully", 
                updatedRequest
        ));
    }

    // ==================== DELETE OPERATIONS ====================
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteRequest(@PathVariable Long id) {
        dealerRequestService.deleteRequest(id);
        return ResponseEntity.ok(new ApiResponse<>(
                true, 
                "Request deleted successfully", 
                null
        ));
    }

    // ==================== STATISTICS ====================
    
    @GetMapping("/dealer/{dealerId}/stats")
        @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRequestStats(
            @PathVariable Long dealerId) {
        Map<String, Object> stats = dealerRequestService.getRequestStats(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(
                true, 
                "Request statistics retrieved successfully", 
                stats
        ));
    }

    @GetMapping("/dealer/{dealerId}/count")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Long>> countRequestsByStatus(
            @PathVariable Long dealerId,
            @RequestParam String status) {
        Long count = dealerRequestService.countRequestsByDealerAndStatus(dealerId, status);
        return ResponseEntity.ok(new ApiResponse<>(
                true, 
                "Request count retrieved successfully", 
                count
        ));
    }

    @GetMapping("/dealer/{dealerId}/total-spent")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Double>> getTotalSpent(
            @PathVariable Long dealerId) {
        Double total = dealerRequestService.getTotalSpentByDealer(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(
                true, 
                "Total spent retrieved successfully", 
                total
        ));
    }
}
