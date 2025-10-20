package com.example.evm.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDto {
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Dealer ID is required")
    private Long dealerId;
    
    private String paymentMethod;
    
    @NotNull(message = "Order details are required")
    private List<OrderDetailRequestDto> orderDetails;
}

