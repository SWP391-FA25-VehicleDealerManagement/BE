package com.example.evm.controller.account;

import com.example.evm.dto.admin.UserAccountInfoResponse;
import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.service.admin.UserAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller quản lý thông tin user accounts
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserAccountController {

    private final UserAccountService userAccountService;

    /**
     * Lấy thông tin user account theo ID
     * GET /api/users/{userId}
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_MANAGER', 'DEALER_STAFF')")
    public ResponseEntity<ApiResponse<UserAccountInfoResponse>> getUserAccount(@PathVariable Long userId) {
        log.info("Fetching user account info for user id: {}", userId);
        
        try {
            UserAccountInfoResponse userInfo = userAccountService.getUserAccountById(userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "User account retrieved successfully", userInfo));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Lấy danh sách tất cả user accounts của dealer
     * GET /api/users/dealer/{dealerId}
     */
    @GetMapping("/dealer/{dealerId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<UserAccountInfoResponse>>> getDealerAccounts(@PathVariable Long dealerId) {
        log.info("Fetching all accounts for dealer id: {}", dealerId);
        
        try {
            List<UserAccountInfoResponse> accounts = userAccountService.getDealerAccounts(dealerId);
            return ResponseEntity.ok(new ApiResponse<>(true, 
                    String.format("Found %d accounts for dealer", accounts.size()), accounts));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Lấy thông tin DEALER_MANAGER của dealer
     * GET /api/users/dealer/{dealerId}/manager
     */
    @GetMapping("/dealer/{dealerId}/manager")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_MANAGER', 'DEALER_STAFF')")
    public ResponseEntity<ApiResponse<UserAccountInfoResponse>> getDealerManager(@PathVariable Long dealerId) {
        log.info("Fetching dealer manager for dealer id: {}", dealerId);
        
        try {
            UserAccountInfoResponse manager = userAccountService.getDealerManager(dealerId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Dealer manager retrieved successfully", manager));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Lấy danh sách DEALER_STAFF của dealer
     * GET /api/users/dealer/{dealerId}/staff
     */
    @GetMapping("/dealer/{dealerId}/staff")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<UserAccountInfoResponse>>> getDealerStaff(@PathVariable Long dealerId) {
        log.info("Fetching dealer staff for dealer id: {}", dealerId);
        
        try {
            List<UserAccountInfoResponse> staff = userAccountService.getDealerStaff(dealerId);
            return ResponseEntity.ok(new ApiResponse<>(true, 
                    String.format("Found %d staff members for dealer", staff.size()), staff));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}

