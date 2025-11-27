package com.example.evm.dto.dealer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestDetailResponse {
    private Long detailId;
    private Long variantId;  //  Thêm variant_id
    private String variantName;
    private String modelName;
    private String color;  //  Thêm màu sắc
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}

