package com.example.evm.repository.order;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.example.evm.dto.report.DealerSalesReportDto;
import com.example.evm.dto.report.DealerSalesSummaryResponse;
import com.example.evm.dto.report.DealerTurnoverReportDto;
import com.example.evm.dto.report.SalesByStaffDto;
import com.example.evm.dto.report.StaffSalesReportDto;
import com.example.evm.entity.order.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByDealerDealerId(Long dealerId);
    List<Order> findByCustomerCustomerId(Long customerId);
    List<Order> findByStatus(String status);
    
    @Query("SELECT o FROM Order o WHERE o.dealer.dealerId = :dealerId AND o.status = :status")
    List<Order> findByDealerAndStatus(@Param("dealerId") Long dealerId, @Param("status") String status);
    
    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.dealer.dealerId = :dealerId AND o.status = 'DELIVERED'")
    Double getTotalSalesByDealer(@Param("dealerId") Long dealerId);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.dealer.dealerId = :dealerId AND o.status = :status")
    Long countByDealerAndStatus(@Param("dealerId") Long dealerId, @Param("status") String status);

    // Lấy order ko có contract
    @Query("""
    SELECT o 
    FROM Order o
    WHERE o.dealer.dealerId = :dealerId
      AND o.orderId NOT IN (
          SELECT c.order.orderId FROM VehicleContract c
      )
    """)
    List<Order> findOrdersWithoutContractByDealer(@Param("dealerId") Long dealerId);

    //  Báo cáo doanh số theo nhân viên (dealer side)
    @Query("""
        SELECT new com.example.evm.dto.report.SalesByStaffDto(
            o.user.userId,
            o.user.userName,
            o.user.fullName,
            o.user.phone,
            o.user.email,
            o.user.role,
            o.dealer.dealerName,
            null,
            null,
            COUNT(o.orderId),
            SUM(o.totalPrice)
        )
        FROM Order o
        WHERE o.dealer.dealerId = :dealerId
          AND o.status IN ('SHIPPED', 'COMPLETED')
          AND (:year IS NULL OR YEAR(o.createdDate) = :year)
          AND (:month IS NULL OR MONTH(o.createdDate) = :month)
        GROUP BY 
            o.user.userId, o.user.userName, o.user.fullName, o.user.phone,
            o.user.email, o.user.role, o.dealer.dealerName
        ORDER BY 
            SUM(o.totalPrice) DESC
    """)
    List<SalesByStaffDto> getSalesByStaff(@Param("dealerId") Long dealerId,
                                          @Param("year") Integer year,
                                          @Param("month") Integer month);

    // Báo cáo doanh thu của 1 nhân viên                                      
    @Query("""
        SELECT new com.example.evm.dto.report.StaffSalesReportDto(
            u.userId,
            u.userName,
            u.fullName,
            u.phone,
            u.email,
            u.role,
            d.dealerName,
            CAST(YEAR(o.createdDate) AS integer),
            CAST(MONTH(o.createdDate) AS integer),
            o.orderId,
            o.totalPrice,
            o.createdDate
        )
        FROM Order o
        JOIN o.user u
        JOIN o.dealer d
        WHERE u.userId = :userId
        AND (:year IS NULL OR YEAR(o.createdDate) = :year)
        AND (:month IS NULL OR MONTH(o.createdDate) = :month)
        AND o.status IN ('SHIPPED', 'COMPLETED')
        ORDER BY o.createdDate DESC
    """)
    List<StaffSalesReportDto> getStaffSalesReport(
            @Param("userId") Long userId,
            @Param("year") Integer year,
            @Param("month") Integer month
    );



    // Báo cáo doanh thu của 1 đại lý (dealer side)
    @Query("""
        SELECT new com.example.evm.dto.report.DealerSalesReportDto(
            d.dealerId,
            d.dealerName,
            YEAR(o.createdDate),
            MONTH(o.createdDate),
            SUM(o.totalPrice),
            COUNT(o.orderId)
        )
        FROM Order o JOIN o.dealer d
        WHERE o.dealer.dealerId = :dealerId
        AND (:year IS NULL OR YEAR(o.createdDate) = :year)
        AND (:month IS NULL OR MONTH(o.createdDate) = :month)
        AND o.status IN ('SHIPPED', 'COMPLETED')
        GROUP BY d.dealerId, d.dealerName, YEAR(o.createdDate), MONTH(o.createdDate)
        ORDER BY YEAR(o.createdDate) DESC, MONTH(o.createdDate) DESC
    """)
    List<DealerSalesReportDto> getDealerSalesReport(@Param("dealerId") Long dealerId, 
                                                    @Param("year") Integer year, 
                                                    @Param("month") Integer month);

    // Báo cáo doanh thu của các đại lý (EVM side)                            
    @Query("""
        SELECT new com.example.evm.dto.report.DealerSalesSummaryResponse(
            o.dealer.dealerId,
            o.dealer.dealerName,
            o.dealer.phone,
            o.dealer.address,
            YEAR(o.createdDate),
            MONTH(o.createdDate), 
            COALESCE(SUM(o.totalPrice), 0),
            COUNT(o)
        )    
        FROM Order o
        WHERE (:year IS NULL OR YEAR(o.createdDate) = :year)
            AND (:month IS NULL OR MONTH(o.createdDate) = :month)
            AND o.status IN ('SHIPPED', 'COMPLETED')
        GROUP BY o.dealer.dealerId, o.dealer.dealerName, o.dealer.phone, o.dealer.address, YEAR(o.createdDate), MONTH(o.createdDate)
        ORDER BY
            YEAR(o.createdDate) DESC,
            MONTH(o.createdDate) DESC,
            SUM(o.totalPrice) DESC
    """)
    List<DealerSalesSummaryResponse> getAllDealersSalesSummary(@Param("year") Integer year,
                                                               @Param("month") Integer month
    );
                          

    //  Tốc độ tiêu thụ
    @Query("""
        SELECT new com.example.evm.dto.report.DealerTurnoverReportDto(
            o.dealer.dealerId,
            o.dealer.dealerName,
            o.dealer.phone,
            o.dealer.address,
            COUNT(o.orderId),
            COUNT(o.orderId) * 1.0 / (SELECT COUNT(v.vehicleId) FROM Vehicle v WHERE v.inventoryStock.dealer.dealerId = o.dealer.dealerId)
        )
        FROM Order o
        WHERE o.status IN ('SHIPPED', 'COMPLETED')
        GROUP BY o.dealer.dealerId, o.dealer.dealerName, o.dealer.phone, o.dealer.address
    """)
    List<DealerTurnoverReportDto> getDealerTurnoverReport();

}
