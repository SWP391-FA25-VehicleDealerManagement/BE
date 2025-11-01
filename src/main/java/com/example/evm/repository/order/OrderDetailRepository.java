package com.example.evm.repository.order;

import com.example.evm.entity.order.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    List<OrderDetail> findByOrderOrderId(Long orderId);
    List<OrderDetail> findByVehicleVehicleId(Long vehicleId);
    boolean existsByVehicle_VehicleId(Long vehicleId);
}
