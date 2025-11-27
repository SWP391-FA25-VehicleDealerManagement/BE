package com.example.evm.repository.debt;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.evm.entity.debt.DebtPayment;

/**
 * Repository cho entity DebtPayment.
 * Giao tiếp trực tiếp với database thông qua Spring Data JPA.
 */
@Repository
public interface DebtPaymentRepository extends JpaRepository<DebtPayment, Long> {

    /**
     *  Tìm tất cả các khoản thanh toán (DebtPayment) thuộc về một khoản nợ (Debt) cụ thể.
     * @param debtId ID của khoản nợ.
     * @return Danh sách các DebtPayment tương ứng với Debt ID.
     */
    List<DebtPayment> findByDebtDebtId(Long debtId);

    /**
     *  Tìm tất cả các khoản thanh toán theo lịch thanh toán (Schedule) cụ thể.
     * @param scheduleId ID của lịch thanh toán.
     * @return Danh sách các DebtPayment tương ứng với Schedule ID.
     */
    List<DebtPayment> findByDebtScheduleScheduleId(Long scheduleId);

    /**
     *  Lấy tất cả các khoản thanh toán của một đại lý (dealer) trong khoảng thời gian chỉ định.
     * Sử dụng truy vấn JPQL để join bảng Debt -> Dealer -> DebtPayment.
     *
     * @param dealerId ID của đại lý.
     * @param startDate Ngày bắt đầu khoảng thời gian.
     * @param endDate Ngày kết thúc khoảng thời gian.
     * @return Danh sách DebtPayment trong khoảng thời gian cho đại lý đó.
     */
    @Query("SELECT dp FROM DebtPayment dp WHERE dp.debt.dealer.dealerId = :dealerId AND dp.paymentDate BETWEEN :startDate AND :endDate")
    List<DebtPayment> findByDealerAndDateRange(
            @Param("dealerId") Long dealerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     *  Tính tổng số tiền mà một đại lý đã thanh toán trong khoảng thời gian chỉ định.
     * Dùng hàm SUM trong JPQL để cộng tất cả các `dp.amount`.
     *
     * @param dealerId ID của đại lý.
     * @param startDate Ngày bắt đầu khoảng thời gian.
     * @param endDate Ngày kết thúc khoảng thời gian.
     * @return Tổng số tiền thanh toán (Double), có thể null nếu không có bản ghi.
     */
    @Query("SELECT SUM(dp.amount) FROM DebtPayment dp WHERE dp.debt.dealer.dealerId = :dealerId AND dp.paymentDate BETWEEN :startDate AND :endDate")
    Double getTotalPaymentsByDealerAndPeriod(
            @Param("dealerId") Long dealerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     *  Tìm tất cả các khoản thanh toán theo debt ID và status.
     * @param debtId ID của khoản nợ.
     * @param status Trạng thái thanh toán (PENDING, CONFIRMED, REJECTED).
     * @return Danh sách các DebtPayment tương ứng.
     */
    List<DebtPayment> findByDebtDebtIdAndStatus(Long debtId, String status);
}
