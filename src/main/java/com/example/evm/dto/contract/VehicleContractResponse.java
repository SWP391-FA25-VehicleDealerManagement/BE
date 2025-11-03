package com.example.evm.dto.contract;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class VehicleContractResponse {
    private Long contractId;
    private String contractNumber;
    private Long orderId;
    private Long orderDetailId;
    private Long dealerId;
    private String dealerName;
    private Long customerId;
    private String customerName;
    private Long vehicleId;
    private String vinNumber;
    private String modelName;
    private String variantName;
    private String color;  
    private BigDecimal salePrice;
    private String paymentMethod;
    private LocalDate contractDate;
    private String status;
    private String notes;
    private String fileUrl;
}
