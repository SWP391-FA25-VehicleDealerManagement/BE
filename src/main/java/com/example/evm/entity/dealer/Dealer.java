package com.example.evm.entity.dealer;

import java.time.LocalDateTime;
import java.util.List;

import com.example.evm.entity.user.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
<<<<<<< HEAD
    private Long dealerId;
=======
    private Integer dealerId;
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d

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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    // ONE‑TO‑MANY Users
    @OneToMany(mappedBy = "dealer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<User> users;
}
