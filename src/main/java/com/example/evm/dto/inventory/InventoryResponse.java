package com.example.evm.dto.inventory;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {
    private Integer stockId;
    private Integer quantity;
    private String status;
    private String vehicleName;
    private String variantName;
    private String color;
    private String dealerName;
}
