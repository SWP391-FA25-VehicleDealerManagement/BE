package com.example.evm.repository.inventory;

import com.example.evm.entity.inventory.InventoryStock;
// import com.example.evm.entity.dealer.Dealer; // <-- Không cần thiết cho các hàm này nữa
// import com.example.evm.entity.vehicle.Vehicle; // <-- Xóa hoàn toàn
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryStockRepository extends JpaRepository<InventoryStock, Long> {

    @Query("SELECT s FROM InventoryStock s " +
           "LEFT JOIN FETCH s.variant v " +
           "LEFT JOIN FETCH v.model m " +
           "LEFT JOIN FETCH s.dealer d")
    List<InventoryStock> findAllWithRelations();
    
    // ✅ Lấy kho của dealer cụ thể (với relations)
    @Query("SELECT s FROM InventoryStock s " +
           "LEFT JOIN FETCH s.variant v " +
           "LEFT JOIN FETCH v.model m " +
           "LEFT JOIN FETCH s.dealer d " +
           "WHERE d.dealerId = :dealerId")
    List<InventoryStock> findByDealerIdWithRelations(Long dealerId);

    Optional<InventoryStock> findByVariantVariantIdAndColorAndDealerDealerId(
        Long variantId, String color, Long dealerId
    );
}