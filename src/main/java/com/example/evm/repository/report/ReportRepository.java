package com.example.evm.repository.report;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Repository
public class ReportRepository {

    private final EntityManager entityManager;

    public ReportRepository(EntityManager entityManager) { 
        this.entityManager = entityManager;
    }

    // Gán kết quả trả về là Map.class cho native query
    private List<Map<String, Object>> getNativeQueryResult(String sql) {
        Query query = entityManager.createNativeQuery(sql, Map.class);
        return query.getResultList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMonthlyRevenue() {
        String sql = """
            SELECT 
                YEAR(o.createddate) AS year,
                MONTH(o.createddate) AS month,
                SUM(o.total_price) AS totalRevenue
            FROM [Order] o
            WHERE o.status = 'COMPLETED'
            GROUP BY YEAR(o.createddate), MONTH(o.createddate)
            ORDER BY year, month
        """;
        return getNativeQueryResult(sql);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDealerPerformance() {
        String sql = """
            SELECT 
                d.dealerName AS dealer,
                SUM(o.total_price) AS totalRevenue,
                COUNT(o.order_id) AS totalOrders
            FROM [Order] o
            JOIN Dealer d ON o.dealer_id = d.dealer_id
            WHERE o.status = 'COMPLETED'
            GROUP BY d.dealerName
            ORDER BY totalRevenue DESC
        """;
        return getNativeQueryResult(sql);
    }
    
    // Xe bán chạy nhất
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopSellingVehicles() {
        String sql = """
            SELECT TOP 5 
                v.name AS vehicleName,
                SUM(od.quantity) AS totalSold
            FROM OrderDetail od
            JOIN Vehicle v ON od.vehicle_id = v.vehicle_id
            JOIN [Order] o ON od.order_id = o.order_id
            WHERE o.status = 'COMPLETED'
            GROUP BY v.name
            ORDER BY totalSold DESC
        """;
        return getNativeQueryResult(sql);
    }

    // Báo cáo tồn kho
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getInventoryStatus() {
        String sql = """
            SELECT 
                d.dealerName,
                v.name AS vehicle,
                i.quantity AS stockQuantity
            FROM InventoryStock i
            JOIN Dealer d ON i.dealer_id = d.dealer_id
            JOIN Vehicle v ON i.vehicle_id = v.vehicle_id
            ORDER BY d.dealerName, v.name
        """;
        return getNativeQueryResult(sql);
    }

    // Báo cáo công nợ
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDebtSummary() {
        String sql = """
            SELECT 
                d.dealerName,
                SUM(debt.amount_due - debt.amount_paid) AS totalDebt
            FROM Debt debt
            JOIN Dealer d ON debt.dealer_id = d.dealer_id
            GROUP BY d.dealerName
            ORDER BY totalDebt DESC
        """;
        return getNativeQueryResult(sql);
    }
}