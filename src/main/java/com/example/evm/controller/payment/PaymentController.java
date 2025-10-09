package com.example.evm.controller.payment;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.entity.payment.Payment;
import com.example.evm.service.payment.PaymentService;

import org.springframework.web.bind.annotation.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
private final PaymentService paymentService;

@GetMapping
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_EVM_STAFF','ROLE_DEALER_STAFF','ROLE_DEALER_MANAGER')")
public ResponseEntity<ApiResponse<List<Payment>>> getAllPayments(){
    List<Payment> payments = paymentService.getAllPayments();
    return ResponseEntity.ok(new ApiResponse<>(true, "Paymets retrived successfully", payments));
}

@GetMapping("/{id}")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_EVM_STAFF','ROLE_DEALER_STAFF','ROLE_DEALER_MANAGER')")
public ResponseEntity<ApiResponse<Payment>> getPaymentById(@PathVariable Integer id){
    Payment payment = paymentService.getPaymentById(id);
    return payment!=null
    ?ResponseEntity.ok(new ApiResponse<>(true, "Payment retrived successfully", payment))
    :ResponseEntity.ok(new ApiResponse<>(false, "Payments not found", payment));
}

@PostMapping
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_EVM_STAFF','ROLE_DEALER_STAFF','ROLE_DEALER_MANAGER')")
public ResponseEntity<ApiResponse<Payment>> createPayment(@RequestBody Payment payment){
    try {
        Payment createPayment = paymentService.createPayment(payment);
        return ResponseEntity.ok(new ApiResponse<>(true, "Create payments successfully", createPayment));
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
    }
}

@PutMapping("/{id}")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_EVM_STAFF','ROLE_DEALER_MANAGER','ROLE_DEALER_STAFF')")
public ResponseEntity<ApiResponse<Payment>> updatePayment(@PathVariable Integer id, @RequestBody Payment payment){
    try {
        payment.setPaymentId(id);
        Payment updatePayment = paymentService.updatePayment(payment);
        return ResponseEntity.ok(new ApiResponse<>(true, "Update Payment successfully", updatePayment));
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
    }
}

@DeleteMapping("/{id}")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
public ResponseEntity<ApiResponse<Payment>> deletePayment(@PathVariable Integer id){
    try {
        paymentService.deletePayment(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Delete successfully", null));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
    }
    
}

}
