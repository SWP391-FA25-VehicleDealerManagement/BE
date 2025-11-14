package com.example.evm.service.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.evm.dto.payment.PaymentInfo;
import com.example.evm.entity.payment.Payment;
import com.example.evm.entity.order.Order;
import com.example.evm.repository.order.OrderRepository;
import com.example.evm.repository.payment.PaymentRepository;
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private OrderRepository orderRepository;

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public Payment getPaymentById(Long id) {
       Optional <Payment> payment = paymentRepository.findById(id);
       return payment.orElse(null);
    }

    @Override
    public Payment createPayment(PaymentInfo paymentInfo) {
        Order order = null;
        if (paymentInfo.getOrderId() != null) {
            order = orderRepository.findById(paymentInfo.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid order id " + paymentInfo.getOrderId()));
        }

        BigDecimal completedAmount = BigDecimal.ZERO;
        if (paymentInfo.getOrderId() != null) {
            completedAmount = paymentRepository.sumCompletedAmountByOrderId(paymentInfo.getOrderId());
            if (completedAmount == null) {
                completedAmount = BigDecimal.ZERO;
            }
        }

        if (order != null && order.getTotalPrice() != null) {
            BigDecimal orderTotal = BigDecimal.valueOf(order.getTotalPrice());
            BigDecimal remaining = orderTotal.subtract(completedAmount);
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Order has already been fully paid.");
            }
            if (paymentInfo.getAmount().compareTo(remaining) > 0) {
                throw new IllegalArgumentException("Payment amount exceeds the remaining balance (" + remaining.toPlainString() + " VND).");
            }
        }
        
        Payment payment = new Payment();
        payment.setOrderId(paymentInfo.getOrderId());
        payment.setAmount(paymentInfo.getAmount());
        payment.setPaymentMethod(paymentInfo.getPaymentMethod());
        payment.setPaymentType(paymentInfo.getPaymentType());
        payment.setPaymentId(null);
        if(payment.getPaymentDate() == null){
                payment.setPaymentDate(LocalDateTime.now());
        }
        // Tất cả thanh toán đều hoàn thành ngay
        payment.setStatus("Completed");
        return paymentRepository.save(payment);
    }

    @Override
    public Payment updatePayment(Payment payment) {
        if (payment.getPaymentId() == null || !paymentRepository.existsById(payment.getPaymentId())){
            throw new IllegalArgumentException("PaymentId not found");
        } 
        if (payment.getOrderId() != null && !orderRepository.existsById(payment.getOrderId())) {
            throw new IllegalArgumentException("Invalid orderId "+payment.getOrderId());
        }
        return paymentRepository.save(payment);
    }

    @Override
    public void deletePayment(Long id) {
      paymentRepository.deleteById(id);
    }

    // Cập nhật trạng thái thanh toán
    public Payment updatePaymentStatus(Long paymentId, String status) {
       Payment payment = paymentRepository.findById(paymentId)
        .orElseThrow(()-> new IllegalArgumentException("Payment with paymentId " + paymentId + " not found"));
        payment.setStatus(status);
        return paymentRepository.save(payment);
    }

    @Override
    public PaymentResponse createPaymentResponse(Payment payment) {
        try {
            payment.setStatus("Completed");
            payment.setPaymentDate(LocalDateTime.now());
            payment.setOrderId(System.currentTimeMillis());
            paymentRepository.save(payment);

            return new PaymentResponse(true, "Create payment successfully", payment);
        } catch (Exception e) {
           return new PaymentResponse(false,"Error: " + e.getMessage(),null);
        }
    }
}


