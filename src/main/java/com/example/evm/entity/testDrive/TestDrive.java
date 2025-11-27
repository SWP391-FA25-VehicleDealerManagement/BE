package com.example.evm.entity.testDrive;

import java.time.LocalDateTime;

import com.example.evm.entity.customer.Customer;
import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.vehicle.Vehicle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;



import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

import com.example.evm.entity.feedback.Feedback;
import jakarta.persistence.CascadeType;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id")
    @JsonIgnore  // Tránh lazy loading issues và circular reference
    private Dealer dealer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    @JsonIgnore  // Tránh lazy loading issues và circular reference
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    @JsonIgnore  // Tránh lazy loading issues và circular reference
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
    @OneToMany(mappedBy = "testDrive", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Feedback> feedbacks;
}
