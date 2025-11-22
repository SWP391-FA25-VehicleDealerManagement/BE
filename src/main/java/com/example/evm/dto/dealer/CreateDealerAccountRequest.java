package com.example.evm.dto.dealer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO để tạo tài khoản cho dealer đã tồn tại
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDealerAccountRequest {

    // ID của dealer đã tồn tại
    @NotNull(message = "Dealer ID is required")
    private Long dealerId;

    // Thông tin user account
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    @NotBlank(message = "Email is required")
    private String email;
}