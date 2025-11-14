package com.example.evm.repository.payment;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.evm.entity.payment.Payment;

public interface PaymentRepository extends JpaRepository<Payment,Long>{
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.orderId = :orderId AND LOWER(p.status) = 'completed'")
    BigDecimal sumCompletedAmountByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.orderId = :orderId AND LOWER(p.status) = 'pending'")
    BigDecimal sumPendingAmountByOrderId(@Param("orderId") Long orderId);

    List<Payment> findAllByOrderId(Long orderId);
}
