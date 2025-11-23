package com.example.evm.repository.vehicle;

import com.example.evm.entity.vehicle.VehicleVariant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleVariantRepository extends JpaRepository<VehicleVariant, Long> {
    boolean existsByModelModelId(Long modelId);
}
