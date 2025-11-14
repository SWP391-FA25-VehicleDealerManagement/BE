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
import com.example.evm.repository.dealer.DealerRequestRepository;
import com.example.evm.repository.customer.CustomerRepository;
import com.example.evm.repository.auth.UserRepository;
import com.example.evm.repository.payment.PaymentRepository;
import com.example.evm.repository.order.OrderRepository;
import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.dealer.DealerRequest;
import com.example.evm.entity.customer.Customer;
import com.example.evm.entity.user.User;
import lombok.RequiredArgsConstructor;

import com.example.evm.entity.payment.Payment   ;
import com.example.evm.entity.order.Order;

import java.util.Comparator;

@Slf4j
@Service
@RequiredArgsConstructor
public class DebtService {

    // Inject các repository cần thiết để truy cập dữ liệu
    private final DebtRepository debtRepository;
    private final DebtScheduleRepository debtScheduleRepository;
    private final DebtPaymentRepository debtPaymentRepository;
    private final DealerRepository dealerRepository;
    private final DealerRequestRepository dealerRequestRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    // ================== CÁC HÀM LẤY DỮ LIỆU CƠ BẢN ==================

    // Lấy toàn bộ danh sách khoản nợ
    public List<Debt> getAllDebts() {
        return debtRepository.findAll();
    }

    // 🆕 Lấy danh sách NỢ CỦA DEALER (dealer nợ hãng) - EVM xem
    @Transactional
    public List<Debt> getDealerDebts() {
        List<Debt> debts = debtRepository.findByDebtType("DEALER_DEBT");
        // ✅ Tính lại amountPaid cho mỗi debt và PERSIST vào DB
        debts.forEach(debt -> {
            recalculateAmountPaid(debt);
            debt.setUpdatedDate(LocalDateTime.now());
            debtRepository.save(debt);
        });
        log.info("✅ Retrieved and recalculated {} dealer debts", debts.size());
        return debts;
    }

    // 🆕 Lấy danh sách NỢ CỦA DEALER theo dealerId (dealer nợ hãng) - EVM xem
    @Transactional
    public List<Debt> getDealerDebtsByDealerId(Long dealerId) {
        List<Debt> debts = debtRepository.findByDebtTypeAndDealerDealerId("DEALER_DEBT", dealerId);
        // ✅ Tính lại amountPaid cho mỗi debt và PERSIST vào DB
        debts.forEach(debt -> {
            recalculateAmountPaid(debt);
            debt.setUpdatedDate(LocalDateTime.now());
            debtRepository.save(debt);
        });
        log.info("✅ Retrieved and recalculated {} debts for dealer {}", debts.size(), dealerId);
        return debts;
    }

    // 🆕 Lấy danh sách NỢ CỦA CUSTOMER (customer nợ dealer) - Dealer xem
    @Transactional
    public List<Debt> getCustomerDebts(Long dealerId) {
        List<Debt> debts = debtRepository.findByDebtTypeAndDealerDealerId("CUSTOMER_DEBT", dealerId);
        // ✅ Tính lại amountPaid cho mỗi debt và PERSIST vào DB
        debts.forEach(debt -> {
            recalculateAmountPaid(debt);
            debt.setUpdatedDate(LocalDateTime.now());
            debtRepository.save(debt);
        });
        log.info("✅ Retrieved and recalculated {} customer debts for dealer {}", debts.size(), dealerId);
        return debts;
    }

