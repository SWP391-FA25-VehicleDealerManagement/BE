package com.example.evm.dto.order;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateOrderRequest {
    private Integer customerId;
    private Integer userId;
    private Integer dealerId;
    private String paymentMethod;
    private List<OrderDetailRequest> orderDetails;
}

@Data
class OrderDetailRequest {
    private Integer vehicleId;
    private Integer promotionId;
    private Integer quantity;
    private Double price;
}

@Data
class OrderResponse {
    private Integer orderId;
    private String customerName;
    private String dealerName;
    private String userName;
    private Double totalPrice;
    private String paymentMethod;
    private String status;
    private LocalDateTime createdDate;
    private Integer orderdetailId;
    private List<OrderDetailResponse> orderDetails;
}

@Data
class OrderDetailResponse {
    private String vehicleName;
    private String promotionTitle;
    private Double discountRate;
    private Integer quantity;
    private Double price;
    private Double lineTotal;
}
