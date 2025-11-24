package com.example.evm.dto.inventory;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RecallRequest {

    @NotNull(message = "Dealer ID is required")
    private Long dealerId;
}
