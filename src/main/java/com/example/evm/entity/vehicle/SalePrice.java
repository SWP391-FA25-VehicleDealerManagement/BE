package com.example.evm.entity.vehicle; // Đảm bảo đúng package path

import com.example.evm.entity.dealer.Dealer; // Thay thế bằng package path Dealer
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

import java.time.LocalDate; // Sử dụng LocalDate cho ngày tháng

@Entity
@Table(name = "SalePrice")
@Data // Tương đương @Getter, @Setter, @ToString, @EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalePrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "saleprice_id")
    private Integer salepriceId;

    // --- Mối quan hệ với Dealer ---
    @Column(name = "dealer_id", nullable = false)
    private Integer dealerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id", insertable = false, updatable = false)
    private Dealer dealer; 

    // --- Mối quan hệ với VehicleVariant ---
    @Column(name = "variant_id", nullable = false)
    private Integer variantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", insertable = false, updatable = false)
    private VehicleVariant variant;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "effectivedate", nullable = false)
    private LocalDate effectiveDate; // Lưu ý tên cột trong DB là effectivedate

    // Có thể thêm trường isActive/status nếu bạn muốn quản lý giá nào đang áp dụng bằng cách khác
    // @Column(name = "is_active")
    // private Boolean isActive = true;
}