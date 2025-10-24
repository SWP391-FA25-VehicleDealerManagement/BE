package com.example.evm.repository.inventory;

import com.example.evm.entity.inventory.ManufacturerStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository ManufacturerStockRepository - Quản lý kho tổng
 * 
 * Cấu trúc mới:
 * - ManufacturerStock không còn chứa variant_id, color, quantity
 * - Chỉ quản lý thông tin kho (warehouse_name, location, status)
 * - Số lượng xe được tính qua COUNT(Vehicle) WHERE manufacturer_stock_id = ?
 */
@Repository
public interface ManufacturerStockRepository extends JpaRepository<ManufacturerStock, Long> {

    /**
     * Tìm kho theo status
     */
    List<ManufacturerStock> findByStatus(String status);
    
    /**
     * Tìm kho theo tên
     */
    List<ManufacturerStock> findByWarehouseNameContainingIgnoreCase(String name);
}