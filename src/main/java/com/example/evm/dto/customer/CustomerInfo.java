package com.example.evm.dto.customer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerInfo {
    private Integer customerId;
    private String customerName;
    private String email;
    private String phone;
    private Integer dealerId;
    private String createBy;
}
