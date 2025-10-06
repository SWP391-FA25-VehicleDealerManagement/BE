package com.example.evm.service.inventory;

import com.example.evm.dto.inventory.InventoryResponse;
import com.example.evm.entity.inventory.InventoryStock;

import java.util.List;

public interface InventoryService {
    List<InventoryResponse> getAll();

    InventoryResponse addStock(InventoryStock stock);

    InventoryResponse updateStock(Integer id, InventoryStock stock);

    void deleteStock(Integer id);

    String allocateVehicleToDealer(Integer vehicleId, Integer dealerId, Integer quantity);
}
