package com.example.evm.service.payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.evm.dto.payment.PaymentInfo;
import com.example.evm.entity.payment.Payment;
import com.example.evm.repository.order.OrderRepository;
import com.example.evm.repository.payment.PaymentRepository;
import com.example.evm.service.debt.DebtService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private final VNPayService VNPayService;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private DebtService debtService;

    PaymentServiceImpl(VNPayService VNPayService) {
        this.VNPayService = VNPayService;
    }

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
        Payment payment = new Payment();
        payment.setOrderId(paymentInfo.getOrderId());
        payment.setAmount(paymentInfo.getAmount());

        // Normalize payment method from UI (vi/EN/case-insensitive)
        String rawMethod = paymentInfo.getPaymentMethod() == null ? "" : paymentInfo.getPaymentMethod().trim();
        String methodUpper = rawMethod.toUpperCase();
        String normalized;
        if (methodUpper.equals("CASH") || methodUpper.equals("TIEN MAT") || methodUpper.equals("TIỀN MẶT")) {
            normalized = "CASH";
        } else if (methodUpper.equals("TRANSFER") || methodUpper.equals("BANK_TRANSFER") || methodUpper.equals("CHUYEN KHOAN") || methodUpper.equals("CHUYỂN KHOẢN")) {
            normalized = "TRANSFER";
        } else {
            // keep original but still fail below with helpful msg
            normalized = rawMethod;
        }

        payment.setPaymentMethod(normalized);
        payment.setPaymentType(paymentInfo.getPaymentType());
        payment.setPaymentId(null);
        if(payment.getPaymentDate() == null){
                payment.setPaymentDate(LocalDateTime.now());
        }
        if(payment.getOrderId()!=null && !orderRepository.existsById(payment.getOrderId())){
            throw new IllegalArgumentException("Invalid order id "+payment.getOrderId());
        }
        if (payment.getAmount() == null) {
            throw new IllegalArgumentException("Amount is required");
        }

           // Xử lý theo phương thức thanh toán
        if ("CASH".equalsIgnoreCase(payment.getPaymentMethod())) {
            payment.setStatus("Completed"); // Hoàn thành ngay
        } else if ("TRANSFER".equalsIgnoreCase(payment.getPaymentMethod())) {
            payment.setStatus("Pending"); // Chờ VNPay xử lý
        } else {
            throw new IllegalArgumentException("Unsupported payment method: " + payment.getPaymentMethod());
        }
        
        Payment savedPayment = paymentRepository.save(payment);
        
        log.info("💰 Payment created: PaymentId={}, OrderId={}, Amount={}, Method={}, Type={}, Status={}", 
                savedPayment.getPaymentId(), savedPayment.getOrderId(), savedPayment.getAmount(),
                savedPayment.getPaymentMethod(), savedPayment.getPaymentType(), savedPayment.getStatus());
        
        // ✅ Tự động tạo Debt ngay khi tạo Payment INSTALLMENT (kể cả khi Pending)
        // Điều này cho phép hiển thị amountPaid ngay lập tức
        if (savedPayment.getPaymentType() != null && "INSTALLMENT".equalsIgnoreCase(savedPayment.getPaymentType())) {
            try {
                log.info("🔄 Auto-creating debt from INSTALLMENT payment (Pending): PaymentId={}, OrderId={}, Amount={}", 
                        savedPayment.getPaymentId(), savedPayment.getOrderId(), savedPayment.getAmount());
                debtService.autoCreateDebtFromPayment(savedPayment.getPaymentId());
                log.info("✅ Debt created successfully for PaymentId={}", savedPayment.getPaymentId());
            } catch (Exception e) {
                log.error("⚠️ Failed to auto-create debt from INSTALLMENT payment {}: {}", 
                        savedPayment.getPaymentId(), e.getMessage(), e);
                log.error("⚠️ Exception details: ", e);
                // Không throw exception để không làm gián đoạn payment creation
            }
        } else {
            log.warn("⚠️ Payment {} is not INSTALLMENT type (paymentType={}), skipping debt creation", 
                    savedPayment.getPaymentId(), savedPayment.getPaymentType());
        }
        
        return savedPayment;
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
    public void deletePayment(Long id) {
      paymentRepository.deleteById(id);
    }

    // ✅ Cập nhật trạng thái thanh toán (dùng cho callback VNPay)
    @Transactional
    public Payment updatePaymentStatus(Long orderId, String status) {
       Payment payment = paymentRepository.findByOrderId(orderId)
        .orElseThrow(()-> new IllegalArgumentException("Payment with orderId " + orderId + " not found"));
        payment.setStatus(status);
        Payment savedPayment = paymentRepository.save(payment);
        
        // ✅ Tự động tạo Debt khi payment completed và paymentType = INSTALLMENT
        if ("Completed".equalsIgnoreCase(status) && "INSTALLMENT".equalsIgnoreCase(payment.getPaymentType())) {
            try {
                log.info("🔄 Auto-creating debt from payment: PaymentId={}, OrderId={}, Amount={}, Type={}", 
                        payment.getPaymentId(), orderId, payment.getAmount(), payment.getPaymentType());
                debtService.autoCreateDebtFromPayment(payment.getPaymentId());
            } catch (Exception e) {
                log.error("⚠️ Failed to auto-create debt from payment {}: {}", payment.getPaymentId(), e.getMessage(), e);
                // Không throw exception để không làm gián đoạn callback VNPay
            }
        }
        
        return savedPayment;
    }

    @Override
    public PaymentResponse createPaymentResponse(Payment payment) {
        try {
            payment.setStatus("Pending");
            payment.setPaymentDate(LocalDateTime.now());
            payment.setOrderId(System.currentTimeMillis());
            paymentRepository.save(payment);

            String redirectUrl=null;

            if ("TRANSFER".equalsIgnoreCase(payment.getPaymentMethod())) {
                redirectUrl= VNPayService.createVNPayUrl(payment);
            }

            return new PaymentResponse(true,
            redirectUrl!=null ? "Redirect to VnPay": "Create payment successfully",
            redirectUrl !=null ? redirectUrl: payment);
        } catch (Exception e) {
           return new PaymentResponse(false,"Erorr"+e.getMessage(),null);
        }
    }
}


