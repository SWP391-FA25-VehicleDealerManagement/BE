package com.example.evm.entity.customer;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class CustomerContract {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)

@Column(name = "CustomerContract_id")
private int CustomerContractid;

@Column(name = "order_id")
private int orderid;

@Column(name = "terms")
private String terms;

@Column(name = "payment_method")
private String paymentmethod;

@Column(name = "status")
private String status;

public int getCustomerContractid() {
    return CustomerContractid;
}

public void setCustomerContractid(int customerContractid) {
    CustomerContractid = customerContractid;
}

public int getOrderid() {
    return orderid;
}

public void setOrderid(int orderid) {
    this.orderid = orderid;
}

public String getTerms() {
    return terms;
}

public void setTerms(String terms) {
    this.terms = terms;
}

public String getPaymentmethod() {
    return paymentmethod;
}

public void setPaymentmethod(String paymentmethod) {
    this.paymentmethod = paymentmethod;
}

public String getStatus() {
    return status;
}

public void setStatus(String status) {
    this.status = status;
}

}
