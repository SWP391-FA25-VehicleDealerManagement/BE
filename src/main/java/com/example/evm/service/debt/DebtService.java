package com.example.evm.service.debt;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import com.example.evm.repository.payment.PaymentRepository;
import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.customer.Customer;
import com.example.evm.entity.user.User;
import lombok.RequiredArgsConstructor;

import com.example.evm.entity.payment.Payment   ;
import com.example.evm.entity.order.Order;

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
    private final PaymentRepository paymentRepository;

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

        // Auto-fix rounding nếu cần
        updateDebtStatusWithRounding(savedDebt, savedDebt.getAmountPaid());
        debtRepository.save(savedDebt);

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
        BigDecimal totalAmount = debt.getAmountDue();
        BigDecimal amountPaid = debt.getAmountPaid() != null ? debt.getAmountPaid() : BigDecimal.ZERO;
        
        // ✅ Tính số tiền còn nợ (sau khi đã trả 20% đầu tiên)
        BigDecimal remainingDebt = totalAmount.subtract(amountPaid);
        BigDecimal installment = remainingDebt.divide(BigDecimal.valueOf(numberOfPeriods), 2, RoundingMode.HALF_UP);
    
        BigDecimal remainingBalance = remainingDebt;
    
        // ✅ Nếu đã có amountPaid (20% đầu tiên), cập nhật kỳ đầu tiên
        boolean hasInitialPayment = amountPaid.compareTo(BigDecimal.ZERO) > 0;
    
        // Tạo từng kỳ trả nợ
        for (int i = 1; i <= numberOfPeriods; i++) {
            DebtSchedule schedule = new DebtSchedule();
            schedule.setPeriodNo((long) i);
            schedule.setStartBalance(remainingBalance);
    
            BigDecimal principal;
            if (i == numberOfPeriods) {
                // Kỳ cuối: Điều chỉnh principal để endBalance = 0
                principal = remainingBalance;
            } else {
                principal = installment;
            }
    
            BigDecimal interest = BigDecimal.ZERO;  // Không có lãi
            BigDecimal endBalance = remainingBalance.subtract(principal).setScale(2, RoundingMode.HALF_UP);
    
            schedule.setPrincipal(principal);
            schedule.setInterest(interest);
            schedule.setInstallment(installment);
            schedule.setEndBalance(endBalance);
            schedule.setDueDate(LocalDate.now().plusMonths(i));
            
            // ✅ Kỳ đầu tiên: Nếu đã thanh toán 20%, đánh dấu là PARTIAL hoặc PAID
            if (i == 1 && hasInitialPayment) {
                if (amountPaid.compareTo(installment) >= 0) {
                    schedule.setPaidAmount(installment);
                    schedule.setStatus("PAID");
                    schedule.setPaymentDate(LocalDate.now());
                } else {
                    schedule.setPaidAmount(amountPaid);
                    schedule.setStatus("PARTIAL");
                }
            } else {
                schedule.setStatus("PENDING");
            }
    
            debt.addDebtSchedule(schedule);
            remainingBalance = endBalance;
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
        
        // Cập nhật status với auto-fix rounding
        updateDebtStatusWithRounding(debt, totalPaid);
        
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
        
        // 7. Update status của Debt với auto-fix rounding
        updateDebtStatusWithRounding(debt, totalPaidFromPayments);
        
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

    // ================== TẠO DEBT TỪ PAYMENT ==================

    /**
     * ✅ Tạo CUSTOMER_DEBT từ Payment (customer nợ dealer)
     * Flow: Dealer tạo Order cho customer → Customer thanh toán → Tự động tạo debt
     * 
     * Logic:
     * - Nếu chưa có debt cho order này: Tạo mới với amountDue = Order.totalPrice, amountPaid = payment.amount (20%)
     * - Nếu đã có debt: Cập nhật amountPaid += payment.amount
     */
    @Transactional
    public Debt createDebtFromPayment(Long paymentId) {
        // 1. Lấy Payment
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        
        // 2. Lấy Order từ Payment (Order được tạo bởi Dealer cho customer)
        Order order = payment.getOrder();
        if (order == null) {
            throw new IllegalArgumentException("Payment must be linked to an Order");
        }
        
        // 3. Validate: Chỉ tạo debt cho INSTALLMENT payment
        if (!"INSTALLMENT".equals(payment.getPaymentType())) {
            throw new IllegalArgumentException("Only INSTALLMENT payments can create debt");
        }
        
        // 4. Validate: Order phải có customer (Dealer tạo Order cho customer)
        if (order.getCustomer() == null) {
            throw new IllegalArgumentException("Order must have a customer (created by dealer for customer)");
        }
        
        // 5. Validate: Order phải có dealer
        if (order.getDealer() == null) {
            throw new IllegalArgumentException("Order must have a dealer");
        }
        
        // 6. Kiểm tra xem đã có debt cho order này chưa (tìm theo notes chứa orderId)
        String orderIdStr = "Order: " + order.getOrderId();
        List<Debt> existingDebts = debtRepository.findByCustomerCustomerIdAndDealerDealerIdAndStatus(
                order.getCustomer().getCustomerId(), 
                order.getDealer().getDealerId(), 
                "ACTIVE"
        );
        
        Debt debt = null;
        for (Debt d : existingDebts) {
            if (d.getNotes() != null && d.getNotes().contains(orderIdStr)) {
                debt = d;
                break;
            }
        }
        
        if (debt == null) {
            // 7. Tạo CUSTOMER_DEBT mới (customer nợ dealer)
            debt = new Debt();
            debt.setDebtType("CUSTOMER_DEBT");
            debt.setCustomer(order.getCustomer()); // Customer nợ
            debt.setDealer(order.getDealer()); // Dealer được trả
            
            // ✅ FIX: amountDue = Order.totalPrice (toàn bộ số tiền), amountPaid = payment.amount (20% đã trả)
            BigDecimal orderTotal = order.getTotalPrice() != null ? 
                    BigDecimal.valueOf(order.getTotalPrice()) : payment.getAmount();
            debt.setAmountDue(orderTotal);
            debt.setAmountPaid(payment.getAmount()); // ✅ Đã thanh toán 20%
            
            debt.setPaymentMethod(payment.getPaymentMethod());
            debt.setStatus("ACTIVE");
            debt.setNotes("Auto-generated from Payment: " + paymentId + " - Order: " + order.getOrderId() + 
                         " - Dealer: " + order.getDealer().getDealerName() + " - Customer: " + order.getCustomer().getCustomerName());
            debt.setStartDate(LocalDateTime.now());
            debt.setDueDate(LocalDateTime.now().plusMonths(12)); // 12 tháng trả góp
            debt.setCreatedDate(LocalDateTime.now());
            
            // 8. Tự động tạo lịch trả nợ 12 tháng
            generateDebtSchedule(debt);
            
            // 9. Lưu Debt
            debt = debtRepository.save(debt);
            
            log.info("✅ CUSTOMER_DEBT created: Customer {} nợ Dealer {} - Total: {} - Paid: {} (20%) - Order: {} - Payment: {}", 
                    order.getCustomer().getCustomerName(), order.getDealer().getDealerName(), 
                    orderTotal, payment.getAmount(), order.getOrderId(), paymentId);
        } else {
            // 10. Cập nhật debt đã tồn tại: cộng thêm số tiền đã thanh toán
            BigDecimal currentPaid = debt.getAmountPaid() != null ? debt.getAmountPaid() : BigDecimal.ZERO;
            BigDecimal newPaid = currentPaid.add(payment.getAmount());
            debt.setAmountPaid(newPaid);
            debt.setUpdatedDate(LocalDateTime.now());
            debt = debtRepository.save(debt);
            
            log.info("✅ CUSTOMER_DEBT updated: Payment {} added - New total paid: {} / {} - Order: {}", 
                    payment.getAmount(), newPaid, debt.getAmountDue(), order.getOrderId());
        }
        
        // 11. Auto-fix rounding nếu cần
        updateDebtStatusWithRounding(debt, debt.getAmountPaid());
        debt = debtRepository.save(debt);
        
        return debt;
    }

    /**
     * ✅ Cập nhật status của Debt với auto-fix rounding
     * Tự động xử lý làm tròn khi chênh lệch < 1000đ
     */
    private void updateDebtStatusWithRounding(Debt debt, BigDecimal totalPaid) {
        BigDecimal remainingAmount = debt.getAmountDue().subtract(totalPaid);
        
        // Xử lý làm tròn: nếu chênh lệch < 1000đ thì coi như đã trả đủ
        if (remainingAmount.abs().compareTo(new BigDecimal("1000")) < 0) {
            debt.setStatus("PAID");
            debt.setAmountPaid(debt.getAmountDue()); // Đặt chính xác bằng amountDue
            log.info("🎉 Debt {} auto-fixed to PAID! Total paid: {} / {} (rounded difference: {}đ)", 
                    debt.getDebtId(), debt.getAmountDue(), debt.getAmountDue(), remainingAmount);
        } else if (totalPaid.compareTo(debt.getAmountDue()) >= 0) {
            debt.setStatus("PAID");
            log.info("🎉 Debt {} is now PAID! Total paid: {} / {}", debt.getDebtId(), totalPaid, debt.getAmountDue());
        } else if (debt.getDueDate() != null && LocalDateTime.now().isAfter(debt.getDueDate())) {
            debt.setStatus("OVERDUE");
            log.info("⚠️ Debt {} auto-updated to OVERDUE! Due date passed", debt.getDebtId());
        } else {
            debt.setStatus("ACTIVE");
            log.info("💰 Debt {} payment updated: {} / {} ({} remaining)",
                    debt.getDebtId(), totalPaid, debt.getAmountDue(), remainingAmount);
        }
    }


    /**
     * ✅ Tự động tạo debt khi customer thanh toán (nếu payment_type = INSTALLMENT)
     * Flow: Dealer tạo Order cho customer → Customer thanh toán → Tự động tạo CUSTOMER_DEBT
     */
    @Transactional
    public void autoCreateDebtFromPayment(Long paymentId) {
        try {
            Payment payment = paymentRepository.findById(paymentId).orElse(null);
            if (payment == null) return;
            
            // Chỉ tạo debt cho INSTALLMENT payment
            if ("INSTALLMENT".equals(payment.getPaymentType())) {
                Order order = payment.getOrder();
                if (order != null && order.getCustomer() != null && order.getDealer() != null) {
                    // Tạo CUSTOMER_DEBT (customer nợ dealer)
                    createDebtFromPayment(paymentId);
                    log.info("🔄 Auto-created CUSTOMER_DEBT: Customer {} nợ Dealer {} - Payment: {}", 
                            order.getCustomer().getCustomerName(), order.getDealer().getDealerName(), paymentId);
                } else {
                    log.warn("⚠️ Cannot create CUSTOMER_DEBT: Order missing customer or dealer - Payment: {}", paymentId);
                }
            }
        } catch (Exception e) {
            log.error("Failed to auto-create debt from payment {}: {}", paymentId, e.getMessage());
        }
    }

    // ================== THANH TOÁN TRỰC TIẾP DEBT SCHEDULE ==================

    /**
     * ✅ Thanh toán trực tiếp cho một kỳ trả nợ (DebtSchedule) mà không cần xác nhận
     * Cập nhật paidAmount của schedule và tổng amountPaid của Debt
     */
    @Transactional
    public DebtSchedule payDebtScheduleDirectly(Long scheduleId, BigDecimal amount) {
        // 1. Lấy DebtSchedule
        DebtSchedule schedule = debtScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("DebtSchedule not found with id: " + scheduleId));

        // 2. Cập nhật paidAmount của schedule
        BigDecimal currentPaidAmount = schedule.getPaidAmount() != null ? schedule.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal newPaidAmount = currentPaidAmount.add(amount);
        schedule.setPaidAmount(newPaidAmount);

        // 3. Cập nhật status của schedule
        if (newPaidAmount.compareTo(schedule.getInstallment()) >= 0) {
            schedule.setStatus("PAID");
            schedule.setPaymentDate(LocalDate.now()); // Đặt ngày thanh toán khi đủ
            log.info("✅ Schedule {} is now PAID! Paid: {} / {}",
                    schedule.getScheduleId(), newPaidAmount, schedule.getInstallment());
        } else {
            schedule.setStatus("PARTIAL");
            log.info("💰 Schedule {} updated: {} / {} ({} remaining)",
                    schedule.getScheduleId(), newPaidAmount, schedule.getInstallment(),
                    schedule.getInstallment().subtract(newPaidAmount));
        }
        debtScheduleRepository.save(schedule);

        // 4. Cập nhật Debt chính
        Debt debt = schedule.getDebt();
        if (debt == null) {
            throw new IllegalArgumentException("DebtSchedule is not linked to a Debt");
        }

        // Tính lại tổng số tiền đã thanh toán cho Debt từ tất cả các schedule đã thanh toán
        BigDecimal totalPaidFromSchedules = debtScheduleRepository.findByDebtOrderByPeriodNo(debt.getDebtId())
                .stream()
                .map(s -> s.getPaidAmount() != null ? s.getPaidAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        debt.setAmountPaid(totalPaidFromSchedules);

        // 5. Cập nhật status của Debt với auto-fix rounding
        updateDebtStatusWithRounding(debt, totalPaidFromSchedules);
        debt.setUpdatedDate(LocalDateTime.now());
        debtRepository.save(debt);

        // 4.5. Tạo bản ghi DebtPayment tương ứng để hiển thị ở API GET /debts/{id}/payments
        DebtPayment payment = new DebtPayment();
        payment.setDebt(debt);
        payment.setDebtSchedule(schedule);
        payment.setAmount(amount);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentMethod("DIRECT");
        payment.setReferenceNumber(String.format("DIR-%d-%s",
                debt.getDebtId(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))));
        payment.setNotes("Direct pay via API /debts/schedules/{id}/direct-pay");
        payment.setCreatedBy("system");
        payment.setStatus("CONFIRMED");
        payment.setConfirmedBy("system");
        payment.setConfirmedDate(LocalDateTime.now());
        debtPaymentRepository.save(payment);

        log.info("✅ Direct payment for DebtSchedule {} processed. Amount: {}", scheduleId, amount);
        return schedule;
    }
}
