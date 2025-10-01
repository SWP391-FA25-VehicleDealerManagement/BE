package com.example.evm.entity.debt;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class Debt {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name = "debt_id")
private int debtid;

@Column(name = "user_id")
private int userid;

@Column(name = "dealer_id")
private int dealerid;

@Column(name = "customer_id")
private int customerid;

@Column(name = "amount_due")
private int amountdue;

@Column(name = "amount_paid")
private int amountpaid;

@Column(name = "status")
private String status;

public int getDebtid() {
    return debtid;
}

public void setDebtid(int debtid) {
    this.debtid = debtid;
}

public int getUserid() {
    return userid;
}

public void setUserid(int userid) {
    this.userid = userid;
}

public int getDealerid() {
    return dealerid;
}

public void setDealerid(int dealerid) {
    this.dealerid = dealerid;
}

public int getCustomerid() {
    return customerid;
}

public void setCustomerid(int customerid) {
    this.customerid = customerid;
}

public int getAmountdue() {
    return amountdue;
}

public void setAmountdue(int amountdue) {
    this.amountdue = amountdue;
}

public int getAmountpaid() {
    return amountpaid;
}

public void setAmountpaid(int amountpaid) {
    this.amountpaid = amountpaid;
}

public String getStatus() {
    return status;
}

public void setStatus(String status) {
    this.status = status;
}
}
