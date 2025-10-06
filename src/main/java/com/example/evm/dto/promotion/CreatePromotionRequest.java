package com.example.evm.dto.promotion;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreatePromotionRequest {
    private Integer dealerId; // null for system-wide
    private String title;
    private String description;
    private Double discountRate;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Integer> vehicleIds; // Vehicles this promotion applies to
    private List<Integer> dealerIds; // Specific dealers (if system-wide but limited)
}

@Data
class PromotionResponse {
    private Integer promoId;
    private String title;
    private String description;
    private Double discountRate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Boolean isActive;
    private String dealerName; // null if system-wide
    private List<String> vehicleNames;
}
