package com.example.evm.repository.contract;

import com.example.evm.entity.contract.VehicleContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleContractRepository extends JpaRepository<VehicleContract, Long> {
    boolean existsByOrderDetail_OrderDetailId(Long orderDetailId);
}
