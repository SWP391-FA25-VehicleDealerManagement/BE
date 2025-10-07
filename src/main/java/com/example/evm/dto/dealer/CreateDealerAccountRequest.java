package com.example.evm.dto.dealer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDealerAccountRequest {

    // Thông tin dealer
    @NotBlank(message = "Dealer name is required")
    @Size(max = 255, message = "Dealer name must not exceed 255 characters")
    private String dealerName;

    @Size(max = 50, message = "Phone must not exceed 50 characters")
    private String dealerPhone;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String dealerAddress;

    // Thông tin user account
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Size(max = 255, message = "Full name must not exceed 255 characters")
    private String fullName;

    @Size(max = 50, message = "Phone must not exceed 50 characters")
    private String userPhone;

    @Email(message = "Email should be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @NotBlank(message = "Role is required")
    private String role; // ROLE_DEALER_STAFF hoặc ROLE_DEALER_MANAGER

}