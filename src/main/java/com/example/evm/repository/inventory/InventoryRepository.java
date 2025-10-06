package com.example.evm.repository.inventory;

import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.inventory.InventoryStock;
import com.example.evm.entity.vehicle.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryStock, Integer> {

    // ✅ Lấy hàng tồn trong kho tổng (chưa có đại lý, ID = null)
    List<InventoryStock> findByDealerIsNullAndVehicle_VehicleId(Integer vehicleId);

    // ✅ Lấy hàng tồn của một đại lý cụ thể
    Optional<InventoryStock> findByDealerAndVehicle(Dealer dealer, Vehicle vehicle);
}
