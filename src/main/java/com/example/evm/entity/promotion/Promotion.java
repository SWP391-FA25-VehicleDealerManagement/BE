
package com.example.evm.entity.promotion;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class Promotion {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name ="promo_id")
private int promoid;

@Column(name = "dealer_id")
private int dealerid;

@Column(name = "title")
private String title;

@Column(name = "description")
private String description;

@Column(name = "discount_rate")
private int discountrate;

@Column(name = "start_date")
private String startdate;

@Column(name = "end_date")
private String enddate;

public int getPromoid() {
    return promoid;
}

public void setPromoid(int promoid) {
    this.promoid = promoid;
}

public int getDealerid() {
    return dealerid;
}

public void setDealerid(int dealerid) {
    this.dealerid = dealerid;
}

public String getTitle() {
    return title;
}

public void setTitle(String title) {
    this.title = title;
}

public String getDescription() {
    return description;
}

public void setDescription(String description) {
    this.description = description;
}

public int getDiscountrate() {
    return discountrate;
}

public void setDiscountrate(int discountrate) {
    this.discountrate = discountrate;
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
}
