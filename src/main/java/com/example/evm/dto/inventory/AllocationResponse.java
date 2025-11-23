package com.example.evm.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO AllocationResponse - Response cho allocate/recall xe (Schema mới)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllocationResponse {
    
    /**
     * Message kết quả
     */
    private String message;
    
    /**
     * Số lượng xe đã xử lý
     */
    private Integer quantity;
    
    /**
     * Danh sách Vehicle IDs đã allocate/recall
     */
    private List<Long> vehicleIds;
    
    /**
     * Dealer ID
     */
    private Long dealerId;
    
    /**
     * Variant ID
     */
    private Long variantId;
    
    /**
     * Màu xe
     */
    private String color;
    
    /**
     * Request ID (nếu có)
     */
    private Long requestId;
}

