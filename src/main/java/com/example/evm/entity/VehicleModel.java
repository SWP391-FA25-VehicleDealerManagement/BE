package com.example.evm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
@Entity
public class VehicleModel {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
 @Column(name = "model_id")
 private int modeid;

 @Column(name = "name")
 private String name;

 @Column (name = "description")
 private String description;

 public int getModeid() {
    return modeid;
 }

 public void setModeid(int modeid) {
    this.modeid = modeid;
 }

 public String getName() {
    return name;
 }

 public void setName(String name) {
    this.name = name;
 }

 public String getDescription() {
    return description;
 }

 public void setDescription(String description) {
    this.description = description;
 }
}
