package com.example.evm.dto.testDrive;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO để tạo test drive mới
    * KHÔNG bao gồm testDriveId - để database tự động tạo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTestDriveRequest {
    
    @NotNull(message = "Dealer ID is required")
    private Long dealerId;
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;
    
    @NotNull(message = "Scheduled date is required")
    private LocalDateTime scheduledDate;
    
    private String notes;
    
    private String assignedBy;
}

