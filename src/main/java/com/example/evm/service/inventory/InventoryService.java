package com.example.evm.service.inventory;

import com.example.evm.dto.inventory.AllocationRequest;
import com.example.evm.dto.inventory.AllocationResponse;
import com.example.evm.dto.inventory.InventoryResponse;
import com.example.evm.dto.inventory.ManufacturerStockResponse;
import com.example.evm.dto.inventory.StockRequest;

import java.util.List;

public interface InventoryService {

    // --- 1. KHO ĐẠI LÝ ---

    List<InventoryResponse> getAllDealerStock();
    
    /**
     * Lấy danh sách kho của dealer cụ thể
     * @param dealerId ID của dealer
     * @return Danh sách InventoryResponse (variant + color + quantity)
     */
    List<InventoryResponse> getDealerStockByDealerId(Long dealerId);
    
    InventoryResponse addOrUpdateDealerStock(StockRequest request);

    InventoryResponse updateStockStatus(Long id, String status);

    void deleteStock(Long id);

    // --- 2. KHO TỔNG ---

    List<ManufacturerStockResponse> getAllManufacturerStock();

    ManufacturerStockResponse addOrUpdateManufacturerStock(StockRequest request); // <-- THÊM HÀM NÀY

    ManufacturerStockResponse updateManufacturerStockStatus(Long id, String status); // <-- THÊM HÀM NÀY

    // --- 3. ĐIỀU PHỐI ---

    /**
     * Phân bổ xe từ kho tổng cho đại lý
     * Tự động update status DealerRequest: APPROVED → SHIPPED
     * 
     * @param request Thông tin phân bổ (dealerId, variantId, color, quantity)
     * @return AllocationResponse chứa thông tin kho đại lý + request đã update
     */
    AllocationResponse allocateStockToDealer(AllocationRequest request);

    String recallStockFromDealer(AllocationRequest request);
}