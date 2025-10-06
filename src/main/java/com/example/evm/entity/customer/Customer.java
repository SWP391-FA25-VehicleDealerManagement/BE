package com.example.evm.entity.customer;

import com.example.evm.entity.dealer.Dealer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
<<<<<<< HEAD
    private Long customerId;
=======
    private Integer customerId;
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d

    @Column(name = "customerName", nullable = false, length = 255)
    @NotBlank(message = "Customer name is required")
    @Size(max = 255, message = "Customer name must not exceed 255 characters")
    private String customerName;

    @Column(name = "email", length = 255)
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Column(name = "phone", length = 50)
    @Size(max = 50, message = "Phone must not exceed 50 characters")
    private String phone;

    @Column(name = "dealer_id")
<<<<<<< HEAD
    private Long dealerId;
=======
    private Integer dealerId;
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id", insertable = false, updatable = false)
    @JsonIgnore
    private Dealer dealer;

    @Column(name = "createBy", length = 100)
<<<<<<< HEAD
=======
    @Size (max = 100, message = "Create By must not exceed 100 characters")
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
    private String createBy;
}
