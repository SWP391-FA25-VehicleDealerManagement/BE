package com.example.evm.dto.vehicle;

import java.util.Locale;
import java.text.NumberFormat;

import com.example.evm.entity.vehicle.Vehicle;
import com.example.evm.entity.vehicle.VehicleDetail;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class VehicleResponse {
    private Long vehicleId;
    private String name;
    private String color;
    private String image;
    private String price;
    private Integer stock;
    private String variantName;
    private String variantImage;
    private String modelName;
    private String modelDescription;
    private String dealerName;
    
    @JsonProperty("vehicleDetails")
    private VehicleDetailResponse details;

    public VehicleResponse() {}

    public VehicleResponse(Vehicle vehicle) {
        this.vehicleId = vehicle.getVehicleId();
        this.name = vehicle.getName();
        this.color = vehicle.getColor();
        this.image = vehicle.getImage();
        this.stock = vehicle.getStock();

        if (vehicle.getPrice() != null) {
            // 1. Sử dụng NumberFormat với Locale.US để có dấu phẩy (,) làm phân cách hàng nghìn
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        
            // 2. Định dạng giá trị Double thành String
            String formattedPrice = formatter.format(vehicle.getPrice());
        
            // 3. Nối chuỗi "VND" vào kết quả đã định dạng
            this.price = formattedPrice + "VND";
        } else {
            this.price = null; // Hoặc set thành "N/A"
        }
            // --- KẾT THÚC PHẦN SỬA ĐỔI ĐỊNH DẠNG GIÁ ---

        if (vehicle.getVariant() != null) {
            this.variantName = vehicle.getVariant().getName();
            this.variantImage = vehicle.getVariant().getImage();
            if (vehicle.getVariant().getModel() != null) {
                this.modelName = vehicle.getVariant().getModel().getName();
                this.modelDescription = vehicle.getVariant().getModel().getDescription();
            }
        }

        if (vehicle.getDealer() != null) {
            this.dealerName = vehicle.getDealer().getDealerName();
        }
    }

    public VehicleResponse(Vehicle vehicle, VehicleDetail detail) {
        // 1. Gọi constructor cũ để lấy thông tin cơ bản
        this(vehicle);

        // 2. Thêm thông tin chi tiết nếu có
        if (detail != null) {
            this.details = new VehicleDetailResponse(detail);
        }
    }

}
