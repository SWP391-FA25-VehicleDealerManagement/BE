package com.example.evm.controller.debt;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.debt.CreateDebtPaymentRequest;
import com.example.evm.dto.debt.DebtResponse;
import com.example.evm.entity.debt.Debt;
import com.example.evm.entity.debt.DebtPayment;
import com.example.evm.entity.debt.DebtSchedule;
import com.example.evm.service.debt.DebtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/debts")
@RequiredArgsConstructor
public class DebtController {

    private final DebtService debtService;

    // ==========================================================
    // =============== CREATE OPERATIONS ========================
    // ==========================================================

    /**
     * API tạo mới một khoản nợ (Debt)
     * URL: POST /api/debts
     * Quyền: ADMIN, EVM_STAFF, DEALER_STAFF, DEALER_MANAGER
     * Dữ liệu gửi lên: Debt (JSON)
     * Kết quả: Debt vừa được tạo hoặc thông báo lỗi
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Debt>> createDebt(@Valid @RequestBody Debt debt) {
        try {
            Debt createdDebt = debtService.createDebt(debt);
            return ResponseEntity.ok(new ApiResponse<>(true, "Debt created successfully", createdDebt));
        } catch (Exception e) {
            log.error("Error creating debt", e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to create debt: " + e.getMessage(), null));
        }
    }

    /**
     * API thanh toán nợ - Đơn giản hóa, KHÔNG CẦN ORDER_ID
     * URL: POST /api/debts/{debtId}/payments
     * Quyền: ADMIN, EVM_STAFF, DEALER_STAFF, DEALER_MANAGER
     * 
     * Request Body:
     * {
     *   "amount": 10000000,
     *   "paymentMethod": "CASH|BANK_TRANSFER|CREDIT_CARD",
     *   "scheduleId": 123,  // Optional: ID của kỳ thanh toán nếu là trả góp
     *   "referenceNumber": "TT123456",  // Optional
     *   "notes": "Thanh toán tiền mặt",  // Optional
     *   "createdBy": "admin"  // Optional
     * }
     * 
     * Response: DebtPayment (không có order_id)
     */
    @PostMapping("/{debtId}/payments")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<DebtPayment>> makePayment(
            @PathVariable Long debtId, 
            @Valid @RequestBody CreateDebtPaymentRequest request) {
        try {
            DebtPayment createdPayment = debtService.makePayment(debtId, request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Payment made successfully", createdPayment));
        } catch (Exception e) {
            log.error("Error making payment for debt {}", debtId, e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to make payment: " + e.getMessage(), null));
        }
    }


    /**
     * Lấy danh sách tất cả các khoản nợ trong hệ thống
     * URL: GET /api/debts
     * Quyền: ADMIN, EVM_STAFF
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<List<DebtResponse>>> getAllDebts() {
        List<DebtResponse> debts = debtService.getAllDebtsWithFullInfo();
        return ResponseEntity.ok(new ApiResponse<>(true, "All debts retrieved successfully", debts));
    }

    /**
     * API LẤY NỢ CỦA DEALER (dealer nợ hãng VinFast)
     * URL: GET /api/debts/dealer-debts
     * Quyền: ADMIN, EVM_STAFF (chỉ hãng xe mới xem được)
     * Mô tả: Hiển thị danh sách các dealer đang nợ tiền hãng
     */
    @GetMapping("/dealer-debts")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<List<DebtResponse>>> getDealerDebts() {
        try {
            List<DebtResponse> debts = debtService.getDealerDebtsWithFullInfo();
            log.info("Retrieved {} dealer debts", debts.size());
            return ResponseEntity.ok(new ApiResponse<>(true, "Dealer debts retrieved successfully", debts));
        } catch (Exception e) {
            log.error("Error retrieving dealer debts", e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, "Failed to retrieve dealer debts", null));
        }
    }

    /**
     * API LẤY NỢ CỦA DEALER theo dealerId (dealer nợ hãng)
     * URL: GET /api/debts/dealer-debts/{dealerId}
     * Quyền: ADMIN, EVM_STAFF
     * Mô tả: Xem chi tiết nợ của 1 dealer với hãng
     */
    @GetMapping("/dealer-debts/{dealerId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<List<DebtResponse>>> getDealerDebtsByDealerId(@PathVariable Long dealerId) {
        try {
            List<DebtResponse> debts = debtService.getDealerDebtsByDealerIdWithFullInfo(dealerId);
            log.info("Retrieved {} debts for dealer {}", debts.size(), dealerId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Dealer debts retrieved successfully", debts));
        } catch (Exception e) {
            log.error("Error retrieving debts for dealer {}", dealerId, e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, "Failed to retrieve dealer debts", null));
        }
    }

    /**
     * API LẤY NỢ CỦA CUSTOMER (customer nợ dealer)
     * URL: GET /api/debts/customer-debts/{dealerId}
     * Quyền: ADMIN, EVM_STAFF, DEALER_STAFF, DEALER_MANAGER
     * Mô tả: Dealer xem danh sách khách hàng đang nợ tiền mua xe
     */
    @GetMapping("/customer-debts/{dealerId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<DebtResponse>>> getCustomerDebts(@PathVariable Long dealerId) {
        try {
            List<DebtResponse> debts = debtService.getCustomerDebtsWithFullInfo(dealerId);
            log.info("Retrieved {} customer debts for dealer {}", debts.size(), dealerId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Customer debts retrieved successfully", debts));
        } catch (Exception e) {
            log.error("Error retrieving customer debts for dealer {}", dealerId, e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, "Failed to retrieve customer debts", null));
        }
    }

    /**
     * Lấy danh sách nợ theo dealer (deprecated - dùng dealer-debts hoặc customer-debts)
     * URL: GET /api/debts/dealer/{dealerId}
     * Quyền: ADMIN, EVM_STAFF, DEALER_STAFF, DEALER_MANAGER
     */
    @Deprecated
    @GetMapping("/dealer/{dealerId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<DebtResponse>>> getDebtsByDealer(@PathVariable Long dealerId) {
        List<DebtResponse> debts = debtService.getDealerDebtsByDealerIdWithFullInfo(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Debts for dealer retrieved successfully", debts));
    }

    /**
     * Lấy danh sách nợ theo customer
     * URL: GET /api/debts/customer/{customerId}
     * Quyền: ADMIN, EVM_STAFF, DEALER_STAFF, DEALER_MANAGER
     */
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Debt>>> getDebtsByCustomer(@PathVariable Long customerId) {
        List<Debt> debts = debtService.getDebtsByCustomer(customerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Debts for customer retrieved successfully", debts));
    }

    /**
     * Cập nhật trạng thái debt tự động
     * URL: PUT /api/debts/{debtId}/update-status
     * Quyền: ADMIN, EVM_STAFF
     */
    @PutMapping("/{debtId}/update-status")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<Debt>> updateDebtStatus(@PathVariable Long debtId) {
        try {
            debtService.updateDebtStatus(debtId);
            Debt updatedDebt = debtService.getDebtById(debtId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Debt status updated successfully", updatedDebt));
        } catch (Exception e) {
            log.error("Error updating debt status for debt {}", debtId, e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to update debt status: " + e.getMessage(), null));
        }
    }

    /**
     * Lấy danh sách nợ theo user
     * URL: GET /api/debts/user/{userId}
     * Quyền: DEALER_STAFF, DEALER_MANAGER
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyAuthority('DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Debt>>> getDebtsByUser(@PathVariable Long userId) {
        List<Debt> debts = debtService.getDebtsByUser(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Debts for user retrieved successfully", debts));
    }

    /**
     * Lấy danh sách nợ theo trạng thái (status)
     * URL: GET /api/debts/status/{status}
     * Quyền: ADMIN, EVM_STAFF
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<List<Debt>>> getDebtsByStatus(@PathVariable String status) {
        List<Debt> debts = debtService.getDebtsByStatus(status);
        return ResponseEntity.ok(new ApiResponse<>(true, "Debts by status retrieved successfully", debts));
    }

    /**
     * Lấy danh sách nợ quá hạn (overdue) theo dealer
     * URL: GET /api/debts/overdue/{dealerId}
     * Quyền: ADMIN, EVM_STAFF, DEALER_STAFF, DEALER_MANAGER
     */
    @GetMapping("/overdue/{dealerId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Debt>>> getOverdueDebts(@PathVariable Long dealerId) {
        List<Debt> debts = debtService.getOverdueDebts(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Overdue debts retrieved successfully", debts));
    }

    /**
     * Lấy chi tiết một khoản nợ theo ID
     * URL: GET /api/debts/{id}
     * Quyền: ADMIN, EVM_STAFF, DEALER_STAFF, DEALER_MANAGER
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<DebtResponse>> getDebtById(@PathVariable Long id) {
        DebtResponse debt = debtService.getDebtByIdWithFullInfo(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Debt retrieved successfully", debt));
    }

    /**
     * Lấy danh sách lịch trả nợ (DebtSchedule) theo ID khoản nợ
     * URL: GET /api/debts/{id}/schedules
     */
    @GetMapping("/{id}/schedules")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<DebtSchedule>>> getDebtSchedules(@PathVariable Long id) {
        List<DebtSchedule> schedules = debtService.getDebtSchedules(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Debt schedules retrieved successfully", schedules));
    }

    /**
     * Lấy thông tin chi tiết debt schedule với thống kê
     * URL: GET /api/debts/{id}/schedule-details
     */
    @GetMapping("/{id}/schedule-details")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDebtScheduleDetails(@PathVariable Long id) {
        try {
            Map<String, Object> details = debtService.getDebtScheduleDetails(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Debt schedule details retrieved successfully", details));
        } catch (Exception e) {
            log.error("Error getting debt schedule details for debt {}", id, e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to get debt schedule details: " + e.getMessage(), null));
        }
    }

    /**
     * Tạo CUSTOMER_DEBT từ Payment (customer nợ dealer)
     * URL: POST /api/debts/create-from-payment/{paymentId}
     */
    @PostMapping("/create-from-payment/{paymentId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Debt>> createDebtFromPayment(@PathVariable Long paymentId) {
        try {
            Debt debt = debtService.createDebtFromPayment(paymentId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Debt created from payment successfully", debt));
        } catch (Exception e) {
            log.error("Error creating debt from payment {}", paymentId, e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to create debt from payment: " + e.getMessage(), null));
        }
    }

    /**
     * Thanh toán trực tiếp cho DebtSchedule (không cần xác nhận)
     * URL: POST /api/debts/schedules/{scheduleId}/direct-pay
     */
    @PostMapping("/schedules/{scheduleId}/direct-pay")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<DebtSchedule>> payDebtScheduleDirectly(
            @PathVariable Long scheduleId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false, defaultValue = "DIRECT") String paymentMethod,
            @RequestParam(required = false) String notes,
            Authentication authentication) {
        try {
            String createdBy = authentication != null && authentication.getName() != null 
                    ? authentication.getName() 
                    : "system";
            DebtSchedule schedule = debtService.payDebtScheduleDirectly(scheduleId, amount, paymentMethod, notes, createdBy);
            return ResponseEntity.ok(new ApiResponse<>(true, "Debt schedule paid successfully", schedule));
        } catch (Exception e) {
            log.error("Error paying debt schedule {}", scheduleId, e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to pay debt schedule: " + e.getMessage(), null));
        }
    }

    /**
     * Lấy danh sách các khoản thanh toán (DebtPayment) theo ID khoản nợ
     * URL: GET /api/debts/{id}/payments
     */
    @GetMapping("/{id}/payments")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<DebtPayment>>> getDebtPayments(@PathVariable Long id) {
        List<DebtPayment> payments = debtService.getDebtPayments(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Debt payments retrieved successfully", payments));
    }

    // ==========================================================
    // =============== UPDATE OPERATIONS ========================
    // ==========================================================

    /**
     * EVM Staff xác nhận thanh toán
     * URL: PUT /api/debts/{debtId}/payments/{paymentId}/confirm
     * Quyền: ADMIN, EVM_STAFF
     */
    @PutMapping("/{debtId}/payments/{paymentId}/confirm")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<DebtPayment>> confirmPayment(
            @PathVariable Long debtId,
            @PathVariable Long paymentId,
            @RequestParam(required = false) String confirmedBy) {
        try {
            String confirmedByValue = confirmedBy != null ? confirmedBy : "EVMSystem";
            DebtPayment confirmedPayment = debtService.confirmPayment(debtId, paymentId, confirmedByValue);
            return ResponseEntity.ok(new ApiResponse<>(true, "Payment confirmed successfully", confirmedPayment));
        } catch (Exception e) {
            log.error("Error confirming payment", e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to confirm payment: " + e.getMessage(), null));
        }
    }

    /**
     * EVM Staff từ chối thanh toán
     * URL: PUT /api/debts/{debtId}/payments/{paymentId}/reject
     * Quyền: ADMIN, EVM_STAFF
     */
    @PutMapping("/{debtId}/payments/{paymentId}/reject")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<DebtPayment>> rejectPayment(
            @PathVariable Long debtId,
            @PathVariable Long paymentId,
            @RequestParam(required = false) String rejectedBy,
            @RequestParam(required = false) String reason) {
        try {
            String rejectedByValue = rejectedBy != null ? rejectedBy : "EVMSystem";
            String reasonValue = reason != null ? reason : "Không hợp lệ";
            DebtPayment rejectedPayment = debtService.rejectPayment(debtId, paymentId, rejectedByValue, reasonValue);
            return ResponseEntity.ok(new ApiResponse<>(true, "Payment rejected successfully", rejectedPayment));
        } catch (Exception e) {
            log.error("Error rejecting payment", e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to reject payment: " + e.getMessage(), null));
        }
    }

    /**
     * Cập nhật trạng thái (status) của một khoản nợ
     * URL: PUT /api/debts/{id}/status?status=...&notes=...
     * Quyền: ADMIN, EVM_STAFF, DEALER_STAFF, DEALER_MANAGER
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Debt>> updateDebtStatus(@PathVariable Long id, @RequestParam String status, @RequestParam(required = false) String notes) {
        Debt updatedDebt = debtService.updateDebtStatus(id, status, notes);
        return ResponseEntity.ok(new ApiResponse<>(true, "Debt status updated successfully", updatedDebt));
    }

    // ==========================================================
    // =============== DELETE OPERATIONS ========================
    // ==========================================================

    /**
     * Xóa một khoản nợ khỏi hệ thống
     * URL: DELETE /api/debts/{id}
     * Quyền: ADMIN, EVM_STAFF
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteDebt(@PathVariable Long id) {
        debtService.deleteDebt(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Debt deleted successfully", null));
    }
    
 
    // ==========================================================
    // =============== STATISTICS (THỐNG KÊ) ====================
    // ==========================================================

    /**
     * Lấy thống kê nợ của một dealer (ví dụ: tổng nợ, nợ đã trả, nợ còn lại, v.v.)
     * URL: GET /api/debts/dealer/{dealerId}/stats
     */
    @GetMapping("/dealer/{dealerId}/stats")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDebtStats(@PathVariable Long dealerId) {
        Map<String, Object> stats = debtService.getDebtStats(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Debt statistics retrieved successfully", stats));
    }

    /**
     * Lấy tổng số tiền nợ chưa thanh toán (outstanding) của một dealer
     * URL: GET /api/debts/dealer/{dealerId}/outstanding
     */
    @GetMapping("/dealer/{dealerId}/outstanding")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Double>> getTotalOutstanding(@PathVariable Long dealerId) {
        Double total = debtService.getTotalOutstandingByDealer(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Total outstanding amount retrieved successfully", total));
    }

    /**
     * Lấy tổng số tiền nợ chưa thanh toán của một khách hàng
     * URL: GET /api/debts/customer/{customerId}/outstanding
     */
    @GetMapping("/customer/{customerId}/outstanding")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Double>> getCustomerOutstanding(@PathVariable Long customerId) {
        Double total = debtService.getTotalOutstandingByCustomer(customerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Customer outstanding amount retrieved successfully", total));
    }

    /**
     * Lấy danh sách lịch trả nợ (DebtSchedule) bị quá hạn của một dealer
     * URL: GET /api/debts/overdue-schedules/{dealerId}
     */
    @GetMapping("/overdue-schedules/{dealerId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<DebtSchedule>>> getOverdueSchedules(@PathVariable Long dealerId) {
        List<DebtSchedule> schedules = debtService.getOverdueSchedules(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Overdue schedules retrieved successfully", schedules));
    }

}
