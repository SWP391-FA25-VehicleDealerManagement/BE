package com.example.evm.repository.inventory;

import com.example.evm.entity.inventory.InventoryStock;
// import com.example.evm.entity.dealer.Dealer; // <-- Không cần thiết cho các hàm này nữa
// import com.example.evm.entity.vehicle.Vehicle; // <-- Xóa hoàn toàn
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryStockRepository extends JpaRepository<InventoryStock, Long> {

    /**
     * Tìm InventoryStock theo dealer ID (cho schema mới)
     */
    Optional<InventoryStock> findByDealerDealerId(Long dealerId);
    
    /**
     * Tìm tất cả InventoryStock theo dealer ID
     */
    List<InventoryStock> findAllByDealerDealerId(Long dealerId);
}