package com.example.evm.repository.debt;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.evm.entity.debt.Debt;

/**
 * Repository cho entity Debt.
 * Cung cấp các phương thức thao tác dữ liệu nợ (Debt) trong database,
 * bao gồm truy vấn mặc định của JPA và các truy vấn tuỳ chỉnh (JPQL).
 */
@Repository
public interface DebtRepository extends JpaRepository<Debt, Long> {

    /**
     *  Lấy tất cả các khoản nợ theo loại (DEALER_DEBT hoặc CUSTOMER_DEBT).
     * @param debtType Loại nợ: DEALER_DEBT hoặc CUSTOMER_DEBT.
     * @return Danh sách các khoản nợ thuộc loại đó.
     */
    List<Debt> findByDebtType(String debtType);

    /**
     *  Lấy danh sách nợ theo loại và dealer cụ thể.
     * @param debtType Loại nợ.
     * @param dealerId ID của đại lý.
     * @return Danh sách các khoản nợ.
     */
    List<Debt> findByDebtTypeAndDealerDealerId(String debtType, Long dealerId);

    /**
     *  Lấy tất cả các khoản nợ của một đại lý (dealer) cụ thể.
     * @param dealerId ID của đại lý.
     * @return Danh sách các khoản nợ thuộc về đại lý đó.
     */
    List<Debt> findByDealerDealerId(Long dealerId);

    /**
     *  Lấy tất cả các khoản nợ của một khách hàng cụ thể.
     * @param customerId ID của khách hàng.
     * @return Danh sách nợ của khách hàng đó.
     */
    List<Debt> findByCustomerCustomerId(Long customerId);

    /**
     *  Lấy tất cả các khoản nợ được tạo bởi một người dùng cụ thể.
     * @param userId ID của người dùng (user).
     * @return Danh sách các khoản nợ do người đó tạo.
     */
    List<Debt> findByUserUserId(Long userId);

    /**
     *  Lấy danh sách nợ theo trạng thái (status) nhất định.
     * @param status Trạng thái nợ (ví dụ: ACTIVE, PAID, OVERDUE).
     * @return Danh sách các khoản nợ có cùng trạng thái.
     */
    List<Debt> findByStatus(String status);

    /**
     *  Lấy các khoản nợ của một đại lý theo trạng thái.
     * @param dealerId ID của đại lý.
     * @param status Trạng thái nợ.
     * @return Danh sách nợ theo đại lý và trạng thái.
     */
    List<Debt> findByDealerDealerIdAndStatus(Long dealerId, String status);
    
    /**
     *  Lấy các khoản nợ theo loại, dealer và trạng thái.
     * @param debtType Loại nợ (DEALER_DEBT hoặc CUSTOMER_DEBT).
     * @param dealerId ID của đại lý.
     * @param status Trạng thái nợ.
     * @return Danh sách nợ theo loại, dealer và trạng thái.
     */
    List<Debt> findByDebtTypeAndDealerDealerIdAndStatus(String debtType, Long dealerId, String status);

    /**
     *  Lấy các khoản nợ của một khách hàng theo trạng thái.
     * @param customerId ID của khách hàng.
     * @param status Trạng thái nợ.
     * @return Danh sách nợ theo khách hàng và trạng thái.
     */
    List<Debt> findByCustomerCustomerIdAndStatus(Long customerId, String status);

    /**
     *  Lấy các khoản nợ của một khách hàng với một dealer cụ thể theo trạng thái.
     * @param customerId ID của khách hàng.
     * @param dealerId ID của đại lý.
     * @param status Trạng thái nợ.
     * @return Danh sách nợ theo khách hàng, dealer và trạng thái.
     */
    List<Debt> findByCustomerCustomerIdAndDealerDealerIdAndStatus(Long customerId, Long dealerId, String status);

