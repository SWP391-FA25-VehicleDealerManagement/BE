package com.example.evm.entity.vehicle; // Hoặc package entity của bạn

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal; // Import BigDecimal

@Entity
@Table(name = "VehicleVariant")
@Data
@NoArgsConstructor
public class VehicleVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_id")
    private Long variantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private VehicleModel model; // <-- Giữ

    @Column(name = "name", nullable = false, length = 150)
    private String name; // <-- Giữ

    // Sửa: Đổi tên cột và trường
    @Column(name = "image", length = 500) 
    private String imageUrl; // <-- Sửa tên từ 'image'

    @Column(name = "status", length = 50)
    private String status; // <-- Giữ (dùng cho soft delete)

    // Thêm: Giá niêm yết
    @Column(name = "msrp", nullable = false) 
    private BigDecimal msrp; // <-- Thêm
}