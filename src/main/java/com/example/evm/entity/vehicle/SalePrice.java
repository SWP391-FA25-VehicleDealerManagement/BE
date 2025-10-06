<<<<<<< HEAD
package com.example.evm.entity.vehicle; // Đảm bảo đúng package path

import com.example.evm.entity.dealer.Dealer; // Thay thế bằng package path Dealer
=======
package com.example.evm.entity.vehicle;

import com.example.evm.entity.dealer.Dealer;
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

<<<<<<< HEAD
import java.time.LocalDate; // Sử dụng LocalDate cho ngày tháng

@Entity
@Table(name = "SalePrice")
@Data // Tương đương @Getter, @Setter, @ToString, @EqualsAndHashCode
=======
import java.time.LocalDate;

@Entity
@Table(name = "SalePrice")
@Data
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalePrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "saleprice_id")
<<<<<<< HEAD
    private Long salepriceId;

    // --- Mối quan hệ với Dealer ---
    @Column(name = "dealer_id", nullable = false)
    private Long dealerId;
=======
    private Integer salepriceId;

    // --- Mối quan hệ với Dealer ---
    @Column(name = "dealer_id", nullable = false)
    private Integer dealerId;
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id", insertable = false, updatable = false)
    private Dealer dealer; 

    // --- Mối quan hệ với VehicleVariant ---
    @Column(name = "variant_id", nullable = false)
<<<<<<< HEAD
    private Long variantId;
=======
    private Integer variantId;
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", insertable = false, updatable = false)
    private VehicleVariant variant;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "effectivedate", nullable = false)
<<<<<<< HEAD
    private LocalDate effectiveDate; // Lưu ý tên cột trong DB là effectivedate

    // Có thể thêm trường isActive/status nếu bạn muốn quản lý giá nào đang áp dụng bằng cách khác
    // @Column(name = "is_active")
    // private Boolean isActive = true;
=======
    private LocalDate effectiveDate;
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
}