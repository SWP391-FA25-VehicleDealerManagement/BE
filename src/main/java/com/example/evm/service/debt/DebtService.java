package com.example.evm.service.debt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

        // ✅ TỰ ĐỘNG SINH LỊCH TRẢ NỢ nếu payment_type = INSTALLMENT
        if ("INSTALLMENT".equalsIgnoreCase(debt.getPaymentType()) 
            && debt.getDebtSchedules().isEmpty() 
            && debt.getAmountDue() != null 
            && debt.getAmountDue().compareTo(BigDecimal.ZERO) > 0) {
            
            log.info("🔄 Auto-generating debt schedule for INSTALLMENT payment...");
            generateDebtSchedule(debt);
        }

        // Lưu vào DB
        Debt savedDebt = debtRepository.save(debt);

        log.info("✅ Debt created: ID {} - Amount: {} - Type: {}",
                savedDebt.getDebtId(), 
                debt.getAmountDue(), 
                debt.getPaymentType());

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
        double monthlyPaymentDouble = amountDouble * (rate * Math.pow(1 + rate, numberOfPeriods))
                / (Math.pow(1 + rate, numberOfPeriods) - 1);

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
        payment.setReferenceNumber(request.getReferenceNumber());
        payment.setNotes(request.getNotes());
        payment.setCreatedBy(request.getCreatedBy());

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

        // 6. Cập nhật số tiền đã thanh toán vào Debt
        debt.setAmountPaid(currentPaid.add(request.getAmount()));
        
        // 7. Update status của Debt nếu đã thanh toán đủ
        if (debt.getAmountPaid().compareTo(debt.getAmountDue()) >= 0) {
            debt.setStatus("PAID");
        }
        
        debtRepository.save(debt);

        log.info("✅ Payment made: Debt {} - Amount: {} - Method: {}", 
                debtId, request.getAmount(), request.getPaymentMethod());
        
        return savedPayment;
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
