package com.example.evm.service.payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.evm.entity.payment.Payment;
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
    public Payment getPaymentById(Integer id) {
       Optional <Payment> payment = paymentRepository.findById(id);
       return payment.orElse(null);
    }

    @Override
    public Payment createPayment(Payment payment) {
        if(payment.getPaymentDate() == null){
                payment.setPaymentDate(LocalDateTime.now());
        }
        if(payment.getOrderId()!=null && !orderRepository.existsById(payment.getOrderId())){
            throw new IllegalArgumentException("Invalid order id "+payment.getOrderId());
        }
        return paymentRepository.save(payment);
    }

    @Override
    public Payment updatePayment(Payment payment) {
        if (payment.getPaymentId() == null || !paymentRepository.existsById(payment.getPaymentId())){
            throw new IllegalArgumentException("PaymentId not found");
        } if (payment.getOrderId() != null && orderRepository.existsById(payment.getOrderId())) {
            throw new IllegalArgumentException("Invalid orderId "+payment.getOrderId());
        }
        return paymentRepository.save(payment);
    }

    @Override
    public void deletePayment(Integer id) {
      paymentRepository.deleteById(id);
    }

}
