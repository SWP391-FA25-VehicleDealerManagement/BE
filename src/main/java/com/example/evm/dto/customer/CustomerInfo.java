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
<<<<<<< HEAD
=======
    private String createBy;
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
}
