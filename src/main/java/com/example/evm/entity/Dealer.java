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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Dealer")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Dealer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dealer_id")
    private Integer dealerId;

    @Column(name = "dealerName", nullable = false, length = 255)
    @NotBlank @Size(max = 255)
    private String dealerName;

    @Column(length = 50)
    @Size(max = 50)
    private String phone;

    @Column(length = 500)
    @Size(max = 500)
    private String address;

    @Column(length = 100)
    @Size(max = 100)
    private String createdBy;

    private LocalDateTime createdDate;

    // ONE‑TO‑MANY Users
    @OneToMany(mappedBy = "dealer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> users;
}
