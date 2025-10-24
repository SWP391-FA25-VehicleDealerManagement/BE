package com.example.evm.service.vehicle;

import com.example.evm.dto.vehicle.StockSummaryResponse;
import com.example.evm.dto.vehicle.VehicleFullResponse;
import com.example.evm.dto.vehicle.VehicleRequest;
import java.util.List;

public interface VehicleService { // Đặt tên Interface

    List<VehicleFullResponse> getAllVehicles();
    VehicleFullResponse createVehicle(VehicleRequest request);
    VehicleFullResponse getVehicleById(Long id);
    List<VehicleFullResponse> getAllManufacturerVehicles();
    List<StockSummaryResponse> getManufacturerStockSummary();
    List<VehicleFullResponse> getDealerVehicles(Long dealerId);
    List<StockSummaryResponse> getDealerStockSummary(Long dealerId);

}