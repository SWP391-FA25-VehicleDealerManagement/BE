package com.example.evm.entity.dealer;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class DealerContract {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column (name = "DealerContract_id")
private int DealerContractid;

@Column (name = "dealer_id")
private int dealerid;

@Column(name = "start_date")
private String startdate;

@Column(name = "end_date")
private String enddate;

@Column(name = "terms")
private String terms;

@Column(name = "target_sales")
private String targetsales;

public int getDealerContractid() {
    return DealerContractid;
}

public void setDealerContractid(int dealerContractid) {
    DealerContractid = dealerContractid;
}

public int getDealerid() {
    return dealerid;
}

public void setDealerid(int dealerid) {
    this.dealerid = dealerid;
}

public String getStartdate() {
    return startdate;
}

public void setStartdate(String startdate) {
    this.startdate = startdate;
}

public String getEnddate() {
    return enddate;
}

public void setEnddate(String enddate) {
    this.enddate = enddate;
}

public String getTerms() {
    return terms;
}

public void setTerms(String terms) {
    this.terms = terms;
}

public String getTargetsales() {
    return targetsales;
}

public void setTargetsales(String targetsales) {
    this.targetsales = targetsales;
}

}
