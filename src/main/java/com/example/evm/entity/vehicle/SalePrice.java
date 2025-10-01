package com.example.evm.entity.vehicle;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class SalePrice {
@Id
@GeneratedValue (strategy = GenerationType.IDENTITY)
@Column (name = "saleprice_id")
private int salepriceid;

@Column(name = "dealer_id")
private int dealerid;

@Column(name = "variant_id")
private int variantid;

@Column(name = "price")
private int price;

@Column(name = "effectivedate")
private int effectivedate;

public int getSalepriceid() {
    return salepriceid;
}

public void setSalepriceid(int salepriceid) {
    this.salepriceid = salepriceid;
}

public int getDealerid() {
    return dealerid;
}

public void setDealerid(int dealerid) {
    this.dealerid = dealerid;
}

public int getVariantid() {
    return variantid;
}

public void setVariantid(int variantid) {
    this.variantid = variantid;
}

public int getPrice() {
    return price;
}

public void setPrice(int price) {
    this.price = price;
}

public int getEffectivedate() {
    return effectivedate;
}

public void setEffectivedate(int effectivedate) {
    this.effectivedate = effectivedate;
}
}