    // Lấy danh sách nợ theo dealer (deprecated - dùng getDealerDebtsByDealerId hoặc getCustomerDebts)
    @Deprecated
    @Transactional
    public List<Debt> getDebtsByDealer(Long dealerId) {
        List<Debt> debts = debtRepository.findByDealerDealerId(dealerId);
        // ✅ Tính lại amountPaid cho mỗi debt và PERSIST vào DB
        debts.forEach(debt -> {
            recalculateAmountPaid(debt);
            debt.setUpdatedDate(LocalDateTime.now());
            debtRepository.save(debt);
        });
        return debts;
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
    // ✅ Tự động tính lại amountPaid từ Payment ban đầu + DebtPayments CONFIRMED
    public Debt getDebtById(Long id) {
        Debt debt = debtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Debt not found with id: " + id));
        
        // ✅ Tính lại amountPaid để đảm bảo bao gồm số tiền ban đầu từ Payment
        recalculateAmountPaid(debt);
        
        return debt;
    }
    
    /**
     * ✅ Tính lại amountPaid từ Payment ban đầu + DebtPayments CONFIRMED (không lưu vào DB)
     * Method này chỉ tính toán trong memory, không persist
     */
    private void recalculateAmountPaid(Debt debt) {
        // 1. Lấy số tiền ban đầu từ Payment (Order)
        BigDecimal initialPaymentAmount = getInitialPaymentAmountFromOrder(debt);
        
        // 2. Tính tổng DebtPayments CONFIRMED
        BigDecimal totalPaidFromDebtPayments = debtPaymentRepository.findByDebtDebtIdAndStatus(debt.getDebtId(), "CONFIRMED")
                .stream()
                .map(DebtPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 3. amountPaid = số tiền ban đầu + tổng DebtPayments CONFIRMED
        BigDecimal totalPaid = initialPaymentAmount.add(totalPaidFromDebtPayments);
        
        // 4. Set vào debt object (không lưu vào DB - chỉ trong memory)
        debt.setAmountPaid(totalPaid);
        
        log.info("💰 Recalculated amountPaid for Debt {}: Initial={}, DebtPayments={}, Total={}", 
                debt.getDebtId(), initialPaymentAmount, totalPaidFromDebtPayments, totalPaid);
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
        
        // ✅ Lấy số tiền đã thanh toán từ Payment (Order) - không dùng debt.getAmountPaid() vì có thể chưa được set
        BigDecimal initialPaymentAmount = getInitialPaymentAmountFromOrder(debt);
        
        // ✅ Tính số tiền còn nợ = Tổng tiền - Số tiền đã thanh toán từ Order
        BigDecimal remainingDebt = totalAmount.subtract(initialPaymentAmount);
        
        // ✅ Chỉ chia cho số kỳ nếu remainingDebt > 0
        if (remainingDebt.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn(" Remaining debt is {} (total={}, initialPayment={}) - Cannot generate schedule", 
                    remainingDebt, totalAmount, initialPaymentAmount);
            return;
        }
        
        BigDecimal installment = remainingDebt.divide(BigDecimal.valueOf(numberOfPeriods), 2, RoundingMode.HALF_UP);
    
        BigDecimal remainingBalance = remainingDebt;
    
        // ✅ Nếu đã có initialPayment (từ Order), cập nhật kỳ đầu tiên
        boolean hasInitialPayment = initialPaymentAmount.compareTo(BigDecimal.ZERO) > 0;
        
        log.info("📊 Generating debt schedule for Debt {}: Total={}, InitialPayment={}, Remaining={}, Installment={}/period", 
                debt.getDebtId(), totalAmount, initialPaymentAmount, remainingDebt, installment);
    
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
            
            // ✅ Kỳ đầu tiên: Nếu đã thanh toán từ Order, đánh dấu là PARTIAL hoặc PAID
            if (i == 1 && hasInitialPayment) {
                if (initialPaymentAmount.compareTo(installment) >= 0) {
                    schedule.setPaidAmount(installment);
                    schedule.setStatus("PAID");
                    schedule.setPaymentDate(LocalDate.now());
                } else {
                    schedule.setPaidAmount(initialPaymentAmount);
                    schedule.setStatus("PARTIAL");
                }
            } else {
                schedule.setStatus("PENDING");
            }
    
            debt.addDebtSchedule(schedule);
            remainingBalance = endBalance;
        }
    }

    /**
     * ✅ Cập nhật lại start_balance và end_balance của các kỳ sau khi thanh toán
     * Được gọi sau mỗi lần thanh toán được confirmed để đảm bảo các kỳ sau phản ánh đúng số tiền còn lại
     */
    @Transactional
    private void updateDebtScheduleBalances(Debt debt) {
        // 1. Tính tổng số tiền đã thanh toán
        BigDecimal initialPaymentAmount = getInitialPaymentAmountFromOrder(debt);
        BigDecimal totalConfirmedPayments = debtPaymentRepository.findByDebtDebtIdAndStatus(debt.getDebtId(), "CONFIRMED")
                .stream()
                .map(DebtPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPaid = initialPaymentAmount.add(totalConfirmedPayments);
        
        // 2. Tính số tiền còn lại
        BigDecimal totalAmount = debt.getAmountDue();
        BigDecimal remainingDebt = totalAmount.subtract(totalPaid);
        
        // 3. Lấy tất cả schedules và sắp xếp theo period_no
        List<DebtSchedule> schedules = debtScheduleRepository.findByDebtOrderByPeriodNo(debt.getDebtId());
        if (schedules.isEmpty()) {
            return;
        }
        
        // 4. Tính số tiền mỗi kỳ (dựa trên số kỳ còn lại chưa thanh toán đầy đủ)
        // Một kỳ được coi là đã thanh toán đầy đủ nếu: status = "PAID" VÀ paidAmount >= installment
        long unpaidPeriods = schedules.stream()
                .filter(s -> {
                    BigDecimal sPaidAmount = s.getPaidAmount() != null ? s.getPaidAmount() : BigDecimal.ZERO;
                    BigDecimal sRequiredAmount = s.getInstallment();
                    return !("PAID".equals(s.getStatus()) && sPaidAmount.compareTo(sRequiredAmount) >= 0);
                })
                .count();
        
        if (unpaidPeriods == 0) {
            // Đã thanh toán hết, tất cả kỳ đều PAID và đủ tiền
            log.info("✅ All schedules are fully PAID for Debt {}", debt.getDebtId());
            return;
        }
        
        BigDecimal installmentPerPeriod = remainingDebt.divide(BigDecimal.valueOf(unpaidPeriods), 2, RoundingMode.HALF_UP);
        
        log.info("📊 Updating schedule balances for Debt {}: TotalPaid={}, Remaining={}, UnpaidPeriods={}, InstallmentPerPeriod={}", 
                debt.getDebtId(), totalPaid, remainingDebt, unpaidPeriods, installmentPerPeriod);
        
        // 5. Cập nhật lại start_balance và end_balance cho tất cả các kỳ
        // Tính lại currentBalance: bắt đầu từ số tiền còn lại (remainingDebt) - đây là số tiền sau khi đã thanh toán tất cả các kỳ trước
        BigDecimal currentBalance = remainingDebt;
        
        // Tìm kỳ đầu tiên chưa thanh toán đầy đủ
        long firstUnpaidPeriod = schedules.stream()
                .filter(s -> {
                    BigDecimal sPaidAmount = s.getPaidAmount() != null ? s.getPaidAmount() : BigDecimal.ZERO;
                    BigDecimal sRequiredAmount = s.getInstallment();
                    return !("PAID".equals(s.getStatus()) && sPaidAmount.compareTo(sRequiredAmount) >= 0);
                })
                .mapToLong(DebtSchedule::getPeriodNo)
                .min()
                .orElse(1L);
        
        // Tìm kỳ cuối cùng chưa thanh toán đầy đủ
        long lastUnpaidPeriod = schedules.stream()
                .filter(s -> {
                    BigDecimal sPaidAmount = s.getPaidAmount() != null ? s.getPaidAmount() : BigDecimal.ZERO;
                    BigDecimal sRequiredAmount = s.getInstallment();
                    return !("PAID".equals(s.getStatus()) && sPaidAmount.compareTo(sRequiredAmount) >= 0);
                })
                .mapToLong(DebtSchedule::getPeriodNo)
                .max()
                .orElse((long) schedules.size());
        
        for (DebtSchedule schedule : schedules) {
            // Kiểm tra kỳ đã thanh toán đầy đủ: status = "PAID" VÀ paidAmount >= installment
            BigDecimal paidAmount = schedule.getPaidAmount() != null ? schedule.getPaidAmount() : BigDecimal.ZERO;
            BigDecimal requiredAmount = schedule.getInstallment();
            boolean isFullyPaid = "PAID".equals(schedule.getStatus()) && paidAmount.compareTo(requiredAmount) >= 0;
            
            if (isFullyPaid) {
                // Kỳ đã thanh toán đầy đủ: cập nhật end_balance dựa trên paid_amount
                // Không thay đổi start_balance và paid_amount
                BigDecimal scheduleStartBalance = schedule.getStartBalance() != null ? schedule.getStartBalance() : BigDecimal.ZERO;
                
                // Tính end_balance = start_balance - paid_amount
                BigDecimal newEndBalance = scheduleStartBalance.subtract(paidAmount).setScale(2, RoundingMode.HALF_UP);
                if (newEndBalance.compareTo(BigDecimal.ZERO) < 0) {
                    newEndBalance = BigDecimal.ZERO;
                }
                schedule.setEndBalance(newEndBalance);
                
                // Cập nhật currentBalance cho kỳ tiếp theo
                currentBalance = newEndBalance;
            } else {
                // Kỳ chưa thanh toán: cập nhật lại start_balance và end_balance
                // start_balance = currentBalance (từ end_balance của kỳ trước, hoặc remainingDebt nếu là kỳ đầu tiên chưa thanh toán)
                if (schedule.getPeriodNo() == firstUnpaidPeriod) {
                    // Kỳ đầu tiên chưa thanh toán: start_balance = remainingDebt
                    schedule.setStartBalance(remainingDebt);
                    currentBalance = remainingDebt;
                } else {
                    // Kỳ sau: start_balance = end_balance của kỳ trước
                    schedule.setStartBalance(currentBalance);
                }
                
                BigDecimal principal;
                if (schedule.getPeriodNo() == lastUnpaidPeriod) {
                    // Kỳ cuối cùng chưa thanh toán: điều chỉnh để end_balance = 0
                    principal = currentBalance;
                } else {
                    principal = installmentPerPeriod;
                }
                
                BigDecimal endBalance = currentBalance.subtract(principal).setScale(2, RoundingMode.HALF_UP);
                if (endBalance.compareTo(BigDecimal.ZERO) < 0) {
                    endBalance = BigDecimal.ZERO;
                }
                
                schedule.setPrincipal(principal);
                schedule.setEndBalance(endBalance);
                schedule.setInstallment(installmentPerPeriod);
                
                // ✅ Cập nhật status cho kỳ chưa thanh toán đầy đủ
                // Sử dụng installmentPerPeriod (giá trị mới sau khi tính lại) để so sánh với paidAmount
                // Nếu kỳ có status = "PAID" nhưng paidAmount < installment, cập nhật lại status
                BigDecimal newRequiredAmount = schedule.getPeriodNo() == lastUnpaidPeriod ? principal : installmentPerPeriod;
                if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                    if (paidAmount.compareTo(newRequiredAmount) >= 0) {
                        schedule.setStatus("PAID");
                        schedule.setPaymentDate(LocalDate.now());
                    } else {
                        schedule.setStatus("PARTIAL");
                    }
                } else {
                    schedule.setStatus("PENDING");
                }
                
                // Cập nhật currentBalance cho kỳ tiếp theo
                currentBalance = endBalance;
            }
            
            debtScheduleRepository.save(schedule);
        }
        
        log.info("✅ Updated all schedule balances for Debt {}", debt.getDebtId());
    }

    /**
     * ✅ Tự động dồn số tiền dư từ kỳ đã thanh toán sang các kỳ tiếp theo
     * Được gọi sau khi một payment được confirmed và có scheduleId
     * @param currentSchedule Kỳ hiện tại đã được thanh toán
     * @param newTotalPaidAmount Tổng số tiền đã thanh toán của kỳ này (bao gồm paymentAmount mới)
     */
    @Transactional
    private void applyOverpaymentToNextPeriods(DebtSchedule currentSchedule, BigDecimal newTotalPaidAmount) {
        BigDecimal requiredAmount = currentSchedule.getInstallment();
        BigDecimal overpayment = newTotalPaidAmount.subtract(requiredAmount);
        
        // Nếu không có dư, không cần làm gì
        if (overpayment.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        
        log.info("💰 Overpayment detected for Schedule {}: TotalPaid={}, Required={}, Overpayment={}", 
                currentSchedule.getScheduleId(), newTotalPaidAmount, requiredAmount, overpayment);
        
        // Lấy tất cả các kỳ chưa thanh toán đầy đủ, sắp xếp theo period_no
        // Bao gồm cả các kỳ có status = "PAID" nhưng paidAmount < installment (trường hợp lỗi dữ liệu)
        List<DebtSchedule> unpaidSchedules = debtScheduleRepository.findByDebtOrderByPeriodNo(currentSchedule.getDebt().getDebtId())
                .stream()
                .filter(s -> {
                    if (s.getPeriodNo() <= currentSchedule.getPeriodNo()) {
                        return false; // Chỉ lấy các kỳ sau kỳ hiện tại
                    }
                    // Lấy các kỳ chưa thanh toán đầy đủ
                    BigDecimal sPaidAmount = s.getPaidAmount() != null ? s.getPaidAmount() : BigDecimal.ZERO;
                    BigDecimal sRequiredAmount = s.getInstallment();
                    return sPaidAmount.compareTo(sRequiredAmount) < 0;
                })
                .collect(java.util.stream.Collectors.toList());
        
        if (unpaidSchedules.isEmpty()) {
            log.warn("⚠️ No future unpaid schedules found to apply overpayment");
            return;
        }
        
        // Áp dụng số tiền dư cho các kỳ tiếp theo
        BigDecimal remainingOverpayment = overpayment;
        for (DebtSchedule nextSchedule : unpaidSchedules) {
            if (remainingOverpayment.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            
            BigDecimal nextRequiredAmount = nextSchedule.getInstallment();
            BigDecimal nextCurrentPaid = nextSchedule.getPaidAmount() != null ? nextSchedule.getPaidAmount() : BigDecimal.ZERO;
            BigDecimal nextRemaining = nextRequiredAmount.subtract(nextCurrentPaid);
            
            if (nextRemaining.compareTo(BigDecimal.ZERO) > 0) {
                // Áp dụng số tiền dư cho kỳ này
                BigDecimal amountToApply = remainingOverpayment.min(nextRemaining);
                BigDecimal newPaidAmount = nextCurrentPaid.add(amountToApply);
                nextSchedule.setPaidAmount(newPaidAmount);
                
                // Cập nhật status
                if (newPaidAmount.compareTo(nextRequiredAmount) >= 0) {
                    nextSchedule.setStatus("PAID");
                    nextSchedule.setPaymentDate(LocalDate.now());
                    log.info("✅ Schedule {} (Period {}) is now PAID from overpayment: {} / {}", 
                            nextSchedule.getScheduleId(), nextSchedule.getPeriodNo(), newPaidAmount, nextRequiredAmount);
                } else {
                    nextSchedule.setStatus("PARTIAL");
                    log.info("💰 Schedule {} (Period {}) updated from overpayment: {} / {} (remaining: {})", 
                            nextSchedule.getScheduleId(), nextSchedule.getPeriodNo(), newPaidAmount, nextRequiredAmount, 
                            nextRequiredAmount.subtract(newPaidAmount));
                }
                
                debtScheduleRepository.save(nextSchedule);
                remainingOverpayment = remainingOverpayment.subtract(amountToApply);
            }
        }
        
        // Điều chỉnh lại paidAmount của kỳ hiện tại để bằng đúng requiredAmount
        // (phần dư đã được áp dụng cho các kỳ tiếp theo)
        currentSchedule.setPaidAmount(requiredAmount);
        debtScheduleRepository.save(currentSchedule);
        
        if (remainingOverpayment.compareTo(BigDecimal.ZERO) > 0) {
            log.warn("⚠️ Remaining overpayment {} could not be applied to any future periods", remainingOverpayment);
        } else {
            log.info("✅ All overpayment applied to future periods");
        }
    }

    // ================== XỬ LÝ THANH TOÁN ==================
    
    /**
     * ✅ Tự động cập nhật debt status dựa trên tổng số tiền đã thanh toán
     */
    @Transactional
    public void updateDebtStatus(Long debtId) {
        Debt debt = getDebtById(debtId);
        
        // ✅ Tính số tiền ban đầu từ Payment (Order) nếu có
        BigDecimal initialPaymentAmount = getInitialPaymentAmountFromOrder(debt);
        
        // Tính tổng số tiền đã thanh toán từ DebtPayments CONFIRMED
        BigDecimal totalPaidFromDebtPayments = debtPaymentRepository.findByDebtDebtIdAndStatus(debtId, "CONFIRMED")
                .stream()
                .map(DebtPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // amountPaid = số tiền ban đầu + tổng DebtPayments CONFIRMED
        BigDecimal totalPaid = initialPaymentAmount.add(totalPaidFromDebtPayments);
        
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
        // 1. Lấy khoản nợ cần thanh toán (sẽ recalculate amountPaid)
        Debt debt = getDebtById(debtId);

        // 2. Validate: Kiểm tra số tiền thanh toán không vượt quá số tiền còn nợ
        // Dùng amountPaid đã được recalculate (bao gồm initial payment + confirmed payments)
        BigDecimal currentPaid = debt.getAmountPaid() != null ? debt.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal remainingAmount = debt.getAmountDue().subtract(currentPaid);
        
        // Cho phép thanh toán với sai số nhỏ do rounding (0.01)
        if (request.getAmount().subtract(remainingAmount).compareTo(new BigDecimal("0.01")) > 0) {
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
        }

        // 5. Lưu payment
        DebtPayment savedPayment = debtPaymentRepository.save(payment);
        
        // ✅ Nếu thanh toán bằng CASH, tự động CONFIRMED và cập nhật amount_paid ngay
        if ("CASH".equalsIgnoreCase(request.getPaymentMethod())) {
            savedPayment.setStatus("CONFIRMED");
            savedPayment.setConfirmedBy(request.getCreatedBy());
            savedPayment.setConfirmedDate(LocalDateTime.now());
            debtPaymentRepository.save(savedPayment);
            
            // Cập nhật DebtSchedule nếu có
            if (savedPayment.getDebtSchedule() != null) {
                DebtSchedule schedule = savedPayment.getDebtSchedule();
                BigDecimal currentPaidAmount = schedule.getPaidAmount() != null ? schedule.getPaidAmount() : BigDecimal.ZERO;
                BigDecimal newPaidAmount = currentPaidAmount.add(savedPayment.getAmount());
                schedule.setPaidAmount(newPaidAmount);
                
                if (newPaidAmount.compareTo(schedule.getInstallment()) >= 0) {
                    schedule.setStatus("PAID");
                    schedule.setPaymentDate(LocalDate.now());
                } else {
                    schedule.setStatus("PARTIAL");
                }
                debtScheduleRepository.save(schedule);
                
                // ✅ Tự động dồn số tiền dư sang các kỳ tiếp theo
                applyOverpaymentToNextPeriods(schedule, newPaidAmount);
            }
            
            // Cập nhật Debt.amount_paid: Số tiền ban đầu từ Payment + tổng DebtPayments CONFIRMED
            BigDecimal initialPaymentAmount = getInitialPaymentAmountFromOrder(debt);
            BigDecimal totalConfirmedPayments = debtPaymentRepository.findByDebtDebtIdAndStatus(debtId, "CONFIRMED")
                    .stream()
                    .map(DebtPayment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal newAmountPaid = initialPaymentAmount.add(totalConfirmedPayments);
            
            debt.setAmountPaid(newAmountPaid);
            updateDebtStatusWithRounding(debt, newAmountPaid);
            debt.setUpdatedDate(LocalDateTime.now());
            debtRepository.save(debt);
            
            // ✅ Cập nhật lại start_balance và end_balance của các kỳ sau khi thanh toán
            updateDebtScheduleBalances(debt);
            
            log.info("✅ Payment auto-CONFIRMED (CASH): Debt {} - Amount: {} - Total paid: {} / {} - Reference: {}", 
                    debtId, savedPayment.getAmount(), newAmountPaid, debt.getAmountDue(), savedPayment.getReferenceNumber());
        } else {
            log.info("✅ Payment created (PENDING): Debt {} - Amount: {} - Method: {} - Reference: {}", 
                    debtId, request.getAmount(), request.getPaymentMethod(), savedPayment.getReferenceNumber());
        }
        
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
            
            // ✅ Tự động dồn số tiền dư sang các kỳ tiếp theo
            applyOverpaymentToNextPeriods(schedule, newPaidAmount);
        }
        
        // 6. Cập nhật số tiền đã thanh toán vào Debt
        Debt debt = getDebtById(debtId);
        
        // ✅ Tính số tiền ban đầu từ Payment (Order) nếu có
        BigDecimal initialPaymentAmount = getInitialPaymentAmountFromOrder(debt);
        
        // Tính lại tổng số tiền đã thanh toán từ tất cả DebtPayments CONFIRMED
        BigDecimal totalPaidFromDebtPayments = debtPaymentRepository.findByDebtDebtIdAndStatus(debtId, "CONFIRMED")
                .stream()
                .map(DebtPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // ✅ amountPaid = số tiền ban đầu từ Payment (108 triệu) + tổng DebtPayments CONFIRMED (45 triệu)
        BigDecimal newAmountPaid = initialPaymentAmount.add(totalPaidFromDebtPayments);
        
        debt.setAmountPaid(newAmountPaid);
        
        // 7. Update status của Debt với auto-fix rounding
        updateDebtStatusWithRounding(debt, newAmountPaid);
        
        debt.setUpdatedDate(LocalDateTime.now());
        debtRepository.save(debt);
        
        // ✅ Cập nhật lại start_balance và end_balance của các kỳ sau khi thanh toán
        updateDebtScheduleBalances(debt);
        
        log.info("✅ Payment CONFIRMED: Payment {} - Debt {} - Amount: {} - Total paid: {} / {} - By: {}", 
                paymentId, debtId, payment.getAmount(), newAmountPaid, debt.getAmountDue(), confirmedBy);
        
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

    // ================== HELPER METHODS ==================
    
    /**
     * ✅ Lấy số tiền ban đầu từ Payment (Order) - số tiền khách hàng trả trước (20%)
     * Hỗ trợ 2 trường hợp:
     * 1. Debt từ Payment: notes = "Auto-generated from Payment: {paymentId} - Order: {orderId}..."
     * 2. Debt từ DealerRequest: notes = "Auto-generated from DealerRequest: {requestId}"
     */
    private BigDecimal getInitialPaymentAmountFromOrder(Debt debt) {
        try {
            String notes = debt.getNotes();
            log.info("🔍 Checking Debt {} notes: {}", debt.getDebtId(), notes);
            
            if (notes == null) {
                log.warn("⚠️ Debt {} notes is null - returning 0", debt.getDebtId());
                return BigDecimal.ZERO;
            }
            
            Long orderId = null;
            
            // Case 1: Notes chứa "Order:" (Debt từ Payment)
            if (notes.contains("Order:")) {
                String orderIdStr = null;
                String[] parts = notes.split("Order:");
                if (parts.length > 1) {
                    String afterOrder = parts[1].trim();
                    String[] orderParts = afterOrder.split("\\s+");
                    if (orderParts.length > 0) {
                        orderIdStr = orderParts[0].trim();
                    }
                }
                
                if (orderIdStr != null) {
                    orderId = Long.parseLong(orderIdStr);
                    log.info("📋 Extracted orderId {} from Debt {} notes (from Payment)", orderId, debt.getDebtId());
                }
            }
            // Case 2: Notes chứa "DealerRequest:" (Debt từ DealerRequest)
            else if (notes.contains("DealerRequest:")) {
                // Extract requestId
                String requestIdStr = null;
                String[] parts = notes.split("DealerRequest:");
                if (parts.length > 1) {
                    String afterRequest = parts[1].trim();
                    String[] requestParts = afterRequest.split("\\s+");
                    if (requestParts.length > 0) {
                        requestIdStr = requestParts[0].trim();
                    }
                }
                
                if (requestIdStr != null) {
                    Long requestId = Long.parseLong(requestIdStr);
                    log.info("📋 Extracted requestId {} from Debt {} notes", requestId, debt.getDebtId());
                    
                    // Tìm DealerRequest
                    DealerRequest request = dealerRequestRepository.findById(requestId).orElse(null);
                    if (request == null) {
                        log.warn("⚠️ DealerRequest {} not found for Debt {}", requestId, debt.getDebtId());
                        return BigDecimal.ZERO;
                    }
                    
                    // Tìm Order từ DealerRequest
                    // Thử tìm Order có orderId = requestId trước (thường thì DealerRequest #N tương ứng với Order #N)
                    Order order = orderRepository.findById(requestId).orElse(null);
                    
                    // Nếu không tìm thấy Order có orderId = requestId, tìm Order gần nhất của dealer
                    if (order == null) {
                        List<Order> orders = orderRepository.findByDealerDealerId(request.getDealer().getDealerId());
                        // Không filter theo customer vì Order có thể có customer hoặc không
                        order = orders.stream()
                                .max(Comparator.comparing(Order::getCreatedDate))
                                .orElse(null);
                    }
                    
                    if (order == null) {
                        log.warn("⚠️ No Order found for DealerRequest {} (Debt {})", requestId, debt.getDebtId());
                        return BigDecimal.ZERO;
                    }
                    
                    // Đảm bảo Order thuộc về dealer của DealerRequest
                    if (!order.getDealer().getDealerId().equals(request.getDealer().getDealerId())) {
                        log.warn("⚠️ Order {} does not belong to Dealer {} (Debt {})", 
                                order.getOrderId(), request.getDealer().getDealerId(), debt.getDebtId());
                        return BigDecimal.ZERO;
                    }
                    
                    orderId = order.getOrderId();
                    log.info("✅ Found Order {} from DealerRequest {} for Debt {}", orderId, requestId, debt.getDebtId());
                }
            } else {
                log.warn("⚠️ Debt {} notes does not contain 'Order:' or 'DealerRequest:' (notes={}) - returning 0", 
                        debt.getDebtId(), notes);
                return BigDecimal.ZERO;
            }
            
            if (orderId == null) {
                log.warn("⚠️ Could not extract orderId from Debt {} notes: {}", debt.getDebtId(), notes);
                return BigDecimal.ZERO;
            }
            
            // Tìm Payment từ orderId
            List<Payment> payments = paymentRepository.findAllByOrderId(orderId);
            if (payments == null || payments.isEmpty()) {
                log.warn("⚠️ No Payment found for Order {} (Debt {})", orderId, debt.getDebtId());
                return BigDecimal.ZERO;
            }
            
            Payment payment = payments.stream()
                    .filter(p -> "INSTALLMENT".equalsIgnoreCase(p.getPaymentType()))
                    .filter(p -> {
                        String status = p.getStatus();
                        return status != null && ("Completed".equalsIgnoreCase(status) || "Pending".equalsIgnoreCase(status));
                    })
                    .sorted(Comparator.comparing(Payment::getPaymentDate, Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
                    .findFirst()
                    .orElse(null);
            
            if (payment == null) {
                log.warn("⚠️ No suitable INSTALLMENT payment found for Order {} (Debt {})", orderId, debt.getDebtId());
                return BigDecimal.ZERO;
            }
            
            log.info("💳 Found Payment {} for Order {}: type={}, status={}, amount={}", 
                    payment.getPaymentId(), orderId, payment.getPaymentType(), payment.getStatus(), payment.getAmount());
            
            // Lấy nếu paymentType = INSTALLMENT (kể cả Pending hoặc Completed)
            if ("INSTALLMENT".equalsIgnoreCase(payment.getPaymentType())) {
                if ("Completed".equalsIgnoreCase(payment.getStatus()) || 
                    "Pending".equalsIgnoreCase(payment.getStatus())) {
                    BigDecimal amount = payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO;
                    log.info("✅ Found initial payment from Order {}: {} (status: {}) for Debt {}", 
                            orderId, amount, payment.getStatus(), debt.getDebtId());
                    return amount;
                } else {
                    log.warn("⚠️ Payment {} status is '{}' (not Completed/Pending) - skipping for Debt {}", 
                            payment.getPaymentId(), payment.getStatus(), debt.getDebtId());
                }
            } else {
                log.warn("⚠️ Payment {} type is '{}' (not INSTALLMENT) - skipping for Debt {}", 
                        payment.getPaymentId(), payment.getPaymentType(), debt.getDebtId());
            }
            
            return BigDecimal.ZERO;
        } catch (NumberFormatException e) {
            log.error("❌ Failed to parse ID from Debt {} notes: {}", debt.getDebtId(), debt.getNotes(), e);
            return BigDecimal.ZERO;
        } catch (Exception e) {
            log.error("❌ Failed to get initial payment amount from Order for Debt {}: {}", 
                    debt.getDebtId(), e.getMessage(), e);
            return BigDecimal.ZERO;
        }
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
            // 10. Cập nhật debt đã tồn tại
            // Nếu Payment Completed: Đảm bảo amountPaid >= payment.amount
            // Nếu Payment Pending: Chỉ cập nhật nếu amountPaid < payment.amount
            BigDecimal currentPaid = debt.getAmountPaid() != null ? debt.getAmountPaid() : BigDecimal.ZERO;
            BigDecimal paymentAmount = payment.getAmount();
            
            boolean shouldUpdate = false;
            if ("Completed".equalsIgnoreCase(payment.getStatus())) {
                // Payment Completed: Đảm bảo amountPaid >= payment.amount
                if (currentPaid.compareTo(paymentAmount) < 0) {
                    debt.setAmountPaid(paymentAmount); // Cập nhật = payment.amount (108 triệu)
                    shouldUpdate = true;
                    log.info("✅ CUSTOMER_DEBT updated (Payment Completed): amountPaid = {} (from Payment {}) - Order: {}", 
                            paymentAmount, payment.getPaymentId(), order.getOrderId());
                }
            } else {
                // Payment Pending: Cộng thêm nếu chưa có
                if (currentPaid.compareTo(paymentAmount) < 0) {
                    debt.setAmountPaid(paymentAmount);
                    shouldUpdate = true;
                    log.info("✅ CUSTOMER_DEBT updated (Payment Pending): amountPaid = {} - Order: {}", 
                            paymentAmount, order.getOrderId());
                }
            }
            
            if (shouldUpdate) {
                debt.setUpdatedDate(LocalDateTime.now());
                debt = debtRepository.save(debt);
            }
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
    public DebtSchedule payDebtScheduleDirectly(Long scheduleId, BigDecimal amount, String paymentMethod, String notes) {
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
        payment.setPaymentMethod(paymentMethod != null ? paymentMethod : "DIRECT"); // Dùng giá trị từ request hoặc mặc định
        payment.setReferenceNumber(String.format("DIR-%d-%s",
                debt.getDebtId(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))));
        payment.setNotes(notes != null ? notes : "Direct pay via API /debts/schedules/{id}/direct-pay"); // Dùng giá trị từ request hoặc mặc định
        payment.setCreatedBy("system");
        payment.setStatus("CONFIRMED");
        payment.setConfirmedBy("system");
        payment.setConfirmedDate(LocalDateTime.now());
        debtPaymentRepository.save(payment);

        log.info("✅ Direct payment for DebtSchedule {} processed. Amount: {}, Method: {}, Notes: {}", scheduleId, amount, paymentMethod, notes);
        return schedule;
    }
}
