package com.example.evm.repository.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.evm.entity.payment.Payment;

public interface PaymentRepository extends JpaRepository<Payment,Long>{

}
