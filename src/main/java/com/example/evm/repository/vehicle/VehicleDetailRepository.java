package com.example.evm.repository.vehicle;

import com.example.evm.entity.vehicle.VehicleDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VehicleDetailRepository extends JpaRepository<VehicleDetail, Long> {
    // Tìm detail theo variant_id
    Optional<VehicleDetail> findByVariant_VariantId(Long variantId);
}