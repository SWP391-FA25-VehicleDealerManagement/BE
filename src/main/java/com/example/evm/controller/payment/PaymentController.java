package com.example.evm.controller.payment;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.entity.payment.Payment;
import com.example.evm.service.payment.PaymentService;
import com.example.evm.service.payment.VNPayService;

import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    @Autowired
    private  PaymentService paymentService;

    @Autowired
    private VNPayService vnPayService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','EVM_STAFF','DEALER_STAFF','DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Payment>>> getAllPayments() {
        List<Payment> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(new ApiResponse<>(true, "Paymets retrived successfully", payments));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','EVM_STAFF','DEALER_STAFF','DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Payment>> getPaymentById(@PathVariable Long id) {
        Payment payment = paymentService.getPaymentById(id);
        return payment != null
                ? ResponseEntity.ok(new ApiResponse<>(true, "Payment retrived successfully", payment))
                : ResponseEntity.ok(new ApiResponse<>(false, "Payments not found", payment));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','EVM_STAFF','DEALER_STAFF','DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<?>> createPayment(@RequestBody Payment payment) {
        try {
            Payment createPayment = paymentService.createPayment(payment);
            if("TRANSFER".equalsIgnoreCase(payment.getPaymentMethod())){
                     //Nếu là chuyển khoản -> tạo link VNPay sandbox
                     String vnpayUrl= vnPayService.createVNPayUrl(createPayment);
                     return ResponseEntity.ok(new ApiResponse<>(true,"Redirect to VNPay",vnpayUrl));
            }
             return ResponseEntity.ok(new ApiResponse<>(true,"Payment completed successfully",createPayment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

   @GetMapping("/vnpay_return")
public ResponseEntity<ApiResponse<String>> handleVNPayReturn(@RequestParam Map<String,String> allParams){
    try {
        // URL DECODE params
        Map<String, String> decodedParams = new HashMap<>();
        allParams.forEach((k, v) -> {
            try {
                decodedParams.put(k, URLDecoder.decode(v, StandardCharsets.UTF_8));
            } catch (Exception e) {
                decodedParams.put(k, v);
            }
        });
        
        // Validate signature
        boolean isValid = vnPayService.validateVNPayResponse(decodedParams);
        if(!isValid){
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false,"Invalid VNPay signature",null)); 
        }
        
        String orderId = decodedParams.get("vnp_TxnRef");
        String responseCode = decodedParams.get("vnp_ResponseCode");
        String transactionStatus = decodedParams.get("vnp_TransactionStatus");

        if("00".equals(responseCode) && "00".equals(transactionStatus)){
             paymentService.updatePaymentStatus(Long.parseLong(orderId),"Completed");
             return ResponseEntity.ok(new ApiResponse<>(true,"Payment completed successfully",null));
        }else{
             paymentService.updatePaymentStatus(Long.parseLong(orderId), "Failed");
             return ResponseEntity.ok(new ApiResponse<>(false,"Payment failed or canceled",null));
        }
    } catch (Exception e) {
        return ResponseEntity.internalServerError()
            .body(new ApiResponse<>(false,e.getMessage(),null));
    }
}

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','EVM_STAFF','DEALER_MANAGER','DEALER_STAFF')")
    public ResponseEntity<ApiResponse<Payment>> updatePayment(@PathVariable Long id, @RequestBody Payment payment) {
        try {
            payment.setPaymentId(id);
            Payment updatePayment = paymentService.updatePayment(payment);
            return ResponseEntity.ok(new ApiResponse<>(true, "Update Payment successfully", updatePayment));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Payment>> deletePayment(@PathVariable Long id) {
        try {
            paymentService.deletePayment(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Delete successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }

    }

}
