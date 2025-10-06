package com.example.evm.repository.inventory;

import com.example.evm.entity.inventory.InventoryStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryStock, Integer> {
}
