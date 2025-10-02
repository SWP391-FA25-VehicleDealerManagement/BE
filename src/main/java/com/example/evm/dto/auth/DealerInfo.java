package com.example.evm.dto.auth;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DealerInfo {
    private Integer dealerId;
    private String dealerName;
    private String phone;
    private String address;
    private String createdBy;
    private LocalDateTime createdDate;
}
