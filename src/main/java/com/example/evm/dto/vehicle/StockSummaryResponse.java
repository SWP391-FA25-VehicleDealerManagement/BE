package com.example.evm.dto.vehicle;

import lombok.Builder;
import lombok.Data;

/**
 * DTO StockSummaryResponse - Tổng hợp kho (GROUP BY variant + color)
 * 
 * Sử dụng cho:
 * - Hiển thị tổng số xe trong kho tổng
 * - Hiển thị tổng số xe trong kho dealer
 * 
 * Ví dụ:
 * - VF 8 Eco, Red, quantity: 10
 * - VF 8 Eco, White, quantity: 5
 */
@Data
@Builder
public class StockSummaryResponse {
    private Long variantId;
    private String variantName;
    private String modelName;
    private String color;
    private Integer quantity; // Số lượng xe (COUNT)
}

