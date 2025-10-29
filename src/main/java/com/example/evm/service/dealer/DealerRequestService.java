package com.example.evm.service.dealer;

import com.example.evm.dto.dealer.DealerRequestDto;
import com.example.evm.dto.dealer.DealerRequestResponse;
import com.example.evm.dto.dealer.RequestDetailDto;
import com.example.evm.dto.dealer.RequestDetailResponse;
import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.dealer.DealerRequest;
import com.example.evm.entity.dealer.DealerRequestDetail;
import com.example.evm.entity.inventory.InventoryStock;
import com.example.evm.entity.user.User;
import com.example.evm.entity.vehicle.VehicleVariant;
import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.repository.dealer.DealerRepository;
import com.example.evm.repository.dealer.DealerRequestRepository;
import com.example.evm.repository.auth.UserRepository;
import com.example.evm.repository.vehicle.VehicleVariantRepository;
import com.example.evm.repository.inventory.InventoryStockRepository;
import com.example.evm.service.order.OrderService;
import com.example.evm.service.debt.DebtService;
import com.example.evm.dto.order.OrderRequestDto;
import com.example.evm.dto.order.OrderDetailRequestDto;
import com.example.evm.entity.order.Order;
import com.example.evm.entity.debt.Debt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DealerRequestService {

    private final DealerRequestRepository dealerRequestRepository;
    private final DealerRepository dealerRepository;
    private final UserRepository userRepository;
    private final VehicleVariantRepository variantRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final OrderService orderService;
    private final DebtService debtService;

    /**
     * Tạo request mới
     */
    @Transactional
    public DealerRequestResponse createRequest(DealerRequestDto dto) {
        log.info("Creating dealer request for dealer: {}", dto.getDealerId());

        // Validate dealer
        Dealer dealer = dealerRepository.findById(dto.getDealerId())
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found with id: " + dto.getDealerId()));

        // Validate user
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));

        // Create request
        DealerRequest request = new DealerRequest();
        request.setDealer(dealer);
        request.setCreatedBy(user);
        request.setRequestDate(LocalDateTime.now());
        request.setRequiredDate(dto.getRequiredDate());
        
        request.setPriority(dto.getPriority() != null ? dto.getPriority() : "NORMAL");
        
        request.setNotes(dto.getNotes());
        request.setStatus("PENDING");

        // Add details
        for (RequestDetailDto detailDto : dto.getRequestDetails()) {
            VehicleVariant variant = variantRepository.findById(detailDto.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + detailDto.getVariantId()));

            DealerRequestDetail detail = new DealerRequestDetail();
            detail.setDealerRequest(request);
            detail.setVehicleVariant(variant);
            detail.setColor(detailDto.getColor());
            detail.setQuantity(detailDto.getQuantity());
            detail.setUnitPrice(detailDto.getUnitPrice());
            detail.setNotes(detailDto.getNotes());

            request.addRequestDetail(detail);
        }

        // Calculate total
        BigDecimal totalAmount = request.calculateTotalAmount();
        log.info("🔍 Calculated total amount: {} for request with {} details", 
                totalAmount, request.getRequestDetails().size());
        
        // Debug: Log each detail
        for (DealerRequestDetail detail : request.getRequestDetails()) {
            log.info("📋 Detail - Variant: {}, Quantity: {}, UnitPrice: {}, LineTotal: {}", 
                    detail.getVehicleVariant() != null ? detail.getVehicleVariant().getName() : "NULL",
                    detail.getQuantity(),
                    detail.getUnitPrice(),
                    detail.getLineTotal());
        }
        
        request.setTotalAmount(totalAmount);

        DealerRequest savedRequest = dealerRequestRepository.save(request);
        log.info("Created request with ID: {} - Priority: {}", savedRequest.getRequestId(), savedRequest.getPriority());

        return convertToResponseDto(savedRequest);
    }

    /**
     * Lấy tất cả requests
     */
    @Transactional(readOnly = true)
    public List<DealerRequestResponse> getAllRequests() {
        return dealerRequestRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy request theo ID
     */
    @Transactional(readOnly = true)
    public DealerRequestResponse getRequestById(Long id) {
        DealerRequest request = dealerRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));
        return convertToResponseDto(request);
    }

    /**
     * Lấy requests theo dealer ID
     */
    @Transactional(readOnly = true)
    public List<DealerRequestResponse> getRequestsByDealerId(Long dealerId) {
        return dealerRequestRepository.findByDealerDealerId(dealerId).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy requests theo status
     */
    @Transactional(readOnly = true)
    public List<DealerRequestResponse> getRequestsByStatus(String status) {
        return dealerRequestRepository.findByStatus(status).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật status của request
     */
    @Transactional
    public DealerRequestResponse updateRequestStatus(Long id, String status, String approvedBy) {
        log.info("Updating request {} to status: {}", id, status);

        DealerRequest request = dealerRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));

        String oldStatus = request.getStatus();
        request.setStatus(status);

        if ("APPROVED".equals(status)) {
            request.setApprovedDate(LocalDateTime.now());
            request.setApprovedBy(approvedBy);
            log.info("Request {} APPROVED by {}", id, approvedBy);
        } else if ("SHIPPED".equals(status)) {
            request.setShippedDate(LocalDateTime.now());
            log.info("Request {} SHIPPED", id);
        } else if ("DELIVERED".equals(status)) {
            request.setDeliveryDate(LocalDateTime.now());
            // Add vehicles to dealer stock
            addStockToDealerOnDelivery(request);
            
            // ✅ TỰ ĐỘNG tạo Order và Debt khi giao hàng
            try {
                createOrderAndDebtFromRequest(request);
                log.info("Request {} DELIVERED - Order and Debt created automatically", id);
            } catch (Exception e) {
                log.error("Failed to create Order/Debt for request {}: {}", id, e.getMessage());
                // Không throw exception để không ảnh hưởng đến việc cập nhật status
            }
        }

        DealerRequest updated = dealerRequestRepository.save(request);
        log.info("Updated request status from {} to {}", oldStatus, status);

        return convertToResponseDto(updated);
    }

    /**
     * ✅ Tạo Order và Debt tự động từ DealerRequest khi giao hàng
     */
    @Transactional
    private void createOrderAndDebtFromRequest(DealerRequest request) {
        log.info("Creating Order and Debt from DealerRequest: {}", request.getRequestId());
        
        // 1. Tạo Order từ DealerRequest
        Order order = createOrderFromDealerRequest(request);
        log.info("✅ Created Order: {} for DealerRequest: {}", order.getOrderId(), request.getRequestId());
        
        // 2. Tạo Debt từ Order (nếu payment_type = INSTALLMENT)
        // Mặc định tạo debt với payment_type = INSTALLMENT cho dealer
        createDebtFromOrder(order, request);
        log.info("✅ Created Debt for Order: {}", order.getOrderId());
    }

    /**
     * Tạo Order từ DealerRequest
     */
    private Order createOrderFromDealerRequest(DealerRequest request) {
        // Tạo OrderRequestDto từ DealerRequest
        OrderRequestDto orderDto = new OrderRequestDto();
        orderDto.setDealerId(request.getDealer().getDealerId());
        orderDto.setUserId(request.getCreatedBy().getUserId());
        orderDto.setCustomerId(null); // Dealer request không có customer cụ thể
        orderDto.setPaymentMethod("INSTALLMENT"); // Mặc định trả góp cho dealer
        
        // Tạo OrderDetail từ DealerRequestDetail
        List<OrderDetailRequestDto> orderDetails = request.getRequestDetails().stream()
                .map(detail -> {
                    OrderDetailRequestDto orderDetail = new OrderDetailRequestDto();
                    // Tìm vehicle từ variant
                    Long vehicleId = findVehicleByVariant(detail.getVehicleVariant().getVariantId());
                    orderDetail.setVehicleId(vehicleId);
                    orderDetail.setQuantity(detail.getQuantity());
                    orderDetail.setPrice(detail.getUnitPrice().doubleValue());
                    return orderDetail;
                })
                .collect(Collectors.toList());
        
        orderDto.setOrderDetails(orderDetails);
        
        // Tạo Order
        return orderService.createOrderFromDto(orderDto);
    }

    /**
     * Tạo Debt từ Order
     */
    private void createDebtFromOrder(Order order, DealerRequest request) {
        Debt debt = new Debt();
        
        // Set thông tin cơ bản
        debt.setDealer(order.getDealer());
        debt.setUser(order.getUser());
        debt.setCustomer(order.getCustomer()); // Có thể null nếu là dealer order
        
        // Tính tổng tiền từ order
        double totalAmount = order.getOrderDetails().stream()
                .mapToDouble(detail -> detail.getPrice() * detail.getQuantity())
                .sum();
        
        debt.setAmountDue(BigDecimal.valueOf(totalAmount));
        debt.setAmountPaid(BigDecimal.ZERO);
        debt.setPaymentMethod("BANK_TRANSFER");
        debt.setDebtType("DEALER_DEBT"); // Dealer nợ EVM
        debt.setStatus("ACTIVE");
        debt.setNotes("Auto-generated from DealerRequest: " + request.getRequestId());
        debt.setStartDate(LocalDateTime.now());
        debt.setDueDate(LocalDateTime.now().plusMonths(12)); // 12 tháng trả góp
        
        // Tạo Debt
        debtService.createDebt(debt);
    }

    /**
     * Tìm vehicle ID từ variant ID
     * TODO: Implement proper mapping logic
     */
    private Long findVehicleByVariant(Long variantId) {
        // Tạm thời return variantId làm vehicleId
        // Trong thực tế cần query database để tìm vehicle tương ứng với variant
        // vehicleRepository.findByVariantId(variantId).orElse(variantId);
        return variantId;
    }

    /**
     * Thêm xe vào kho dealer khi giao hàng
     */
    private void addStockToDealerOnDelivery(DealerRequest request) {
        // Lấy hoặc tạo InventoryStock cho dealer
        inventoryStockRepository.findByDealerDealerId(request.getDealer().getDealerId())
                .orElseGet(() -> {
                    InventoryStock newStock = InventoryStock.builder()
                            .dealer(request.getDealer())
                            .status("ACTIVE")
                            .build();
                    return inventoryStockRepository.save(newStock);
                });

        log.info("✅ Dealer {} stock prepared for delivery", request.getDealer().getDealerId());
        
        // Note: Với schema mới (Vehicle-centric), việc cộng xe vào kho
        // sẽ được xử lý bởi InventoryService khi allocate vehicles
        // Ở đây chỉ đảm bảo dealer có warehouse
    }

    /**
     * Xóa request
     */
    @Transactional
    public void deleteRequest(Long id) {
        DealerRequest request = dealerRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));

        if (!"PENDING".equals(request.getStatus()) && !"REJECTED".equals(request.getStatus())) {
            throw new IllegalStateException("Cannot delete request with status: " + request.getStatus());
        }

        dealerRequestRepository.delete(request);
        log.info("Deleted request with ID: {}", id);
    }

    /**
     * Convert entity to response DTO
     */
    private DealerRequestResponse convertToResponseDto(DealerRequest request) {
        DealerRequestResponse response = new DealerRequestResponse();
        response.setRequestId(request.getRequestId());
        
        // Dealer & User info
        response.setDealerId(request.getDealer().getDealerId());
        response.setDealerName(request.getDealer().getDealerName());
        response.setUserId(request.getCreatedBy().getUserId());
        response.setUserFullName(request.getCreatedBy().getFullName());
        response.setUserRole(request.getCreatedBy().getRole());
        
        // Request info
        response.setRequestDate(request.getRequestDate());
        response.setRequiredDate(request.getRequiredDate());
        response.setStatus(request.getStatus());
        
        response.setPriority(request.getPriority());
        
        response.setNotes(request.getNotes());
        response.setTotalAmount(request.getTotalAmount());
        
        // Workflow tracking
        response.setApprovedDate(request.getApprovedDate());
        response.setApprovedBy(request.getApprovedBy());
        response.setShippedDate(request.getShippedDate());
        response.setDeliveryDate(request.getDeliveryDate());
        
        // Details
        response.setRequestDetails(getRequestDetails(request));
        
        return response;
    }

    /**
     * Convert request details to response DTOs
     */
    private List<RequestDetailResponse> getRequestDetails(DealerRequest request) {
        return request.getRequestDetails().stream()
                .map(detail -> {
                    RequestDetailResponse detailResponse = new RequestDetailResponse();
                    detailResponse.setDetailId(detail.getDetailId());
                    detailResponse.setVariantId(detail.getVehicleVariant().getVariantId());
                    detailResponse.setVariantName(detail.getVehicleVariant().getName());
                    detailResponse.setModelName(detail.getVehicleVariant().getModel().getName());
                    detailResponse.setColor(detail.getColor());
                    detailResponse.setQuantity(detail.getQuantity());
                    detailResponse.setUnitPrice(detail.getUnitPrice());
                    detailResponse.setLineTotal(detail.getLineTotal());
                    return detailResponse;
                })
                .collect(Collectors.toList());
    }

    /**
     * Lấy order của request
     */
    public Object getRequestOrder(Long requestId) {
        DealerRequest request = dealerRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        
        log.info("🔍 Getting order for request {} - Status: {} - Total: {} - Dealer: {}", 
                requestId, request.getStatus(), request.getTotalAmount(), request.getDealer().getDealerId());
        
        // Kiểm tra request có status DELIVERED không
        if (!"DELIVERED".equals(request.getStatus())) {
            throw new RuntimeException("Request must be DELIVERED to have an order. Current status: " + request.getStatus());
        }
        
        // Tìm order theo dealer_id và total_amount tương ứng
        List<Order> orders = orderService.getOrdersByDealer(request.getDealer().getDealerId());
        log.info("📦 Found {} orders for dealer {}", orders.size(), request.getDealer().getDealerId());
        
        // Convert Order.totalPrice (Double) to BigDecimal for comparison
        BigDecimal requestTotalAmount = request.getTotalAmount();
        
        // Debug: Log tất cả orders
        for (Order order : orders) {
            BigDecimal orderTotalPrice = BigDecimal.valueOf(order.getTotalPrice());
            int comparison = orderTotalPrice.compareTo(requestTotalAmount);
            log.info("📋 Order {} - Total: {} - Compare: {}", 
                    order.getOrderId(), 
                    orderTotalPrice.toString(),
                    comparison);
        }
        
        // Tìm order có total_amount khớp với request (dùng compareTo thay vì equals)
        Order matchingOrder = orders.stream()
                .filter(order -> {
                    BigDecimal orderTotalPrice = BigDecimal.valueOf(order.getTotalPrice());
                    boolean matches = orderTotalPrice.compareTo(requestTotalAmount) == 0;
                    log.info("🔍 Order {} matches: {} ({} vs {})", 
                            order.getOrderId(), matches, orderTotalPrice.toString(), requestTotalAmount.toString());
                    return matches;
                })
                .findFirst()
                .orElse(null);
        
        if (matchingOrder == null) {
            log.error("❌ No matching order found for request {} - Dealer: {} - Total: {}", 
                    requestId, request.getDealer().getDealerId(), request.getTotalAmount());
            throw new RuntimeException("No order found for this request. Check logs for details.");
        }
        
        log.info("✅ Found matching order: {}", matchingOrder.getOrderId());
        return matchingOrder;
    }
}

