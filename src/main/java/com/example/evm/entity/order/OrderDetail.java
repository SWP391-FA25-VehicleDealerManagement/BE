package com.example.evm.entity.order;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class OrderDetail {
@Id
@GeneratedValue (strategy = GenerationType.IDENTITY)
@Column(name = "orderdetail_id")
private int orderdetailid;

@Column(name = "order_id")
private int orderid;

@Column(name = "vehicle_id")
private int vehicleid;

@Column(name = "promotion_id")
private int promotionid;

@Column(name = "quantity")
private int quantity;

public int getOrderdetailid() {
    return orderdetailid;
}

public void setOrderdetailid(int orderdetailid) {
    this.orderdetailid = orderdetailid;
}

public int getOrderid() {
    return orderid;
}

public void setOrderid(int orderid) {
    this.orderid = orderid;
}

public int getVehicleid() {
    return vehicleid;
}

public void setVehicleid(int vehicleid) {
    this.vehicleid = vehicleid;
}

public int getPromotionid() {
    return promotionid;
}

public void setPromotionid(int promotionid) {
    this.promotionid = promotionid;
}

public int getQuantity() {
    return quantity;
}

public void setQuantity(int quantity) {
    this.quantity = quantity;
}

public int getPrice() {
    return price;
}

public void setPrice(int price) {
    this.price = price;
}

@Column(name = "price")
private int price;
}

