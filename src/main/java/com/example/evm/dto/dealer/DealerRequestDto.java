package com.example.evm.dto.dealer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO - Frontend chỉ truyền IDs
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealerRequestDto {
    @NotNull(message = "Dealer ID is required")
    private Long dealerId;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private LocalDateTime requiredDate;
    private String priority; //  Bỏ default value để Jackson có thể deserialize đúng
    private String notes;
    
    @NotEmpty(message = "Request details cannot be empty")
    @Valid
    private List<RequestDetailDto> requestDetails;
}

