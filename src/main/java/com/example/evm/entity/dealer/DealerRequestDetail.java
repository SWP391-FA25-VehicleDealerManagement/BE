package com.example.evm.entity.dealer;

import com.example.evm.entity.vehicle.VehicleVariant;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "DealerRequestDetail")
@Data
public class DealerRequestDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Long detailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    @JsonIgnore  // Tránh circular reference
    private DealerRequest dealerRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = true)  // Cho phép NULL - variantId là optional
    @JsonIgnore  // Tránh lazy loading issues, dùng DTO để trả về
    private VehicleVariant vehicleVariant;

    @Column(name = "color", length = 50)
    private String color;  // Thêm trường màu sắc

    @Column(name = "quantity")
    private Integer quantity;  // Đổi lại thành Integer (không phải LongLong)

    // Đổi từ Double sang BigDecimal
    @Column(name = "unit_price", precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "notes", length = 255)
    private String notes;

    // Helper method - cập nhật return type
    public BigDecimal getLineTotal() {
        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}