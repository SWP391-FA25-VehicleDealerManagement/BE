package com.example.evm.dto.vehicle;

import com.example.evm.entity.vehicle.Vehicle;
import com.example.evm.entity.vehicle.VehicleDetail; 
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate; 

/**
 * DTO representing a specific Vehicle, mapping details from Vehicle and InventoryStock.
 */
@Data
@NoArgsConstructor
public class VehicleResponse {

    // --- FIELDS TỪ ENTITY VEHICLE (CÁ NHÂN) ---
    private Long vehicleId;
    private String vinNumber;
    private String licensePlate;
    private LocalDate manufactureDate;
    private LocalDate warrantyExpiryDate;

    // --- FIELDS TỪ INVENTORYSTOCK (ĐƯỢC MAP QUA MỐI QUAN HỆ) ---
    private String color;           
    private Long variantId;         
    private String variantName;     
    private String modelName;

    @JsonProperty("vehicleDetails") 
    private VehicleDetailResponse details;

    // --- CONSTRUCTOR CHÍNH ĐÃ SỬA LỖI ---
    public VehicleResponse(Vehicle vehicle) {
        this.vehicleId = vehicle.getVehicleId();
        this.vinNumber = vehicle.getVinNumber();
        this.licensePlate = vehicle.getLicensePlate();
        this.manufactureDate = vehicle.getManufactureDate();
        this.warrantyExpiryDate = vehicle.getWarrantyExpiryDate();

        // ⚠️ KIỂM TRA QUAN TRỌNG: Đảm bảo InventoryStock không null trước khi truy cập
        if (vehicle.getStock() != null) {
            
            // 1. Lấy thông tin Màu sắc (từ InventoryStock)
            this.color = vehicle.getStock().getColor(); 
            
            // 2. Lấy thông tin Variant và Model
            if (vehicle.getStock().getVariant() != null) {
                this.variantId = vehicle.getStock().getVariant().getVariantId();
                this.variantName = vehicle.getStock().getVariant().getName();
                
                // Lấy thông tin Model từ Variant
                if (vehicle.getStock().getVariant().getModel() != null) {
                    this.modelName = vehicle.getStock().getVariant().getModel().getName();
                }
            }
        }
    }

    // Constructor với Details 
    public VehicleResponse(Vehicle vehicle, VehicleDetail detail) {
        this(vehicle); // Gọi constructor chính
        if (detail != null) {
            // Lưu ý: Đảm bảo VehicleDetailResponse đã được sửa để hoạt động
            this.details = new VehicleDetailResponse(detail); 
        }
    }
}