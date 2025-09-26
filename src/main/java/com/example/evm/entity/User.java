package com.example.evm.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "`User`")   // escape the reserved word
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "userName", nullable = false, length = 100)
    @NotBlank @Size(min = 3, max = 100)
    private String userName;

    @Column(nullable = false, length = 255)
    @NotBlank @Size(min = 6)
    private String password;      // BCrypt hash

    @Column(length = 50)
    @Size(max = 50)
    private String phone;

    @Column(length = 255)
    @Email @Size(max = 255)
    private String email;

    @Column(length = 50, nullable = false)
    @NotBlank
    private String role;          // ADMIN, DEALER, MANAGER, EVM

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id")
    private Dealer dealer;    // nullable for admin/EVM

    private LocalDateTime createdDate;
    private LocalDateTime dateModified;
    private LocalDateTime refreshTokenExpiryTime;
}
