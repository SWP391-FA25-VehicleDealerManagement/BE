package com.example.evm.entity.testDrive;

import java.time.LocalDateTime;

import com.example.evm.entity.customer.Customer;
import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.vehicle.Vehicle;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "TestDrive")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestDrive {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "testdrive_id")
    private Long testDriveId;

    @JsonIgnore  // ✅ Prevent lazy loading serialization error
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id")
    private Dealer dealer;

    @JsonIgnore  // ✅ Prevent lazy loading serialization error
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @JsonIgnore  // ✅ Prevent lazy loading serialization error
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column(name = "scheduled_date")
    private LocalDateTime scheduledDate;

    @Column(name = "status", length = 30)
    private String status = "SCHEDULED";

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "assigned_by", length = 100)
    private String assignedBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
    }
    
    // Helper methods để expose IDs mà không trigger lazy loading
    public Long getDealerId() {
        return dealer != null ? dealer.getDealerId() : null;
    }
    
    public Long getCustomerId() {
        return customer != null ? customer.getCustomerId() : null;
    }
    
    public Long getVehicleId() {
        return vehicle != null ? vehicle.getVehicleId() : null;
    }
}
