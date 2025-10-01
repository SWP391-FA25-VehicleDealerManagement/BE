package com.example.evm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.evm.entity.VehicleModel;

public interface VehicleModelRepository  extends JpaRepository <VehicleModel , Integer>{
    VehicleModel findByName(String name);
    
} 
