package com.example.evm.dto.contract;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VehicleContractRequest {

    @NotNull(message = "orderDetailId is required")
    private Long orderDetailId;

    private String notes;
}
