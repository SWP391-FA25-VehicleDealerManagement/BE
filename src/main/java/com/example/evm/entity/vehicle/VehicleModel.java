package com.example.evm.entity.vehicle; // Hoặc package entity của bạn

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "VehicleModel")
@Data
@NoArgsConstructor
public class VehicleModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "model_id")
    private Long modelId;

    @Column(name = "name", nullable = false, length = 100)
    private String name; // <-- Giữ

    @Column(name = "description")
    @Lob // Dùng @Lob nếu mô tả có thể dài
    private String description; // <-- Giữ

    @Column(name = "status", length = 50)
    private String status; // <-- Giữ (dùng cho soft delete)

    // Thêm: Hãng sản xuất
    @Column(name = "manufacturer", nullable = false, length = 100)
    private String manufacturer; // <-- Thêm

    // Thêm: Năm sản xuất
    @Column(name = "[year]", nullable = false) // Dùng ngoặc vuông vì "year" là từ khóa SQL
    private Integer year; // <-- Thêm

    // Thêm: Kiểu dáng
    @Column(name = "body_type", length = 50)
    private String bodyType; // <-- Thêm
}