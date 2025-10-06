package com.example.evm.dto.vehicle;

<<<<<<< HEAD
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class VehicleResponse {
    private Long vehicleId;
    private String name;
    private String color;
    private String variantName;
    private String modelName;
    private String modelDescription;
    private String price;
    private String dealerName;
    private String vehicleImage;   // 🆕 image của Vehicle
    private String variantImage;   // 🆕 image của Variant
=======
import com.example.evm.entity.vehicle.Vehicle;
import lombok.Data;

@Data
public class VehicleResponse {
    private Integer vehicleId;
    private String name;
    private String color;
    private String image;
    private Double price;
    private Integer stock;
    private String variantName;
    private String variantImage;
    private String modelName;
    private String modelDescription;
    private String dealerName;

    public VehicleResponse() {}

    public VehicleResponse(Vehicle vehicle) {
        this.vehicleId = vehicle.getVehicleId();
        this.name = vehicle.getName();
        this.color = vehicle.getColor();
        this.image = vehicle.getImage();
        this.price = vehicle.getPrice();
        this.stock = vehicle.getStock();

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
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
}
