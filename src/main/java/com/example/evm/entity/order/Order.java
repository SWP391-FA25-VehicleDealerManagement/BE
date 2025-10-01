package com.example.evm.entity.order;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class Order {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column (name = "order_id")
private int orderid;

@Column(name = "customer_id")
private int customerid;

@Column(name = "user_id")
private int userid;

@Column(name = "total_price")
private int totalprice;

@Column(name = "payment_method")
private String paymentmethod;

@Column(name = "createddate")
private String createddate;

public int getOrderid() {
    return orderid;
}

public void setOrderid(int orderid) {
    this.orderid = orderid;
}

public int getCustomerid() {
    return customerid;
}

public void setCustomerid(int customerid) {
    this.customerid = customerid;
}

public int getUserid() {
    return userid;
}

public void setUserid(int userid) {
    this.userid = userid;
}

public int getTotalprice() {
    return totalprice;
}

public void setTotalprice(int totalprice) {
    this.totalprice = totalprice;
}

public String getPaymentmethod() {
    return paymentmethod;
}

public void setPaymentmethod(String paymentmethod) {
    this.paymentmethod = paymentmethod;
}

public String getCreateddate() {
    return createddate;
}

public void setCreateddate(String createddate) {
    this.createddate = createddate;
}

public int getDealerid() {
    return dealerid;
}

public void setDealerid(int dealerid) {
    this.dealerid = dealerid;
}

public String getStatus() {
    return status;
}

public void setStatus(String status) {
    this.status = status;
}

@Column(name = "dealer_id")
private int dealerid;

@Column(name = "status")
private String status;
}
