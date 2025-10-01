package com.example.evm.entity.order;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class Payment {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column (name = "payment_id")
private int paymentid;

@Column(name = "order_id")
private int orderid;

@Column(name = "amount")
private int amount;

@Column(name = "status")
private String status;

@Column(name = "payment_method")
private String paymentmethod;

@Column(name = "payment_date")
private String paymentdate;

public int getPaymentid() {
    return paymentid;
}

public void setPaymentid(int paymentid) {
    this.paymentid = paymentid;
}

public int getOrderid() {
    return orderid;
}

public void setOrderid(int orderid) {
    this.orderid = orderid;
}

public int getAmount() {
    return amount;
}

public void setAmount(int amount) {
    this.amount = amount;
}

public String getStatus() {
    return status;
}

public void setStatus(String status) {
    this.status = status;
}

public String getPaymentmethod() {
    return paymentmethod;
}

public void setPaymentmethod(String paymentmethod) {
    this.paymentmethod = paymentmethod;
}

public String getPaymentdate() {
    return paymentdate;
}

public void setPaymentdate(String paymentdate) {
    this.paymentdate = paymentdate;
}
}
