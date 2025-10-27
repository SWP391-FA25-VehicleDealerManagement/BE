package com.example.evm.dto.payment;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PaymentInfo {
private Long orderId;
private BigDecimal amount;
private String paymentMethod;
private String paymentType;

}


