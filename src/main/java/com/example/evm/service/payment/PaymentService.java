package com.example.evm.service.payment;

import java.util.List;



import com.example.evm.entity.payment.Payment;

public interface PaymentService {
List<Payment> getAllPayments();

Payment getPaymentById(Integer id);

Payment createPayment(Payment payment);

Payment updatePayment(Payment payment);

 void deletePayment(Integer id);   
} 
