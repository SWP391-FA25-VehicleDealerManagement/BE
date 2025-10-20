package com.example.evm.entity.vehicle;

import com.example.evm.entity.inventory.InventoryStock;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "Vehicle")
@Data
@NoArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "vin_number", unique = true, nullable = false, length = 100)
    private String vinNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private InventoryStock stock;

    @Column(name = "license_plate", unique = true, length = 20)
    private String licensePlate;

    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;

    @Column(name = "warranty_expiry_date")
    private LocalDate warrantyExpiryDate;
}