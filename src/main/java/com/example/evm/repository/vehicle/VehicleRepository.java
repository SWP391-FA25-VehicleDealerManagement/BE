package com.example.evm.repository.vehicle;

import com.example.evm.dto.report.DealerInventoryReportDto;
import com.example.evm.dto.report.ManufacturerInventoryReportDto;
import com.example.evm.entity.vehicle.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository VehicleRepository - Query tối ưu với JOIN FETCH
 * 
 * Cấu trúc mới:
 * - Vehicle → Variant → Model
 * - Vehicle → Variant → VehicleDetail
 * - Vehicle → ManufacturerStock hoặc InventoryStock
 */
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    boolean existsByVinNumber(String vinNumber);
    Optional<Vehicle> findByVinNumber(String vinNumber);

    // ===== QUERY TỐI ƯU VỚI FULL INFO (LEFT JOIN FETCH) =====
    
    /**
     * Lấy 1 xe kèm full info (Variant + Model + VehicleDetail)
     * Sử dụng LEFT JOIN FETCH để tránh N+1 query problem
     */
    @Query("SELECT DISTINCT v FROM Vehicle v " +
           "LEFT JOIN FETCH v.variant vr " +
           "LEFT JOIN FETCH vr.model m " +
           "LEFT JOIN FETCH vr.detail d " +
           "WHERE v.vehicleId = :id")
    Optional<Vehicle> findByIdWithFullInfo(@Param("id") Long id);

    /**
     * Lấy danh sách xe trong kho tổng (kèm full info)
     */
    @Query("SELECT DISTINCT v FROM Vehicle v " +
           "LEFT JOIN FETCH v.variant vr " +
           "LEFT JOIN FETCH vr.model m " +
           "LEFT JOIN FETCH vr.detail d " +
           "WHERE v.manufacturerStock.manufacturerStockId = :stockId")
    List<Vehicle> findByManufacturerStockWithFullInfo(@Param("stockId") Long stockId);

    /**
     * Lấy tất cả xe ở kho tổng (bất kỳ kho nào)
     */
    @Query("SELECT DISTINCT v FROM Vehicle v " +
           "LEFT JOIN FETCH v.variant vr " +
           "LEFT JOIN FETCH vr.model m " +
           "LEFT JOIN FETCH vr.detail d " +
           "WHERE v.manufacturerStock IS NOT NULL")
    List<Vehicle> findAllInManufacturerStockWithFullInfo();

    /**
     * Lấy danh sách xe của dealer (kèm full info)
     */
    @Query("SELECT DISTINCT v FROM Vehicle v " +
           "LEFT JOIN FETCH v.variant vr " +
           "LEFT JOIN FETCH vr.model m " +
           "LEFT JOIN FETCH vr.detail d " +
           "WHERE v.inventoryStock.dealer.dealerId = :dealerId")
    List<Vehicle> findByDealerIdWithFullInfo(@Param("dealerId") Long dealerId);

    /**
     * Lấy tất cả xe có status = TEST_DRIVE (kèm full info)
     */
    @Query("SELECT DISTINCT v FROM Vehicle v " +
           "LEFT JOIN FETCH v.variant vr " +
           "LEFT JOIN FETCH vr.model m " +
           "LEFT JOIN FETCH vr.detail d " +
           "WHERE UPPER(v.status) = 'TEST_DRIVE'")
    List<Vehicle> findByStatusTestDriveWithFullInfo();

    /**
     * Lấy xe có status = TEST_DRIVE theo dealer (kèm full info)
     */
    @Query("SELECT DISTINCT v FROM Vehicle v " +
           "LEFT JOIN FETCH v.variant vr " +
           "LEFT JOIN FETCH vr.model m " +
           "LEFT JOIN FETCH vr.detail d " +
           "WHERE UPPER(v.status) = 'TEST_DRIVE' " +
           "AND v.inventoryStock.dealer.dealerId = :dealerId")
    List<Vehicle> findByStatusTestDriveAndDealerIdWithFullInfo(@Param("dealerId") Long dealerId);

    // ===== QUERY TỔNG HỢP (GROUP BY) =====
    
    /**
     * Đếm số xe trong kho tổng (GROUP BY variant + color)
     * Dùng cho hiển thị tổng hợp kho
     */
    @Query("SELECT v.variant.variantId, " +
           "       v.variant.name, " +
           "       v.variant.model.name, " +
           "       v.color, " +
           "       COUNT(v) " +
           "FROM Vehicle v " +
           "WHERE v.manufacturerStock IS NOT NULL " +
           "GROUP BY v.variant.variantId, v.variant.name, v.variant.model.name, v.color " +
           "ORDER BY v.variant.name, v.color")
    List<Object[]> countManufacturerStockByVariantColor();

    /**
     * Đếm số xe của dealer (GROUP BY variant + color)
     */
    @Query("SELECT v.variant.variantId, " +
           "       v.variant.name, " +
           "       v.variant.model.name, " +
           "       v.color, " +
           "       COUNT(v) " +
           "FROM Vehicle v " +
           "WHERE v.inventoryStock.dealer.dealerId = :dealerId " +
           "GROUP BY v.variant.variantId, v.variant.name, v.variant.model.name, v.color " +
           "ORDER BY v.variant.name, v.color")
    List<Object[]> countDealerStockByVariantColor(@Param("dealerId") Long dealerId);

    // ===== QUERY ĐƠN GIẢN =====
    
    /**
     * Lấy xe theo VIN (không cần full info)
     */
    List<Vehicle> findByVariantVariantId(Long variantId);
    
    /**
     * Lấy xe trong kho tổng (không cần full info)
     */
    List<Vehicle> findByManufacturerStockIsNotNull();
    
    /**
     * Lấy xe đã bán (không còn trong kho)
     */
    @Query("SELECT v FROM Vehicle v WHERE v.manufacturerStock IS NULL AND v.inventoryStock IS NULL")
    List<Vehicle> findSoldVehicles();
    
    /**
     * Tìm xe theo variant và color trong kho tổng
     */
    @Query("SELECT v FROM Vehicle v " +
           "WHERE v.variant.variantId = :variantId " +
           "AND v.color = :color " +
           "AND v.manufacturerStock IS NOT NULL " +
           "AND v.status = 'IN_MANUFACTURER_STOCK'")
    List<Vehicle> findAvailableInManufacturerStock(@Param("variantId") Long variantId, 
                                                     @Param("color") String color);
    
    /**
     * Đếm số xe available trong kho tổng theo variant + color
     */
    @Query("SELECT COUNT(v) FROM Vehicle v " +
           "WHERE v.variant.variantId = :variantId " +
           "AND v.color = :color " +
           "AND v.manufacturerStock IS NOT NULL " +
           "AND v.status = 'IN_MANUFACTURER_STOCK'")
    Long countAvailableInManufacturerStock(@Param("variantId") Long variantId, 
                                           @Param("color") String color);
                                        
    boolean existsByVariantVariantId(Long variantId); 

    //  Báo cáo tồn kho theo đại lý
    @Query("""
        SELECT new com.example.evm.dto.report.DealerInventoryReportDto(
            d.dealerId,
            d.dealerName,
            d.phone,
            d.address,
            COUNT(v.vehicleId),
            SUM(CASE WHEN v.status = 'IN_DEALER_STOCK' THEN 1 ELSE 0 END),
            SUM(CASE WHEN v.status = 'SOLD' THEN 1 ELSE 0 END)
        )
        FROM Vehicle v
        JOIN v.inventoryStock.dealer d
        GROUP BY d.dealerId, d.dealerName, d.phone, d.address
        ORDER BY COUNT(v.vehicleId) DESC
    """)
    List<DealerInventoryReportDto> getDealerInventoryReport();

    //  Báo cáo tồn kho ở kho của hãng sản xuất
     @Query("""
       SELECT new com.example.evm.dto.report.ManufacturerInventoryReportDto(
              ms.manufacturerStockId,
              ms.warehouseName,
              ms.location,
              COUNT(v.vehicleId),
              SUM(CASE WHEN v.status = 'IN_MANUFACTURER_STOCK' THEN 1 ELSE 0 END),
              SUM(CASE WHEN v.status = 'SOLD' THEN 1 ELSE 0 END)
       )
       FROM Vehicle v
       JOIN v.manufacturerStock ms
       GROUP BY ms.manufacturerStockId, ms.warehouseName, ms.location
       ORDER BY COUNT(v.vehicleId) DESC
       """)
       List<ManufacturerInventoryReportDto> getManufacturerInventoryReport();
}