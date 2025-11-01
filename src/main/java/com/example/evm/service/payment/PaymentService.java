package com.example.evm.service.payment;

import java.util.List;

import com.example.evm.dto.payment.PaymentInfo;
import com.example.evm.entity.payment.Payment;

public interface PaymentService {
List<Payment> getAllPayments();

Payment getPaymentById(Long id);

Payment createPayment(PaymentInfo paymentInfo);

Payment updatePayment(Payment payment);

 void deletePayment(Long id);   

 Payment updatePaymentStatus(Long paymentId, String status);

 PaymentResponse createPaymentResponse(Payment payment);

 
} 
