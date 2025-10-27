package com.example.evm.entity.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.evm.entity.order.Order;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
@Id
@GeneratedValue (strategy = GenerationType.IDENTITY)
@Column(name = "payment_id")
private Long paymentId;


@Column(name = "order_id")
private Long orderId;

@Column(name = "amount" , precision = 18, scale = 2)
private BigDecimal amount;

@Column(name = "status", length = 255)
@Size(max = 255, message = "Status must not exceed 255 charaters")  
private String status;

@Column(name = "payment_method",length = 255)
@Size(max = 255,message = "Payment method must not exceed 255 characters")
private String paymentMethod;

@Column(name = "payment_date")
@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
private LocalDateTime paymentDate;
  
@Column(name = "payment_type",length = 255)
@Size(max = 255,message = "Payment type must not exceed 255 characters")
private String paymentType;


@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "order_id",insertable = false, updatable = false)
@JsonIgnore
private Order order;
}
