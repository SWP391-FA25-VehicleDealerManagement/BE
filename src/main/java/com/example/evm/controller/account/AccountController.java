package com.example.evm.controller.account;

import com.example.evm.dto.admin.CreateUserAccountRequest;
import com.example.evm.dto.admin.CreateUserAccountResponse;
import com.example.evm.dto.admin.UpdateUserRequest;
import com.example.evm.dto.admin.UpdateUserResponse;
import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.dealer.CreateDealerAccountRequest;
import com.example.evm.dto.dealer.CreateDealerAccountResponse;
import com.example.evm.service.admin.AccountManagementService;
import com.example.evm.service.admin.UserAccountService;
import com.example.evm.service.dealer.DealerAccountService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AccountController {

    private final DealerAccountService dealerAccountService;
    private final UserAccountService userAccountService;
     @Autowired
    private AccountManagementService accountManagementService;

    public AccountController(DealerAccountService dealerAccountService, UserAccountService userAccountService) {
        this.dealerAccountService = dealerAccountService;
        this.userAccountService = userAccountService;
    }

    /**
     * API tạo tài khoản DEALER_MANAGER cho dealer
     *  ADMIN và EVM_STAFF có thể gọi
     *  1 dealer chỉ có thể có 1 DEALER_MANAGER
     *  Response bao gồm createdBy, createdDate, userCreatedDate
     */
    @PostMapping("create-dealer-account")
    @PreAuthorize("hasAnyAuthority('EVM_STAFF')")
    public ResponseEntity<ApiResponse<CreateDealerAccountResponse>> createDealerAccount(
            @Valid @RequestBody CreateDealerAccountRequest request) {

        try {
            CreateDealerAccountResponse response = dealerAccountService.createDealerAccount(request);

            // Check if the operation was successful
            if (!response.isSuccess()) {
                ApiResponse<CreateDealerAccountResponse> errorResponse = new ApiResponse<>();
                errorResponse.setSuccess(false);
                errorResponse.setMessage(response.getMessage());
                errorResponse.setData(response);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            ApiResponse<CreateDealerAccountResponse> apiResponse = new ApiResponse<>();
            apiResponse.setSuccess(true);
            apiResponse.setMessage(response.getMessage());
            apiResponse.setData(response);

            return ResponseEntity.ok(apiResponse);

        } catch (IllegalArgumentException e) {
            ApiResponse<CreateDealerAccountResponse> errorResponse = new ApiResponse<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            errorResponse.setData(null);

            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            ApiResponse<CreateDealerAccountResponse> errorResponse = new ApiResponse<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Failed to create dealer account: " + e.getMessage());
            errorResponse.setData(null);

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * API tạo dealer staff account (chỉ admin và EVM staff có thể gọi)
     * Tạo user account với role DEALER_STAFF cho dealer đã tồn tại
     */
    @PostMapping("/create-dealer-staff")
    @PreAuthorize("hasAnyAuthority('DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<CreateUserAccountResponse>> createDealerStaff(
            @Valid @RequestBody CreateUserAccountRequest request) {

        // Force role to be DEALER_STAFF
        request.setRole("DEALER_STAFF");

        try {
            CreateUserAccountResponse response = userAccountService.createUserAccount(request);

            ApiResponse<CreateUserAccountResponse> apiResponse = new ApiResponse<>();
            apiResponse.setSuccess(true);
            apiResponse.setMessage("Dealer staff account created successfully");
            apiResponse.setData(response);

            return ResponseEntity.ok(apiResponse);

        } catch (IllegalArgumentException e) {
            ApiResponse<CreateUserAccountResponse> errorResponse = new ApiResponse<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            errorResponse.setData(null);

            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            ApiResponse<CreateUserAccountResponse> errorResponse = new ApiResponse<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Failed to create dealer staff account: " + e.getMessage());
            errorResponse.setData(null);

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * API tạo EVM staff account (chỉ admin và EVM staff có thể gọi)
     * Tạo user account với role EVM_STAFF
     */
    @PostMapping("/create-evm-staff")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<CreateUserAccountResponse>> createEvmStaff(
            @Valid @RequestBody CreateUserAccountRequest request) {

        // Force role to be EVM_STAFF and no dealer
        request.setRole("EVM_STAFF");
        request.setDealerId(null);

        try {
            CreateUserAccountResponse response = userAccountService.createUserAccount(request);

            ApiResponse<CreateUserAccountResponse> apiResponse = new ApiResponse<>();
            apiResponse.setSuccess(true);
            apiResponse.setMessage("EVM staff account created successfully");
            apiResponse.setData(response);

            return ResponseEntity.ok(apiResponse);

        } catch (IllegalArgumentException e) {
            ApiResponse<CreateUserAccountResponse> errorResponse = new ApiResponse<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            errorResponse.setData(null);

            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            ApiResponse<CreateUserAccountResponse> errorResponse = new ApiResponse<>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Failed to create EVM staff account: " + e.getMessage());
            errorResponse.setData(null);

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    @PutMapping("/evm-staff/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "ADMIN: Cập nhật EVM_STAFF")
    public ResponseEntity<ApiResponse<UpdateUserResponse>> updateEvmStaff(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication auth) {
        return handleUpdate(id, request, auth, "EVM_STAFF", "EVM_STAFF updated");
    }

    @PutMapping("/dealer-manager/{id}")
    @PreAuthorize("hasAuthority('EVM_STAFF')")
    @Operation(summary = "EVM_STAFF: Cập nhật DEALER_MANAGER")
    public ResponseEntity<ApiResponse<UpdateUserResponse>> updateDealerManager(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication auth) {
        return handleUpdate(id, request, auth, "DEALER_MANAGER", "DEALER_MANAGER updated");
    }

    @PutMapping("/dealer-staff/{id}")
    @PreAuthorize("hasAuthority('DEALER_MANAGER')")
    @Operation(summary = "DEALER_MANAGER: Cập nhật DEALER_STAFF")
    public ResponseEntity<ApiResponse<UpdateUserResponse>> updateDealerStaff(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication auth) {
        return handleUpdate(id, request, auth, "DEALER_STAFF", "DEALER_STAFF updated");
    }

    // ===================================================================
    // DELETE
    // ===================================================================

    @DeleteMapping("/evm-staff/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteEvmStaff(@PathVariable Long id, Authentication auth) {
        return handleDelete(id, auth, "EVM_STAFF", "EVM_STAFF deleted");
    }

    @DeleteMapping("/dealer-manager/{id}")
    @PreAuthorize("hasAuthority('EVM_STAFF')")
    public ResponseEntity<ApiResponse<String>> deleteDealerManager(@PathVariable Long id, Authentication auth) {
        return handleDelete(id, auth, "DEALER_MANAGER", "DEALER_MANAGER deleted");
    }

    @DeleteMapping("/dealer-staff/{id}")
    @PreAuthorize("hasAuthority('DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<String>> deleteDealerStaff(@PathVariable Long id, Authentication auth) {
        return handleDelete(id, auth, "DEALER_STAFF", "DEALER_STAFF deleted");
    }

    // ===================================================================
    // HELPER
    // ===================================================================

    private ResponseEntity<ApiResponse<UpdateUserResponse>> handleUpdate(
            Long userId, UpdateUserRequest request, Authentication auth, String expectedRole, String successMsg) {
        try {
            UpdateUserResponse res = accountManagementService.updateUser(userId,request, auth);
            return ok(successMsg, res);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    private ResponseEntity<ApiResponse<String>> handleDelete(
            Long userId, Authentication auth, String expectedRole, String successMsg) {
        try {
            accountManagementService.deleteUser(userId, auth);
            return ok(successMsg, null);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(String msg, T data) {
        return ResponseEntity.ok(new ApiResponse<>(true, msg, data));
    }

    private <T> ResponseEntity<ApiResponse<T>> error(String msg) {
        if (msg.contains("permission")) return ResponseEntity.status(403).body(new ApiResponse<>(false, msg, null));
        if (msg.contains("not found")) return ResponseEntity.status(404).body(new ApiResponse<>(false, msg, null));
        return ResponseEntity.internalServerError().body(new ApiResponse<>(false, msg, null));
    }
}