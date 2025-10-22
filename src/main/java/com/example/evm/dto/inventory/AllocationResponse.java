package com.example.evm.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Response cho API phân bổ xe (Allocate Stock)
 * Trả về thông tin kho đại lý + thông tin request đã được update
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllocationResponse {
    
    // Thông tin kho đại lý sau khi phân bổ
    private InventoryResponse dealerStock;
    
    // Thông tin DealerRequest đã được update (nếu có)
    private DealerRequestInfo updatedRequest;
    
    // Message thông báo
    private String message;
    
    /**
     * Nested class chứa thông tin cơ bản của DealerRequest
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DealerRequestInfo {
        private Long requestId;
        private String status;  // "SHIPPED"
        private String shippedDate;  // Thời gian giao hàng
        private String dealerName;
    }
}

