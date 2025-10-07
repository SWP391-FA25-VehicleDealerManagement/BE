package com.example.evm.dto.vehicle;

import com.example.evm.entity.vehicle.Vehicle;
import lombok.Data;

@Data
public class VehicleResponse {
    private Long vehicleId;
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
}
