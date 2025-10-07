package com.example.evm.controller.admin;

import com.example.evm.dto.admin.CreateUserAccountRequest;
import com.example.evm.dto.admin.CreateUserAccountResponse;
import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.dealer.CreateDealerAccountRequest;
import com.example.evm.dto.dealer.CreateDealerAccountResponse;
import com.example.evm.service.admin.UserAccountService;
import com.example.evm.service.dealer.DealerAccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class DealerAccountController {

    private final DealerAccountService dealerAccountService;
    private final UserAccountService userAccountService;

    public DealerAccountController(DealerAccountService dealerAccountService, UserAccountService userAccountService) {
        this.dealerAccountService = dealerAccountService;
        this.userAccountService = userAccountService;
    }

    /**
     * API tạo dealer account (chỉ admin mới có thể gọi)
     * Tạo đồng thời cả Dealer entity và User account cho dealer
     */
    @PostMapping("/create-dealer-account")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<CreateDealerAccountResponse>> createDealerAccount(
            @Valid @RequestBody CreateDealerAccountRequest request) {

        try {
            CreateDealerAccountResponse response = dealerAccountService.createDealerAccount(request);

            ApiResponse<CreateDealerAccountResponse> apiResponse = new ApiResponse<CreateDealerAccountResponse>();
            apiResponse.setSuccess(true);
            apiResponse.setMessage("Dealer account created successfully");
            apiResponse.setData(response);

            return ResponseEntity.ok(apiResponse);

        } catch (IllegalArgumentException e) {
            ApiResponse<CreateDealerAccountResponse> errorResponse = new ApiResponse<CreateDealerAccountResponse>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            errorResponse.setData(null);

            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            ApiResponse<CreateDealerAccountResponse> errorResponse = new ApiResponse<CreateDealerAccountResponse>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Failed to create dealer account: " + e.getMessage());
            errorResponse.setData(null);

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * API tạo dealer staff account (chỉ admin và EVM staff có thể gọi)
     * Tạo user account với role ROLE_DEALER_STAFF cho dealer đã tồn tại
     */
    @PostMapping("/create-dealer-staff")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EVM_STAFF')")
    public ResponseEntity<ApiResponse<CreateUserAccountResponse>> createDealerStaff(
            @Valid @RequestBody CreateUserAccountRequest request) {

        // Force role to be DEALER_STAFF
        request.setRole("ROLE_DEALER_STAFF");

        try {
            CreateUserAccountResponse response = userAccountService.createUserAccount(request);

            ApiResponse<CreateUserAccountResponse> apiResponse = new ApiResponse<CreateUserAccountResponse>();
            apiResponse.setSuccess(true);
            apiResponse.setMessage("Dealer staff account created successfully");
            apiResponse.setData(response);

            return ResponseEntity.ok(apiResponse);

        } catch (IllegalArgumentException e) {
            ApiResponse<CreateUserAccountResponse> errorResponse = new ApiResponse<CreateUserAccountResponse>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            errorResponse.setData(null);

            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            ApiResponse<CreateUserAccountResponse> errorResponse = new ApiResponse<CreateUserAccountResponse>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Failed to create dealer staff account: " + e.getMessage());
            errorResponse.setData(null);

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * API tạo EVM staff account (chỉ admin có thể gọi)
     * Tạo user account với role ROLE_EVM_STAFF
     */
    @PostMapping("/create-evm-staff")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<CreateUserAccountResponse>> createEvmStaff(
            @Valid @RequestBody CreateUserAccountRequest request) {

        // Force role to be EVM_STAFF and no dealer
        request.setRole("ROLE_EVM_STAFF");
        request.setDealerId(null);

        try {
            CreateUserAccountResponse response = userAccountService.createUserAccount(request);

            ApiResponse<CreateUserAccountResponse> apiResponse = new ApiResponse<CreateUserAccountResponse>();
            apiResponse.setSuccess(true);
            apiResponse.setMessage("EVM staff account created successfully");
            apiResponse.setData(response);

            return ResponseEntity.ok(apiResponse);

        } catch (IllegalArgumentException e) {
            ApiResponse<CreateUserAccountResponse> errorResponse = new ApiResponse<CreateUserAccountResponse>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            errorResponse.setData(null);

            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            ApiResponse<CreateUserAccountResponse> errorResponse = new ApiResponse<CreateUserAccountResponse>();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Failed to create EVM staff account: " + e.getMessage());
            errorResponse.setData(null);

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}