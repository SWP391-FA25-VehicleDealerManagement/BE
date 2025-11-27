package com.example.evm.service.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.evm.dto.payment.PaymentInfo;
import com.example.evm.entity.payment.Payment;
import com.example.evm.entity.order.Order;
import com.example.evm.entity.debt.Debt;
import com.example.evm.repository.order.OrderRepository;
import com.example.evm.repository.payment.PaymentRepository;
import com.example.evm.repository.debt.DebtRepository;
import com.example.evm.service.debt.DebtService;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private DebtRepository debtRepository;
    @Autowired
    private DebtService debtService;

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
    @Transactional
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
        Payment savedPayment = paymentRepository.save(payment);
        
        //  TỰ ĐỘNG cập nhật DEALER_DEBT khi dealer nhận tiền từ khách
        log.info(" Payment created for Order {}: dealer={}, customer={}, amount={}", 
                order != null ? order.getOrderId() : "null",
                order != null && order.getDealer() != null ? order.getDealer().getDealerId() : "null",
                order != null && order.getCustomer() != null ? order.getCustomer().getCustomerId() : "null",
                paymentInfo.getAmount());
        
        //  FIX: Tạo DEALER_DEBT SAU KHI thanh toán cho TẤT CẢ order của dealer
        if (order != null && order.getDealer() != null) {
            log.info(" Creating/Updating DEALER_DEBT for dealer {} - Order {} after payment (customer={})", 
                    order.getDealer().getDealerId(), order.getOrderId(), 
                    order.getCustomer() != null ? order.getCustomer().getCustomerId() : "null");
            createOrUpdateDealerDebt(order, paymentInfo.getAmount());
        }
        
        //  BỎ: KHÔNG tạo CUSTOMER_DEBT ngay khi thanh toán
        // CUSTOMER_DEBT sẽ được tạo SAU KHI Order status = "Completed"
        
        return savedPayment;
    }
    
    /**
     *  Tạo hoặc cập nhật DEALER_DEBT sau khi thanh toán
     * Logic: Thanh toán xong → Tạo debt với amountDue = total - amountPaid
     */
    private void createOrUpdateDealerDebt(Order order, BigDecimal paymentAmount) {
        try {
            log.info(" Looking for existing DEALER_DEBT with Order {}", order.getOrderId());
            
            // Tìm xem đã có DEALER_DEBT cho order này chưa
            List<Debt> activeDebts = debtRepository.findByDebtTypeAndDealerDealerIdAndStatus(
                    "DEALER_DEBT", 
                    order.getDealer().getDealerId(), 
                    "ACTIVE");
            
            String searchPattern = "Order: " + order.getOrderId();
            Debt dealerDebt = activeDebts.stream()
                    .filter(d -> d.getNotes() != null && d.getNotes().contains(searchPattern))
                    .findFirst()
                    .orElse(null);
            
            if (dealerDebt != null) {
                // Đã có debt → Cập nhật amountPaid
                log.info(" Found existing DEALER_DEBT {}, updating amountPaid", dealerDebt.getDebtId());
                BigDecimal currentPaid = dealerDebt.getAmountPaid() != null ? dealerDebt.getAmountPaid() : BigDecimal.ZERO;
                BigDecimal newPaid = currentPaid.add(paymentAmount);
                dealerDebt.setAmountPaid(newPaid);
                dealerDebt.setUpdatedDate(LocalDateTime.now());
                
                if (dealerDebt.getAmountDue().subtract(newPaid).compareTo(BigDecimal.ZERO) <= 0) {
                    dealerDebt.setStatus("PAID");
                }
                
                debtRepository.save(dealerDebt);
                log.info(" Updated DEALER_DEBT {}: amountPaid = {}", dealerDebt.getDebtId(), newPaid);
            } else {
                // Chưa có debt → Kiểm tra xem có còn nợ không trước khi tạo
                BigDecimal orderTotal = BigDecimal.valueOf(order.getTotalPrice() != null ? order.getTotalPrice() : 0);
                BigDecimal remainingDebt = orderTotal.subtract(paymentAmount);
                
                //  FIX: Chỉ tạo debt khi còn nợ (không thanh toán full)
                if (remainingDebt.compareTo(BigDecimal.ZERO) <= 0) {
                    log.info(" Order {} paid in FULL ({}đ) - NO DEBT CREATED", 
                            order.getOrderId(), paymentAmount);
                    return;
                }
                
                log.info(" Creating NEW DEALER_DEBT for Order {} - Paid: {}, Remaining: {}", 
                        order.getOrderId(), paymentAmount, remainingDebt);
                
                Debt newDebt = new Debt();
                newDebt.setDealer(order.getDealer());
                newDebt.setUser(order.getUser());
                newDebt.setCustomer(null);
                newDebt.setDebtType("DEALER_DEBT");
                newDebt.setAmountDue(orderTotal);
                newDebt.setAmountPaid(paymentAmount); //  Set ngay số tiền đã trả
                newDebt.setPaymentMethod("BANK_TRANSFER");
                newDebt.setStatus("ACTIVE");
                newDebt.setNotes("Auto-generated after payment - Order: " + order.getOrderId());
                newDebt.setStartDate(LocalDateTime.now());
                newDebt.setDueDate(LocalDateTime.now().plusMonths(12));
                newDebt.setCreatedDate(LocalDateTime.now());
                
                //  Gọi debtService.createDebt() để tự động tạo schedule
                Debt savedDebt = debtService.createDebt(newDebt);
                
                log.info(" Created DEALER_DEBT {}: amountDue={}, amountPaid={}, remaining={}, schedules={}", 
                        savedDebt.getDebtId(), orderTotal, paymentAmount, remainingDebt, 
                        savedDebt.getDebtSchedules().size());
            }
        } catch (Exception e) {
            log.error(" Failed to create/update DEALER_DEBT for order {}: {}", order.getOrderId(), e.getMessage(), e);
        }
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


