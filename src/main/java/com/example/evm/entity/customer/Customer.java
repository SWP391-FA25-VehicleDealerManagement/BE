package com.example.evm.entity.customer;

import com.example.evm.entity.dealer.Dealer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Customer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "dealer_id")
    private Long dealerId;

    @Column(name = "customerName", nullable = false, length = 255)
    @NotBlank(message = "Customer name is required")
    @Size(max = 255, message = "Customer name must not exceed 255 characters")
    private String customerName;

    @Column(name = "email", length = 255)
   @Pattern(
    regexp = "^[a-zA-Z0-9._%+-]+@(gmail\\.com|yahoo\\.com|hotmail\\.com|vinfast\\.com|outlook\\.com)$",
    message = "Chỉ chấp nhận email từ Gmail, Yahoo, Hotmail, Outlook hoặc VinFast")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Column(name = "phone", length = 50)
    @Pattern(
    regexp = "^\\+?[0-9]{8,15}$",
    message = "Số điện thoại không hợp lệ. Chỉ chấp nhận số (có thể có dấu +), độ dài 8-15 chữ số")
    @Size(max = 50, message = "Phone must not exceed 50 characters")
    private String phone;



    // 🔹 Liên kết đến dealer — để backend tự gán dealer khi tạo customer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id", insertable = false, updatable = false)
    @JsonIgnore
    private Dealer dealer;

    // 🔹 Ai là người tạo customer này
    @Column(name = "createBy", length = 100)
    @Size(max = 100, message = "Created By must not exceed 100 characters")
    private String createBy;
}
