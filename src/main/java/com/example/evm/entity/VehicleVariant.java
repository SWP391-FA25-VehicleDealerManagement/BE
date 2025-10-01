package com.example.evm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class VehicleVariant {
@Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 @Column (name = "variant_id")
 private int variantid;

 @Column (name = "model_id")
 private int modelid;

 @Column (name = "name")
 private String name;

@Column (name = "image")
private String image;

public int getVariantid() {
    return variantid;
}

public void setVariantid(int variantid) {
    this.variantid = variantid;
}

public int getModelid() {
    return modelid;
}

public void setModelid(int modelid) {
    this.modelid = modelid;
}

public String getName() {
    return name;
}

public void setName(String name) {
    this.name = name;
}

public String getImage() {
    return image;
}

public void setImage(String image) {
    this.image = image;
}


}
