package com.example.evm.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Dealer")
public class Dealer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dealer_id")
    private Integer dealerId;

    @Column(name = "dealerName", nullable = false)
    private String dealerName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "createdby")
    private String createdBy;

    @Column(name = "createddate")
    private LocalDateTime createdDate;

    // Một Dealer có thể có nhiều User
    @OneToMany(mappedBy = "dealer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> users;
}
