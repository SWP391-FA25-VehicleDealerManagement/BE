package com.example.evm.service.vehicle;

import com.example.evm.entity.vehicle.Vehicle;
import com.example.evm.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Optional;

@Data
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public Optional<Vehicle> getVehicleById(Long id) {
        return vehicleRepository.findById(id);
    }

    public Vehicle createVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    public Vehicle updateVehicle(Long id, Vehicle updatedVehicle) {
        return vehicleRepository.findById(id).map(vehicle -> {
            vehicle.setModel(updatedVehicle.getModel());
            vehicle.setVersion(updatedVehicle.getVersion());
            vehicle.setColor(updatedVehicle.getColor());
            vehicle.setPrice(updatedVehicle.getPrice());
            vehicle.setStock(updatedVehicle.getStock());
            vehicle.setFeatures(updatedVehicle.getFeatures());
            return vehicleRepository.save(vehicle);
        }).orElseThrow(() -> new RuntimeException("Vehicle not found"));
    }

    public void deleteVehicle(Long id) {
        vehicleRepository.deleteById(id);
    }
}
