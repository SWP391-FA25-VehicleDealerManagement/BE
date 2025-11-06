package com.example.evm.repository.order;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.example.evm.dto.report.DealerSalesReportDto;
import com.example.evm.dto.report.DealerTurnoverReportDto;
import com.example.evm.dto.report.SalesByStaffDto;
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

    // 📊 Báo cáo doanh số theo đại lý
    @Query("""
        SELECT new com.example.evm.dto.report.DealerSalesReportDto(
            o.dealer.dealerId,
            o.dealer.dealerName,
            o.dealer.phone,
            o.dealer.address,
            COUNT(o.orderId),
            SUM(o.totalPrice)
        )
        FROM Order o
        WHERE o.status IN ('SHIPPED', 'COMPLETED')
        GROUP BY o.dealer.dealerId, o.dealer.dealerName, o.dealer.phone, o.dealer.address
        ORDER BY SUM(o.totalPrice) DESC
    """)
    List<DealerSalesReportDto> getDealerSalesReport();

    // 📈 Báo cáo doanh số theo nhân viên (dealer side)
    @Query("""
        SELECT new com.example.evm.dto.report.SalesByStaffDto(
            o.user.userId,
            o.user.userName,
            o.user.fullName,
            o.user.phone,
            o.user.email,
            o.user.role,
            o.dealer.dealerName,
            COUNT(o.orderId),
            SUM(o.totalPrice)
        )
        FROM Order o
        WHERE o.dealer.dealerId = :dealerId
          AND o.status IN ('SHIPPED', 'COMPLETED')
        GROUP BY o.user.userId, o.user.userName, o.user.fullName, o.user.phone, o.user.email, o.user.role, o.dealer.dealerName
        ORDER BY SUM(o.totalPrice) DESC
    """)
    List<SalesByStaffDto> getSalesByStaff(@Param("dealerId") Long dealerId);

    // 📉 Tốc độ tiêu thụ
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
