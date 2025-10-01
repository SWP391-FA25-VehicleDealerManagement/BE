package com.example.evm.entity.debt;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class DebtSchedule {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)

@Column(name="schedule_id")
private int scheduleid;

@Column(name = "debt_id")
private int debtid;

@Column(name = "period_no")
private int periodno;

@Column(name = "start_balance")
private int startbalance;

@Column(name = "principal")
private int principal;

@Column(name = "interest")
private int interest;

@Column(name = "installment")
private int installment;

@Column(name = "end_balance")
private int endbalance;

@Column(name = "due_date")
private String duedate;

@Column(name = "paid_amount")
private int paidamount;

@Column(name = "status")
private String status;

public int getScheduleid() {
    return scheduleid;
}

public void setScheduleid(int scheduleid) {
    this.scheduleid = scheduleid;
}

public int getDebtid() {
    return debtid;
}

public void setDebtid(int debtid) {
    this.debtid = debtid;
}

public int getPeriodno() {
    return periodno;
}

public void setPeriodno(int periodno) {
    this.periodno = periodno;
}

public int getStartbalance() {
    return startbalance;
}

public void setStartbalance(int startbalance) {
    this.startbalance = startbalance;
}

public int getPrincipal() {
    return principal;
}

public void setPrincipal(int principal) {
    this.principal = principal;
}

public int getInterest() {
    return interest;
}

public void setInterest(int interest) {
    this.interest = interest;
}

public int getInstallment() {
    return installment;
}

public void setInstallment(int installment) {
    this.installment = installment;
}

public int getEndbalance() {
    return endbalance;
}

public void setEndbalance(int endbalance) {
    this.endbalance = endbalance;
}

public String getDuedate() {
    return duedate;
}

public void setDuedate(String duedate) {
    this.duedate = duedate;
}

public int getPaidamount() {
    return paidamount;
}

public void setPaidamount(int paidamount) {
    this.paidamount = paidamount;
}

public String getStatus() {
    return status;
}

public void setStatus(String status) {
    this.status = status;
}

}
