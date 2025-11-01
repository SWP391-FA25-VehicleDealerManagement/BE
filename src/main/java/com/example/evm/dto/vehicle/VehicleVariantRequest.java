package com.example.evm.dto.vehicle;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class VehicleVariantRequest {
    private String name;
    private Long modelId;
    private BigDecimal msrp;
}
