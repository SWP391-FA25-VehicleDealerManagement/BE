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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "Dealer name is required")
    @Size(max = 255, message = "Dealer name must not exceed 255 characters")
    private String dealerName;

    @Column(name = "phone")
    @Size(max = 50, message = "Phone number must not exceed 50 characters")
    private String phone;

    @Column(name = "address")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Column(name = "createdby")
    @Size(max = 100, message = "Created by must not exceed 100 characters")
    private String createdBy;

    @Column(name = "createddate")
    private LocalDateTime createdDate;

    // Một Dealer có thể có nhiều User
    @OneToMany(mappedBy = "dealer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> users;
}
