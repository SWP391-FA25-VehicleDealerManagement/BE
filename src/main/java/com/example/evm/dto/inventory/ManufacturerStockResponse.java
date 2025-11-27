package com.example.evm.dto.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonPropertyOrder({ "id", "variantId", "modelName", "variantName", "color", "quantity", "status" })
@Data
@NoArgsConstructor
public class ManufacturerStockResponse {

    @JsonProperty("manufacturerStockId")
    private Long id;
    private Long variantId;  //  Thêm variant_id
    private Integer quantity;
    private String status;
    private String color;
    private String variantName;
    private String modelName;
}