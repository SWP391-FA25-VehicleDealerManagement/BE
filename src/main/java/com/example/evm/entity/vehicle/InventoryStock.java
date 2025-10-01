package com.example.evm.entity.vehicle;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class InventoryStock {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_id")
private int stockid;

@Column(name = "vehicle_id")
private int vehicleid;

@Column(name = "dealer_id")
private int dealerid;

@Column(name = "quantity")
private int quantity;

@Column( name = "status")
private String status;

public int getStockid() {
    return stockid;
}

public void setStockid(int stockid) {
    this.stockid = stockid;
}

public int getVehicleid() {
    return vehicleid;
}

public void setVehicleid(int vehicleid) {
    this.vehicleid = vehicleid;
}

public int getDealerid() {
    return dealerid;
}

public void setDealerid(int dealerid) {
    this.dealerid = dealerid;
}

public int getQuantity() {
    return quantity;
}

public void setQuantity(int quantity) {
    this.quantity = quantity;
}

public String getStatus() {
    return status;
}

public void setStatus(String status) {
    this.status = status;
}

}
