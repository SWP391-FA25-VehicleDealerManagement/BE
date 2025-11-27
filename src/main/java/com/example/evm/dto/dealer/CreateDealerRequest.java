package com.example.evm.dto.dealer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để tạo dealer mới
 *  KHÔNG bao gồm dealerId - để database tự động tạo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDealerRequest {
    
    @NotBlank(message = "Dealer name is required")
    @Size(max = 255, message = "Dealer name must not exceed 255 characters")
    private String dealerName;
    
    @Size(max = 50, message = "Phone must not exceed 50 characters")
    private String phone;
    
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;
    
    @Size(max = 100, message = "Created by must not exceed 100 characters")
    private String createdBy;
}

