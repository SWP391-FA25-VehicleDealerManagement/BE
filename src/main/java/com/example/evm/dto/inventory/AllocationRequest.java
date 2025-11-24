package com.example.evm.dto.inventory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllocationRequest {

    @NotNull(message = "Dealer ID is required")
    private Long dealerId;

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    private List<AllocationItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllocationItem {
        
        @NotNull(message = "Variant ID is required")
        private Long variantId;

        @NotBlank(message = "Color is required")
        private String color;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
    }
}