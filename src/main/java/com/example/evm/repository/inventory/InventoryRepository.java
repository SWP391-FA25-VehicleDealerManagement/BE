package com.example.evm.repository.inventory;

import com.example.evm.entity.inventory.InventoryStock;
import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.vehicle.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryStock, Integer> {

    @Query("SELECT s FROM InventoryStock s " +
           "LEFT JOIN FETCH s.vehicle v " +
           "LEFT JOIN FETCH v.variant vr " +
           "LEFT JOIN FETCH s.dealer d")
    List<InventoryStock> findAllWithRelations();

    Optional<InventoryStock> findByDealerAndVehicle(Dealer dealer, Vehicle vehicle);
    Optional<InventoryStock> findByDealerIsNullAndVehicle_VehicleId(Long vehicleId);
}
