package com.example.evm.service.vehicle;

import com.example.evm.dto.vehicle.DealerVehicleResponse;
import com.example.evm.dto.vehicle.StockSummaryResponse;
import com.example.evm.dto.vehicle.VehicleFullResponse;
import com.example.evm.dto.vehicle.VehicleRequest;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface VehicleService { // Đặt tên Interface

    List<VehicleFullResponse> getAllVehicles();
    VehicleFullResponse createVehicle(VehicleRequest request, MultipartFile file);
    VehicleFullResponse updateVehicle(Long id, String vinNumber, String color, MultipartFile file);
    void setVehicleAsTestDrive(Long vehicleId);
    void returnVehicleFromTestDrive(Long vehicleId);
    VehicleFullResponse getVehicleById(Long id);
    List<VehicleFullResponse> getAllManufacturerVehicles();
    List<StockSummaryResponse> getManufacturerStockSummary();
    List<DealerVehicleResponse> getDealerVehicles(Long dealerId);
    List<StockSummaryResponse> getDealerStockSummary(Long dealerId);
    List<VehicleFullResponse> getTestDriveVehicles();
    List<VehicleFullResponse> getTestDriveVehiclesByDealer(Long dealerId);
    void deleteVehicle(Long vehicleId);
}