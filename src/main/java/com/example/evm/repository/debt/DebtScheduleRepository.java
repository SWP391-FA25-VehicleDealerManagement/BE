package com.example.evm.repository.debt;  


import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.evm.entity.debt.DebtSchedule;

@Repository  
// Đánh dấu interface này là một Bean của Spring, giúp Spring quét và tự động inject vào Service.
public interface DebtScheduleRepository extends JpaRepository<DebtSchedule, Long> {
    // Kế thừa JpaRepository để có sẵn các phương thức CRUD như save(), findAll(), findById(), deleteById()...
    // Kiểu thực thể là DebtSchedule, khóa chính có kiểu dữ liệu Long.

    // ===================== TRUY VẤN THEO QUAN HỆ =====================

    List<DebtSchedule> findByDebtDebtId(Long debtId);
    // Tìm tất cả các lịch trả nợ (DebtSchedule) thuộc về một khoản nợ (Debt) cụ thể, dựa trên debtId.

    List<DebtSchedule> findByStatus(String status);
    // Lấy danh sách các lịch trả nợ theo trạng thái, ví dụ: "PENDING", "PAID", "PARTIAL"...

    // ===================== TRUY VẤN TÙY CHỈNH (JPQL) =====================

    @Query("SELECT ds FROM DebtSchedule ds WHERE ds.debt.dealer.dealerId = :dealerId AND ds.dueDate <= :dueDate AND ds.status IN ('PENDING', 'PARTIAL')")
    List<DebtSchedule> findOverdueSchedulesByDealer(@Param("dealerId") Long dealerId, @Param("dueDate") LocalDate dueDate);
    // Truy vấn lấy danh sách các kỳ trả nợ đã đến hạn hoặc quá hạn (dueDate <= ngày chỉ định)
    // của một đại lý (dealer) cụ thể, và chỉ lấy các kỳ có trạng thái "PENDING" hoặc "PARTIAL"
    // (tức là chưa thanh toán hoặc mới thanh toán một phần).

    @Query("SELECT ds FROM DebtSchedule ds WHERE ds.dueDate <= :dueDate AND ds.status IN ('PENDING', 'PARTIAL')")
    List<DebtSchedule> findAllOverdueSchedules(@Param("dueDate") LocalDate dueDate);
    // Lấy tất cả các kỳ trả nợ quá hạn trong toàn hệ thống (không giới hạn theo đại lý).

    @Query("SELECT ds FROM DebtSchedule ds WHERE ds.debt.debtId = :debtId ORDER BY ds.periodNo ASC")
    List<DebtSchedule> findByDebtOrderByPeriodNo(@Param("debtId") Long debtId);
    // Lấy tất cả các kỳ thanh toán của một khoản nợ cụ thể, sắp xếp theo thứ tự kỳ (periodNo) tăng dần.
    // Giúp hiển thị lịch trả nợ theo trình tự thời gian.

    List<DebtSchedule> findByDebtDebtIdAndStatus(Long debtId, String status);
    // Lấy các kỳ trả nợ của một khoản nợ (debtId) cụ thể, lọc theo trạng thái.
    // Ví dụ: lấy tất cả các kỳ còn đang "PENDING" cho khoản nợ đó.
}
