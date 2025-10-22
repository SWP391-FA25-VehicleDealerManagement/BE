package com.example.evm.dto.vehicle;

import com.example.evm.entity.vehicle.Vehicle;
import com.example.evm.entity.vehicle.VehicleDetail; 
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale; 

/**
 * DTO representing a specific Vehicle, mapping details from Vehicle and InventoryStock.
 */
@Data
@NoArgsConstructor
public class VehicleResponse {

    // --- FIELDS TỪ ENTITY VEHICLE (CÁ NHÂN) ---
    private Long vehicleId;
    private String vinNumber;
    private LocalDate manufactureDate;
    private LocalDate warrantyExpiryDate;

    // --- FIELDS TỪ INVENTORYSTOCK (ĐƯỢC MAP QUA MỐI QUAN HỆ) ---           
    private Long variantId;
    private String modelName;       
    private String variantName;
    private String color;
    private String listingPrice;
    private String imageUrl;

    @JsonProperty("vehicleDetails") 
    private VehicleDetailResponse details;

    // --- CONSTRUCTOR CHÍNH ĐÃ SỬA LỖI ---
    public VehicleResponse(Vehicle vehicle) {
        this.vehicleId = vehicle.getVehicleId();
        this.vinNumber = vehicle.getVinNumber();
        this.manufactureDate = vehicle.getManufactureDate();
        this.warrantyExpiryDate = vehicle.getWarrantyExpiryDate();
        
        // ⚠️ KIỂM TRA QUAN TRỌNG: Đảm bảo InventoryStock không null trước khi truy cập
        if (vehicle.getInventoryStock() != null) {
            
            // 1. Lấy thông tin Màu sắc (từ InventoryStock)
            this.color = vehicle.getInventoryStock().getColor();

            if (vehicle.getInventoryStock().getListingPrice() != null) {
                BigDecimal price = vehicle.getInventoryStock().getListingPrice();
                NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
                String formattedPrice = formatter.format(price.doubleValue());
                this.listingPrice = formattedPrice + " VND"; 
            } else {
                this.listingPrice = "N/A";
            }

            // 2. Lấy thông tin Variant và Model
            if (vehicle.getInventoryStock().getVariant() != null) {
                this.variantId = vehicle.getInventoryStock().getVariant().getVariantId();
                this.variantName = vehicle.getInventoryStock().getVariant().getName();
                this.imageUrl = vehicle.getInventoryStock().getVariant().getImageUrl();

                // Lấy thông tin Model từ Variant
                if (vehicle.getInventoryStock().getVariant().getModel() != null) {
                    this.modelName = vehicle.getInventoryStock().getVariant().getModel().getName();
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