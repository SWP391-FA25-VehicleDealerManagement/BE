package com.example.evm.entity.testdrive;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class TestDrive {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name = "testdrive_id")
private int testdriveid;

@Column(name = "dealer_id")
private int dealerid;

@Column(name = "customer_id")
private int customerid;

@Column(name = "date")
private String  date;

@Column(name = "status")
private String status;

@Column(name = "assignedBy")
private String assignedBy;

public int getTestdriveid() {
    return testdriveid;
}

public void setTestdriveid(int testdriveid) {
    this.testdriveid = testdriveid;
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

public String getDate() {
    return date;
}

public void setDate(String date) {
    this.date = date;
}

public String getStatus() {
    return status;
}

public void setStatus(String status) {
    this.status = status;
}

public String getAssignedBy() {
    return assignedBy;
}

public void setAssignedBy(String assignedBy) {
    this.assignedBy = assignedBy;
}
}
