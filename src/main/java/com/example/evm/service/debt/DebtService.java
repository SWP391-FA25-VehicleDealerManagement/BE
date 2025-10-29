package com.example.evm.service.debt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.evm.dto.debt.CreateDebtPaymentRequest;
import com.example.evm.entity.debt.Debt;
import com.example.evm.entity.debt.DebtPayment;
import com.example.evm.entity.debt.DebtSchedule;
import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.repository.debt.DebtRepository;
import com.example.evm.repository.debt.DebtScheduleRepository;
import com.example.evm.repository.debt.DebtPaymentRepository;
import com.example.evm.repository.dealer.DealerRepository;
import com.example.evm.repository.customer.CustomerRepository;
import com.example.evm.repository.auth.UserRepository;
import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.customer.Customer;
import com.example.evm.entity.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DebtService {

    // Inject các repository cần thiết để truy cập dữ liệu
    private final DebtRepository debtRepository;
    private final DebtScheduleRepository debtScheduleRepository;
    private final DebtPaymentRepository debtPaymentRepository;
    private final DealerRepository dealerRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    // ================== CÁC HÀM LẤY DỮ LIỆU CƠ BẢN ==================

    // Lấy toàn bộ danh sách khoản nợ
    public List<Debt> getAllDebts() {
        return debtRepository.findAll();
    }

    // 🆕 Lấy danh sách NỢ CỦA DEALER (dealer nợ hãng) - EVM xem
    public List<Debt> getDealerDebts() {
        return debtRepository.findByDebtType("DEALER_DEBT");
    }

    // 🆕 Lấy danh sách NỢ CỦA DEALER theo dealerId (dealer nợ hãng) - EVM xem
    public List<Debt> getDealerDebtsByDealerId(Long dealerId) {
        return debtRepository.findByDebtTypeAndDealerDealerId("DEALER_DEBT", dealerId);
    }

    // 🆕 Lấy danh sách NỢ CỦA CUSTOMER (customer nợ dealer) - Dealer xem
    public List<Debt> getCustomerDebts(Long dealerId) {
        return debtRepository.findByDebtTypeAndDealerDealerId("CUSTOMER_DEBT", dealerId);
    }

    // Lấy danh sách nợ theo dealer (deprecated - dùng getDealerDebtsByDealerId hoặc getCustomerDebts)
    @Deprecated
    public List<Debt> getDebtsByDealer(Long dealerId) {
        return debtRepository.findByDealerDealerId(dealerId);
    }

    // Lấy danh sách nợ theo khách hàng
    public List<Debt> getDebtsByCustomer(Long customerId) {
        return debtRepository.findByCustomerCustomerId(customerId);
    }

    // Lấy danh sách nợ theo user phụ trách
    public List<Debt> getDebtsByUser(Long userId) {
        return debtRepository.findByUserUserId(userId);
    }

    // Lấy danh sách nợ theo trạng thái (ACTIVE, OVERDUE, PAID,...)
    public List<Debt> getDebtsByStatus(String status) {
        return debtRepository.findByStatus(status);
    }

    // Lấy danh sách nợ quá hạn của 1 dealer
    public List<Debt> getOverdueDebts(Long dealerId) {
        return debtRepository.findOverdueDebtsByDealer(dealerId, LocalDateTime.now());
    }

    // Lấy chi tiết nợ theo ID, ném exception nếu không tồn tại
    public Debt getDebtById(Long id) {
        return debtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Debt not found with id: " + id));
    }

    // Lấy danh sách các kỳ thanh toán (schedule) theo debtId
    public List<DebtSchedule> getDebtSchedules(Long debtId) {
        return debtScheduleRepository.findByDebtOrderByPeriodNo(debtId);
    }

    // Lấy thông tin chi tiết về debt schedule với thống kê
    public Map<String, Object> getDebtScheduleDetails(Long debtId) {
        Map<String, Object> details = new HashMap<>();
        
        // Lấy debt info
        Debt debt = getDebtById(debtId);
        details.put("debt", debt);
        
        // Lấy schedules
        List<DebtSchedule> schedules = getDebtSchedules(debtId);
        details.put("schedules", schedules);
        
        // Thống kê schedules
        long totalSchedules = schedules.size();
        long paidSchedules = schedules.stream().filter(s -> "PAID".equals(s.getStatus())).count();
        long partialSchedules = schedules.stream().filter(s -> "PARTIAL".equals(s.getStatus())).count();
        long pendingSchedules = schedules.stream().filter(s -> "PENDING".equals(s.getStatus())).count();
        
        details.put("scheduleStats", Map.of(
            "total", totalSchedules,
            "paid", paidSchedules,
            "partial", partialSchedules,
            "pending", pendingSchedules
        ));
        
        // Lấy payments
        List<DebtPayment> payments = getDebtPayments(debtId);
        details.put("payments", payments);
        
        return details;
    }

    // Lấy danh sách các khoản thanh toán (payment) theo debtId
    public List<DebtPayment> getDebtPayments(Long debtId) {
        return debtPaymentRepository.findByDebtDebtId(debtId);
    }

    // ================== TẠO MỚI MỘT KHOẢN NỢ ==================

    @Transactional
    public Debt createDebt(Debt debt) {
        // ✅ Để database tự động tạo ID (IDENTITY strategy)
        debt.setDebtId(null);  // Đảm bảo ID = null để DB tự generate
        
        // Kiểm tra các entity liên quan có tồn tại không
        Dealer dealer = dealerRepository.findById(debt.getDealer().getDealerId())
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));

        Customer customer = debt.getCustomer() != null 
                ? customerRepository.findById(debt.getCustomer().getCustomerId())
                        .orElseThrow(() -> new ResourceNotFoundException("Customer not found"))
                : null;

        User user = debt.getUser() != null 
                ? userRepository.findById(debt.getUser().getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found")) 
                : null;

        // Gán lại các đối tượng sau khi xác thực
        debt.setDealer(dealer);
        debt.setCustomer(customer);
        debt.setUser(user);
        debt.setCreatedDate(LocalDateTime.now());

        // ✅ TỰ ĐỘNG SINH LỊCH TRẢ NỢ nếu có schedules
        if (debt.getDebtSchedules().isEmpty() 
            && debt.getAmountDue() != null 
            && debt.getAmountDue().compareTo(BigDecimal.ZERO) > 0) {
            
            log.info("🔄 Auto-generating debt schedule...");
            generateDebtSchedule(debt);
        }

        // Lưu vào DB
        Debt savedDebt = debtRepository.save(debt);

        log.info("✅ Debt created: ID {} - Amount: {} - Type: {}",
                savedDebt.getDebtId(), 
                debt.getAmountDue(), 
                debt.getDebtType());

        return savedDebt;
    }

    // ================== HÀM TỰ ĐỘNG SINH LỊCH TRẢ NỢ ==================

    private void generateDebtSchedule(Debt debt) {
        // Mặc định chia đều 12 tháng
        int numberOfPeriods = 12;
        BigDecimal amount = debt.getAmountDue();
        BigDecimal interestRate = debt.getInterestRate() != null ? debt.getInterestRate() : BigDecimal.ZERO;

        // Tính toán theo công thức trả góp (công thức annuity)
        double rate = interestRate.doubleValue() / 100 / 12;
        double amountDouble = amount.doubleValue();
        
        double monthlyPaymentDouble;
        if (rate == 0) {
            // Nếu không có lãi suất, chia đều số tiền
            monthlyPaymentDouble = amountDouble / numberOfPeriods;
        } else {
            monthlyPaymentDouble = amountDouble * (rate * Math.pow(1 + rate, numberOfPeriods))
                    / (Math.pow(1 + rate, numberOfPeriods) - 1);
        }

        // Kiểm tra kết quả hợp lệ
        if (Double.isNaN(monthlyPaymentDouble) || Double.isInfinite(monthlyPaymentDouble)) {
            monthlyPaymentDouble = amountDouble / numberOfPeriods; // Fallback: chia đều
        }

        BigDecimal monthlyPayment = BigDecimal.valueOf(monthlyPaymentDouble);
        BigDecimal remainingBalance = amount;

        // Tạo từng kỳ trả nợ
        for (int i = 1; i <= numberOfPeriods; i++) {
            DebtSchedule schedule = new DebtSchedule();
            schedule.setPeriodNo((long) i);
            schedule.setStartBalance(remainingBalance);

            BigDecimal interest = remainingBalance.multiply(BigDecimal.valueOf(rate));
            BigDecimal principal = monthlyPayment.subtract(interest);

            schedule.setPrincipal(principal);
            schedule.setInterest(interest);
            schedule.setInstallment(monthlyPayment);
            schedule.setEndBalance(remainingBalance.subtract(principal));
            schedule.setDueDate(LocalDate.now().plusMonths(i));
            schedule.setStatus("PENDING");

            debt.addDebtSchedule(schedule);
            remainingBalance = schedule.getEndBalance();
        }
    }

    // ================== XỬ LÝ THANH TOÁN ==================
    
    /**
     * ✅ Tự động cập nhật debt status dựa trên tổng số tiền đã thanh toán
     */
    @Transactional
    public void updateDebtStatus(Long debtId) {
        Debt debt = getDebtById(debtId);
        
        // Tính tổng số tiền đã thanh toán (CONFIRMED payments only)
        BigDecimal totalPaid = debtPaymentRepository.findByDebtDebtIdAndStatus(debtId, "CONFIRMED")
                .stream()
                .map(DebtPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Cập nhật amount_paid
        debt.setAmountPaid(totalPaid);
        
        // Cập nhật status
        if (totalPaid.compareTo(debt.getAmountDue()) >= 0) {
            debt.setStatus("PAID");
            log.info("🎉 Debt {} auto-updated to PAID! Total paid: {} / {}", debtId, totalPaid, debt.getAmountDue());
        } else if (debt.getDueDate() != null && LocalDateTime.now().isAfter(debt.getDueDate())) {
            debt.setStatus("OVERDUE");
            log.info("⚠️ Debt {} auto-updated to OVERDUE! Due date passed", debtId);
        } else {
            debt.setStatus("ACTIVE");
            log.info("💰 Debt {} auto-updated to ACTIVE. Paid: {} / {}", debtId, totalPaid, debt.getAmountDue());
        }
        
        debt.setUpdatedDate(LocalDateTime.now());
        debtRepository.save(debt);
    }

    /**
     * ✅ Tạo thanh toán nợ - KHÔNG CẦN ORDER_ID
     * Thanh toán độc lập với đơn hàng, chỉ gắn liền với Debt
     */
    @Transactional
    public DebtPayment makePayment(Long debtId, CreateDebtPaymentRequest request) {
        // 1. Lấy khoản nợ cần thanh toán
        Debt debt = getDebtById(debtId);

        // 2. Validate: Kiểm tra số tiền thanh toán không vượt quá số tiền còn nợ
        BigDecimal currentPaid = debt.getAmountPaid() != null ? debt.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal remainingAmount = debt.getAmountDue().subtract(currentPaid);
        
        if (request.getAmount().compareTo(remainingAmount) > 0) {
            throw new IllegalArgumentException(
                String.format("Số tiền thanh toán (%,.0f) vượt quá số tiền còn nợ (%,.0f)", 
                    request.getAmount().doubleValue(), remainingAmount.doubleValue())
            );
        }

        // 3. Tạo DebtPayment entity
        DebtPayment payment = new DebtPayment();
        payment.setDebt(debt);
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentDate(LocalDateTime.now());
        
        // Generate referenceNumber nếu null
        if (request.getReferenceNumber() == null || request.getReferenceNumber().trim().isEmpty()) {
            String refNumber = String.format("VFT-%d-%s", 
                debtId, 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
            );
            payment.setReferenceNumber(refNumber);
        } else {
            payment.setReferenceNumber(request.getReferenceNumber());
        }
        
        payment.setNotes(request.getNotes());
        payment.setCreatedBy(request.getCreatedBy());
        payment.setStatus("PENDING"); // Chờ EVM xác nhận

        // 4. Nếu có scheduleId (thanh toán cho 1 kỳ cụ thể), gắn vào payment
        if (request.getScheduleId() != null) {
            DebtSchedule schedule = debtScheduleRepository.findById(request.getScheduleId())
                    .orElseThrow(() -> new ResourceNotFoundException("DebtSchedule not found with id: " + request.getScheduleId()));
            
            // Validate: schedule phải thuộc về debt này
            if (!schedule.getDebt().getDebtId().equals(debtId)) {
                throw new IllegalArgumentException("Schedule không thuộc về debt này");
            }
            
            payment.setDebtSchedule(schedule);
            
            // Update schedule status nếu đã đủ tiền
            if (payment.getAmount().compareTo(schedule.getInstallment()) >= 0) {
                schedule.setStatus("PAID");
                debtScheduleRepository.save(schedule);
            }
        }

        // 5. Lưu payment
        DebtPayment savedPayment = debtPaymentRepository.save(payment);
        
        // ⚠️ KHÔNG cập nhật amount_paid ngay, chờ EVM xác nhận mới cộng tiền
        // Chỉ cập nhật khi status = CONFIRMED

        log.info("✅ Payment created (PENDING): Debt {} - Amount: {} - Method: {} - Reference: {}", 
                debtId, request.getAmount(), request.getPaymentMethod(), savedPayment.getReferenceNumber());
        
        return savedPayment;
    }

    // ================== XÁC NHẬN/TỪ CHỐI THANH TOÁN ==================

    /**
     * ✅ EVM Staff xác nhận thanh toán
     * Khi xác nhận: cập nhật status = CONFIRMED, cộng tiền vào amount_paid
     */
    @Transactional
    public DebtPayment confirmPayment(Long debtId, Long paymentId, String confirmedBy) {
        // 1. Lấy payment
        DebtPayment payment = debtPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        
        // 2. Validate payment thuộc về debt này
        if (!payment.getDebt().getDebtId().equals(debtId)) {
            throw new IllegalArgumentException("Payment does not belong to this debt");
        }
        
        // 3. Validate status phải là PENDING
        if (!"PENDING".equals(payment.getStatus())) {
            throw new IllegalArgumentException("Payment is not pending confirmation. Current status: " + payment.getStatus());
        }
        
        // 4. Cập nhật payment status
        payment.setStatus("CONFIRMED");
        payment.setConfirmedBy(confirmedBy);
        payment.setConfirmedDate(LocalDateTime.now());
        debtPaymentRepository.save(payment);
        
        // 5. Cập nhật DebtSchedule nếu có
        if (payment.getDebtSchedule() != null) {
            DebtSchedule schedule = payment.getDebtSchedule();
            BigDecimal currentPaidAmount = schedule.getPaidAmount() != null ? schedule.getPaidAmount() : BigDecimal.ZERO;
            BigDecimal newPaidAmount = currentPaidAmount.add(payment.getAmount());
            schedule.setPaidAmount(newPaidAmount);
            
            // Cập nhật status của schedule
            if (newPaidAmount.compareTo(schedule.getInstallment()) >= 0) {
                schedule.setStatus("PAID");
                schedule.setPaymentDate(LocalDate.now());
                log.info("✅ Schedule {} is now PAID! Paid: {} / {} (remaining: 0)", 
                    schedule.getScheduleId(), newPaidAmount, schedule.getInstallment());
            } else {
                schedule.setStatus("PARTIAL");
                BigDecimal remaining = schedule.getInstallment().subtract(newPaidAmount);
                log.info("💰 Schedule {} updated: {} / {} (remaining: {})", 
                    schedule.getScheduleId(), newPaidAmount, schedule.getInstallment(), remaining);
            }
            
            debtScheduleRepository.save(schedule);
        }
        
        // 6. Cập nhật số tiền đã thanh toán vào Debt
        Debt debt = getDebtById(debtId);
        
        // Tính lại tổng số tiền đã thanh toán từ tất cả payments CONFIRMED
        BigDecimal totalPaidFromPayments = debtPaymentRepository.findByDebtDebtIdAndStatus(debtId, "CONFIRMED")
                .stream()
                .map(DebtPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        debt.setAmountPaid(totalPaidFromPayments);
        
        // 7. Update status của Debt nếu đã thanh toán đủ
        if (totalPaidFromPayments.compareTo(debt.getAmountDue()) >= 0) {
            debt.setStatus("PAID");
            log.info("🎉 Debt {} is now PAID! Total paid: {} / {} (remaining: 0)", 
                debtId, totalPaidFromPayments, debt.getAmountDue());
        } else {
            BigDecimal remaining = debt.getAmountDue().subtract(totalPaidFromPayments);
            log.info("💰 Debt {} payment updated: {} / {} (remaining: {})", 
                debtId, totalPaidFromPayments, debt.getAmountDue(), remaining);
        }
        
        debt.setUpdatedDate(LocalDateTime.now());
        debtRepository.save(debt);
        
        log.info("✅ Payment CONFIRMED: Payment {} - Debt {} - Amount: {} - By: {}", 
                paymentId, debtId, payment.getAmount(), confirmedBy);
        
        return payment;
    }

    /**
     * ✅ EVM Staff từ chối thanh toán
     * Khi từ chối: cập nhật status = REJECTED
     */
    @Transactional
    public DebtPayment rejectPayment(Long debtId, Long paymentId, String rejectedBy, String reason) {
        // 1. Lấy payment
        DebtPayment payment = debtPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        
        // 2. Validate payment thuộc về debt này
        if (!payment.getDebt().getDebtId().equals(debtId)) {
            throw new IllegalArgumentException("Payment does not belong to this debt");
        }
        
        // 3. Validate status phải là PENDING
        if (!"PENDING".equals(payment.getStatus())) {
            throw new IllegalArgumentException("Payment is not pending confirmation. Current status: " + payment.getStatus());
        }
        
        // 4. Cập nhật payment status
        payment.setStatus("REJECTED");
        payment.setConfirmedBy(rejectedBy); // Dùng confirmedBy để lưu người từ chối
        payment.setConfirmedDate(LocalDateTime.now());
        payment.setRejectionReason(reason);
        debtPaymentRepository.save(payment);
        
        log.info("❌ Payment REJECTED: Payment {} - Debt {} - Reason: {}", 
                paymentId, debtId, reason);
        
        return payment;
    }

    // ================== CẬP NHẬT TRẠNG THÁI NỢ ==================

    @Transactional
    public Debt updateDebtStatus(Long id, String status, String notes) {
        Debt debt = getDebtById(id);
        debt.setStatus(status);
        if (notes != null) {
            debt.setNotes(notes);
        }
        debt.setUpdatedDate(LocalDateTime.now());

        Debt updatedDebt = debtRepository.save(debt);
        log.info("Debt {} status updated to: {}", id, status);
        return updatedDebt;
    }

    // ================== THỐNG KÊ ==================

    // Tổng nợ chưa thanh toán của dealer
    public Double getTotalOutstandingByDealer(Long dealerId) {
        Double total = debtRepository.getTotalOutstandingByDealer(dealerId);
        return total != null ? total : 0.0;
    }

    // Tổng nợ chưa thanh toán của khách hàng
    public Double getTotalOutstandingByCustomer(Long customerId) {
        Double total = debtRepository.getTotalOutstandingByCustomer(customerId);
        return total != null ? total : 0.0;
    }

    // Thống kê tổng hợp nợ của dealer
    public Map<String, Object> getDebtStats(Long dealerId) {
        Map<String, Object> stats = new HashMap<>();

        // Lấy số lượng nợ theo trạng thái
        List<Object[]> statusCounts = debtRepository.getDebtStatsByDealer(dealerId);
        Map<String, Long> statusMap = new HashMap<>();

        for (Object[] data : statusCounts) {
            statusMap.put((String) data[0], (Long) data[1]);
        }

        stats.put("byStatus", statusMap);
        stats.put("totalDebts", debtRepository.count());
        stats.put("totalOutstanding", getTotalOutstandingByDealer(dealerId));
        stats.put("totalPaid",
                debtRepository.getTotalOutstandingByDealer(dealerId) != null
                        ? debtRepository.getTotalOutstandingByDealer(dealerId)
                        : 0.0);

        return stats;
    }

    // ================== XÓA NỢ ==================

    @Transactional
    public void deleteDebt(Long id) {
        Debt debt = getDebtById(id);

        // Xóa các khoản thanh toán liên quan
        List<DebtPayment> payments = debtPaymentRepository.findByDebtDebtId(id);
        debtPaymentRepository.deleteAll(payments);

        // Xóa các lịch trả nợ liên quan
        List<DebtSchedule> schedules = debtScheduleRepository.findByDebtDebtId(id);
        debtScheduleRepository.deleteAll(schedules);

        // Xóa chính khoản nợ
        debtRepository.delete(debt);
        log.info("Debt deleted: {}", id);
    }

    // ================== LẤY LỊCH TRẢ NỢ QUÁ HẠN ==================

    public List<DebtSchedule> getOverdueSchedules(Long dealerId) {
        return debtScheduleRepository.findOverdueSchedulesByDealer(dealerId, LocalDate.now());
    }
}
