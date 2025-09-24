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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "[User]")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "userName", nullable = false, length = 100)
    private String userName;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "role", length = 50)
    private String role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id")
    private Dealer dealer;

    @Column(name = "createddate")
    private LocalDateTime createdDate;

    @Column(name = "datemodified")
    private LocalDateTime dateModified;

    @Column(name = "refreshtokenexpirydime")
    private LocalDateTime refreshTokenExpiryTime;
}