    /**
     *  Lấy danh sách nợ quá hạn của một đại lý.
     * Điều kiện: dueDate <= ngày hiện tại và status = 'ACTIVE'.
     * @param dealerId ID của đại lý.
     * @param dueDate Ngày hạn (thường là LocalDateTime.now()).
     * @return Danh sách nợ quá hạn của đại lý đó.
     */
    @Query("SELECT d FROM Debt d WHERE d.dealer.dealerId = :dealerId AND d.dueDate <= :dueDate AND d.status = 'ACTIVE'")
    List<Debt> findOverdueDebtsByDealer(@Param("dealerId") Long dealerId, @Param("dueDate") LocalDateTime dueDate);

    /**
     *  Lấy toàn bộ các khoản nợ quá hạn trong hệ thống (không chỉ riêng đại lý nào).
     * Điều kiện: dueDate <= ngày hiện tại và status = 'ACTIVE'.
     * @param dueDate Ngày hạn.
     * @return Danh sách tất cả nợ quá hạn.
     */
    @Query("SELECT d FROM Debt d WHERE d.dueDate <= :dueDate AND d.status = 'ACTIVE'")
    List<Debt> findAllOverdueDebts(@Param("dueDate") LocalDateTime dueDate);

    /**
     *  Tính tổng số tiền còn nợ của một đại lý.
     * Công thức: SUM(amountDue - amountPaid)
     * Chỉ tính các khoản nợ có trạng thái ACTIVE hoặc OVERDUE.
     * @param dealerId ID của đại lý.
     * @return Tổng số tiền còn nợ (Double), có thể null nếu không có bản ghi.
     */
    @Query("SELECT SUM(d.amountDue - d.amountPaid) FROM Debt d WHERE d.dealer.dealerId = :dealerId AND d.status IN ('ACTIVE', 'OVERDUE')")
    Double getTotalOutstandingByDealer(@Param("dealerId") Long dealerId);

    /**
     *  Tính tổng số tiền còn nợ của một khách hàng.
     * @param customerId ID của khách hàng.
     * @return Tổng số tiền còn nợ (Double).
     */
    @Query("SELECT SUM(d.amountDue - d.amountPaid) FROM Debt d WHERE d.customer.customerId = :customerId AND d.status IN ('ACTIVE', 'OVERDUE')")
    Double getTotalOutstandingByCustomer(@Param("customerId") Long customerId);

    /**
     * 🔢 Đếm số lượng khoản nợ theo trạng thái cho một đại lý cụ thể.
     * @param dealerId ID của đại lý.
     * @param status Trạng thái nợ (ACTIVE, OVERDUE, PAID, v.v.)
     * @return Số lượng nợ tương ứng.
     */
    @Query("SELECT COUNT(d) FROM Debt d WHERE d.dealer.dealerId = :dealerId AND d.status = :status")
    Long countByDealerAndStatus(@Param("dealerId") Long dealerId, @Param("status") String status);

    /**
     *  Lấy thống kê số lượng nợ của đại lý theo từng trạng thái.
     * Kết quả trả về: List<Object[]> — trong đó:
     *  - Object[0]: trạng thái nợ (status)
     *  - Object[1]: số lượng nợ tương ứng
     * @param dealerId ID của đại lý.
     * @return Danh sách [status, count].
     */
    @Query("SELECT d.status, COUNT(d) FROM Debt d WHERE d.dealer.dealerId = :dealerId GROUP BY d.status")
    List<Object[]> getDebtStatsByDealer(@Param("dealerId") Long dealerId);

    /**
     *  Lấy các khoản nợ của một đại lý trong một khoảng thời gian.
     * @param dealerId ID của đại lý.
     * @param startDate Ngày bắt đầu.
     * @param endDate Ngày kết thúc.
     * @return Danh sách nợ trong khoảng thời gian đó.
     */
    @Query("SELECT d FROM Debt d WHERE d.dealer.dealerId = :dealerId AND d.createdDate BETWEEN :startDate AND :endDate")
    List<Debt> findByDealerAndDateRange(
            @Param("dealerId") Long dealerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
