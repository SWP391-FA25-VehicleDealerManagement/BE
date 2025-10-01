package com.example.evm.entity.vehicle;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class Vehicle {
@Id
@GeneratedValue (strategy = GenerationType.IDENTITY)

@Column (name = "vehicle_id")
private int vehicleid;

@Column (name ="variant_id")
private int variantid;

@Column(name = "dealer_id")
private int delaerid;

@Column(name = "name")
private String id;

public int getVehicleid() {
    return vehicleid;
}

public void setVehicleid(int vehicleid) {
    this.vehicleid = vehicleid;
}

public int getVariantid() {
    return variantid;
}

public void setVariantid(int variantid) {
    this.variantid = variantid;
}

public int getDelaerid() {
    return delaerid;
}

public void setDelaerid(int delaerid) {
    this.delaerid = delaerid;
}

public String getId() {
    return id;
}

public void setId(String id) {
    this.id = id;
}

public String getColor() {
    return color;
}

public void setColor(String color) {
    this.color = color;
}

public String getImage() {
    return image;
}

public void setImage(String image) {
    this.image = image;
}

@Column(name = "color")
private String color;

@Column(name = "image")
private String image;

}
