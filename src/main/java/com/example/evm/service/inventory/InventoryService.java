package com.example.evm.service.inventory;

import com.example.evm.dto.inventory.AllocationRequest;
import com.example.evm.dto.inventory.InventoryResponse;
import com.example.evm.dto.inventory.StockRequest;

import java.util.List;

public interface InventoryService {

    // --- 1. KHO ĐẠI LÝ ---

    List<InventoryResponse> getAllDealerStock();
    
    InventoryResponse addOrUpdateDealerStock(StockRequest request);

    InventoryResponse updateStockStatus(Long id, String status);

    void deleteStock(Long id);

    // --- 2. KHO TỔNG ---

    List<InventoryResponse> getAllManufacturerStock();

    InventoryResponse addOrUpdateManufacturerStock(StockRequest request); // <-- THÊM HÀM NÀY

    InventoryResponse updateManufacturerStockStatus(Long id, String status); // <-- THÊM HÀM NÀY

    // --- 3. ĐIỀU PHỐI ---

    InventoryResponse allocateStockToDealer(AllocationRequest request);

    String recallStockFromDealer(AllocationRequest request);
}