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
import com.example.evm.entity.payment.Payment;
import com.example.evm.repository.payment.PaymentRepository;
import com.example.evm.repository.vehicle.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import com.example.evm.util.DateTimeUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class DealerRequestService {

    private final DealerRequestRepository dealerRequestRepository;
    private final DealerRepository dealerRepository;
    private final UserRepository userRepository;
    private final VehicleVariantRepository variantRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final VehicleRepository vehicleRepository;
    private final PaymentRepository paymentRepository;
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
        request.setRequestDate(DateTimeUtils.nowVietnam());
        request.setRequiredDate(dto.getRequiredDate());
        
        request.setPriority(dto.getPriority() != null ? dto.getPriority() : "NORMAL");
        
        request.setNotes(dto.getNotes());
        request.setStatus("PENDING");

        // Add details
        for (RequestDetailDto detailDto : dto.getRequestDetails()) {
            // Variant ID là optional - chỉ tìm variant nếu được cung cấp
            VehicleVariant variant = null;
            if (detailDto.getVariantId() != null) {
                variant = variantRepository.findById(detailDto.getVariantId())
                        .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + detailDto.getVariantId()));
            }

            DealerRequestDetail detail = new DealerRequestDetail();
            detail.setDealerRequest(request);
            detail.setVehicleVariant(variant);  // Có thể là null
            detail.setColor(detailDto.getColor());
            detail.setQuantity(detailDto.getQuantity());
            detail.setUnitPrice(detailDto.getUnitPrice());
            detail.setNotes(detailDto.getNotes());

            request.addRequestDetail(detail);
        }

        // Calculate total
        BigDecimal totalAmount = request.calculateTotalAmount();
        log.info("Calculated total amount: {} for request with {} details", 
                totalAmount, request.getRequestDetails().size());
        
        // Debug: Log each detail
        for (DealerRequestDetail detail : request.getRequestDetails()) {
            log.info("Detail - Variant: {}, Quantity: {}, UnitPrice: {}, LineTotal: {}", 
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
            request.setApprovedDate(DateTimeUtils.nowVietnam());
            request.setApprovedBy(approvedBy);
            log.info("Request {} APPROVED by {}", id, approvedBy);
        } else if ("SHIPPED".equals(status)) {
            request.setShippedDate(DateTimeUtils.nowVietnam());
            log.info("Request {} SHIPPED", id);
        } else if ("DELIVERED".equals(status)) {
            request.setDeliveryDate(DateTimeUtils.nowVietnam());
            // Add vehicles to dealer stock
            addStockToDealerOnDelivery(request);
            
            // TỰ ĐỘNG tạo Order khi giao hàng (KHÔNG tạo Debt - chờ thanh toán)
            try {
                // Luôn tạo Order từ kho dealer khi xác nhận đã nhận
                Order createdOrder = createOrderFromRequestUsingDealerStock(
                        request.getRequestId(),
                        request.getCreatedBy().getUserId(),
                        "BANK_TRANSFER");
                log.info("Created Order {} from DealerRequest {} upon DELIVERED (Debt will be created after payment)", 
                        createdOrder.getOrderId(), request.getRequestId());

                // BỎ: Không tạo Debt ngay - chờ thanh toán xong
                // createDebtFromExistingOrder(request);
            } catch (Exception e) {
                log.error("Failed to create Order for request {}: {}", id, e.getMessage());
                // Không throw exception để không ảnh hưởng đến việc cập nhật status
            }
        }

        DealerRequest updated = dealerRequestRepository.save(request);
        log.info("Updated request status from {} to {}", oldStatus, status);

        return convertToResponseDto(updated);
    }

    /**
     * Tạo Debt từ Order có sẵn khi DealerRequest giao hàng
     * Order phải được tạo trước bởi Dealer
     */
    @Transactional
    private void createDebtFromExistingOrder(DealerRequest request) {
        log.info("Creating Debt from existing Order for DealerRequest: {}", request.getRequestId());
        
        // 1. Tìm Order liên kết với DealerRequest (Order phải được tạo trước)
        Order order = findOrderByDealerRequest(request);
        if (order == null) {
            throw new IllegalArgumentException("Order must be created before processing DealerRequest. Please create Order first.");
        }
        
        log.info("Found Order: {} for DealerRequest: {}", order.getOrderId(), request.getRequestId());
// 2. Tạo Debt từ Order (nếu payment_type = INSTALLMENT)
        createDebtFromOrder(order, request);
        log.info("Created Debt for Order: {}", order.getOrderId());
    }

    /**
     * Tìm Order liên kết với DealerRequest
     * Order phải được tạo trước bởi Dealer
     */
    private Order findOrderByDealerRequest(DealerRequest request) {
        // Tìm Order theo dealer và thời gian tạo gần nhất
        // Trong thực tế có thể cần thêm field order_id vào DealerRequest
        List<Order> orders = orderService.getOrdersByDealer(request.getDealer().getDealerId());
        
        // Tìm Order được tạo gần nhất (tạm thời)
        return orders.stream()
                .filter(order -> order.getCustomer() == null) // Dealer order (không có customer)
                .max(Comparator.comparing(Order::getCreatedDate))
                .orElse(null);
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
        
        // FIX: Tính ngay amountPaid từ payment của order này
        BigDecimal amountPaid = BigDecimal.ZERO;
        try {
            List<Payment> payments = paymentRepository.findAllByOrderId(order.getOrderId());
            amountPaid = payments.stream()
                    .filter(p -> "Completed".equalsIgnoreCase(p.getStatus()))
                    .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            log.info(" Found {} completed payments for Order {}: Total={}", 
                    payments.stream().filter(p -> "Completed".equalsIgnoreCase(p.getStatus())).count(),
                    order.getOrderId(), amountPaid);
        } catch (Exception e) {
            log.error(" Failed to calculate amountPaid from Order {}: {}", order.getOrderId(), e.getMessage());
        }
        
        debt.setAmountDue(BigDecimal.valueOf(totalAmount));
        debt.setAmountPaid(amountPaid); // Set ngay từ đầu
        debt.setPaymentMethod("BANK_TRANSFER");
        debt.setDebtType("DEALER_DEBT"); // Dealer nợ EVM
        debt.setStatus("ACTIVE");
        // FIX: Thêm orderId vào notes để có thể tìm được Order sau này
        debt.setNotes("Auto-generated from DealerRequest: " + request.getRequestId() + " - Order: " + order.getOrderId());
        debt.setStartDate(DateTimeUtils.nowVietnam());
        debt.setDueDate(DateTimeUtils.nowVietnam().plusMonths(12)); // 12 tháng trả góp
        
        // Tạo Debt
        debtService.createDebt(debt);
        
        log.info(" Created DEALER_DEBT from DealerRequest {}, Order {}, Amount: {}, AmountPaid: {}", 
                request.getRequestId(), order.getOrderId(), totalAmount, amountPaid);
    }

    /**
     * Tạo Order từ DealerRequest (khi còn PENDING/APPROVED) để tiến hành thanh toán
     * - Chọn các xe phù hợp từ kho tổng theo variant + color (nếu có)
     * - Mỗi xe là một dòng OrderDetail (quantity = 1)
     * - Cho phép tạo Order ngay cả khi không có xe trong kho (chỉ tạo OrderDetail cho xe có sẵn)
     */
    @Transactional
    public Order createOrderFromRequest(Long requestId, Long userId, String paymentMethod) {
        DealerRequest request = dealerRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + requestId));

        // Chỉ cho phép khi chưa DELIVERED
        if ("DELIVERED".equals(request.getStatus())) {
            throw new IllegalStateException("Request already delivered. Creating order is not allowed.");
        }

        // Build DTO
        OrderRequestDto dto = new OrderRequestDto();
        dto.setCustomerId(null); // Dealer order
        dto.setUserId(userId);
        dto.setDealerId(request.getDealer().getDealerId());
        dto.setPaymentMethod(paymentMethod);

        List<OrderDetailRequestDto> detailDtos = new java.util.ArrayList<>();

        for (DealerRequestDetail d : request.getRequestDetails()) {
            // Bỏ qua details không có variantId - không thể tìm xe
            if (d.getVehicleVariant() == null) {
                log.warn("Skipping detail {} - no variantId specified. Cannot create order without variant.", d.getDetailId());
                continue;
            }
            
            // Tìm xe available theo variant + color
            List<com.example.evm.entity.vehicle.Vehicle> available = vehicleRepository
                    .findAvailableInManufacturerStock(d.getVehicleVariant().getVariantId(), d.getColor());

            // Sửa: Không throw exception nếu không đủ xe, chỉ tạo OrderDetail cho những xe có sẵn
            int availableCount = available.size();
            int requestedQuantity = d.getQuantity();
            
            if (availableCount < requestedQuantity) {
                int shortage = requestedQuantity - availableCount;
                log.warn("Not enough vehicles for variant {} (color {}). Requested: {}, Available: {}, Shortage: {}. " +
                        "Creating order with available vehicles only.",
                        d.getVehicleVariant().getName(), d.getColor(), requestedQuantity, availableCount, shortage);
            }

            // Chỉ tạo OrderDetail cho những xe có sẵn (nếu có)
            int quantityToProcess = Math.min(availableCount, requestedQuantity);
            for (int i = 0; i < quantityToProcess; i++) {
                com.example.evm.entity.vehicle.Vehicle v = available.get(i);
                OrderDetailRequestDto od = new OrderDetailRequestDto(
                        v.getVehicleId(),
                        null,
                        1,
                        d.getUnitPrice().doubleValue()
                );
                detailDtos.add(od);
            }
        }

        // Kiểm tra: Nếu không có OrderDetail nào (không có xe nào), vẫn tạo Order nhưng log warning
        if (detailDtos.isEmpty()) {
            log.warn("No vehicles available for request {}. Creating order without order details. " +
                    "Order will be created with totalPrice = 0 and can be updated when vehicles become available.", requestId);
        }

        dto.setOrderDetails(detailDtos);
        Order createdOrder = orderService.createOrderFromDto(dto);
        
        log.info("Order {} created from request {}. OrderDetails: {}, TotalPrice: {}", 
                createdOrder.getOrderId(), requestId, detailDtos.size(), createdOrder.getTotalPrice());
        
        return createdOrder;
    }

    /**
     * Tạo Order dựa trên xe đã có trong kho dealer (sau khi allocate, trước/sau khi deliver)
     */
    @Transactional
    public Order createOrderFromRequestUsingDealerStock(Long requestId, Long userId, String paymentMethod) {
        DealerRequest request = dealerRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + requestId));

        Long dealerId = request.getDealer().getDealerId();

        // Build DTO
        OrderRequestDto dto = new OrderRequestDto();
        dto.setCustomerId(null);
        dto.setUserId(userId);
        dto.setDealerId(dealerId);
        dto.setPaymentMethod(paymentMethod);

        List<OrderDetailRequestDto> detailDtos = new java.util.ArrayList<>();

        // Lấy tất cả xe trong kho dealer một lần để filter
        List<com.example.evm.entity.vehicle.Vehicle> dealerVehicles = vehicleRepository
                .findByDealerIdWithFullInfo(dealerId);

        for (DealerRequestDetail d : request.getRequestDetails()) {
            //  Bỏ qua details không có variantId - không thể match xe
            if (d.getVehicleVariant() == null) {
                log.warn(" Skipping detail {} - no variantId specified. Cannot match vehicles without variant.", d.getDetailId());
                continue;
            }
            
            List<com.example.evm.entity.vehicle.Vehicle> matched = dealerVehicles.stream()
                    .filter(v -> v.getVariant() != null
                            && v.getVariant().getVariantId().equals(d.getVehicleVariant().getVariantId())
                            && v.getColor() != null
                            && v.getColor().equalsIgnoreCase(d.getColor() != null ? d.getColor() : v.getColor())
                            && "IN_DEALER_STOCK".equalsIgnoreCase(v.getStatus()))
                    .limit(d.getQuantity())
                    .toList();

            // Sửa: Không throw exception nếu không đủ xe, chỉ tạo OrderDetail cho những xe có sẵn
            int matchedCount = matched.size();
            int requestedQuantity = d.getQuantity();
            
            if (matchedCount < requestedQuantity) {
                int shortage = requestedQuantity - matchedCount;
                log.warn("Not enough vehicles in dealer stock for variant {} (color {}). " +
                        "Requested: {}, Available: {}, Shortage: {}. " +
                        "Creating order with available vehicles only.",
                        d.getVehicleVariant().getName(), d.getColor(), requestedQuantity, matchedCount, shortage);
            }

            // Chỉ tạo OrderDetail cho những xe có sẵn (nếu có)
            for (com.example.evm.entity.vehicle.Vehicle v : matched) {
                OrderDetailRequestDto od = new OrderDetailRequestDto(
                        v.getVehicleId(),
                        null,
                        1,
                        d.getUnitPrice().doubleValue()
                );
                detailDtos.add(od);
            }
        }

        // Kiểm tra: Nếu không có OrderDetail nào (không có xe nào), vẫn tạo Order nhưng log warning
        if (detailDtos.isEmpty()) {
            log.warn("No vehicles available in dealer stock for request {}. Creating order without order details. " +
                    "Order will be created with totalPrice = 0 and can be updated when vehicles become available.", requestId);
        }

        dto.setOrderDetails(detailDtos);
        Order createdOrder = orderService.createOrderFromDto(dto);
        
        log.info("Order {} created from request {} using dealer stock. OrderDetails: {}, TotalPrice: {}", 
                createdOrder.getOrderId(), requestId, detailDtos.size(), createdOrder.getTotalPrice());
        
        return createdOrder;
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

        log.info(" Dealer {} stock prepared for delivery", request.getDealer().getDealerId());
        
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
                    // Xử lý trường hợp variant có thể null
                    if (detail.getVehicleVariant() != null) {
                        detailResponse.setVariantId(detail.getVehicleVariant().getVariantId());
                        detailResponse.setVariantName(detail.getVehicleVariant().getName());
                        if (detail.getVehicleVariant().getModel() != null) {
                            detailResponse.setModelName(detail.getVehicleVariant().getModel().getName());
                        }
                    } else {
                        detailResponse.setVariantId(null);
                        detailResponse.setVariantName(null);
                        detailResponse.setModelName(null);
                    }
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
        
        log.info(" Getting order for request {} - Status: {} - Total: {} - Dealer: {}", 
                requestId, request.getStatus(), request.getTotalAmount(), request.getDealer().getDealerId());
        
        // Kiểm tra request có status DELIVERED không
if (!"DELIVERED".equals(request.getStatus())) {
            throw new RuntimeException("Request must be DELIVERED to have an order. Current status: " + request.getStatus());
        }
        
        // Tìm order theo dealer_id và total_amount tương ứng
        List<Order> orders = orderService.getOrdersByDealer(request.getDealer().getDealerId());
        log.info(" Found {} orders for dealer {}", orders.size(), request.getDealer().getDealerId());
        
        // Convert Order.totalPrice (Double) to BigDecimal for comparison
        BigDecimal requestTotalAmount = request.getTotalAmount();
        
        // Debug: Log tất cả orders
        for (Order order : orders) {
            BigDecimal orderTotalPrice = BigDecimal.valueOf(order.getTotalPrice());
            int comparison = orderTotalPrice.compareTo(requestTotalAmount);
            log.info(" Order {} - Total: {} - Compare: {}", 
                    order.getOrderId(), 
                    orderTotalPrice.toString(),
                    comparison);
        }
        
        // Tìm order có total_amount khớp với request (dùng compareTo thay vì equals)
        Order matchingOrder = orders.stream()
                .filter(order -> {
                    BigDecimal orderTotalPrice = BigDecimal.valueOf(order.getTotalPrice());
                    boolean matches = orderTotalPrice.compareTo(requestTotalAmount) == 0;
                    log.info(" Order {} matches: {} ({} vs {})", 
                            order.getOrderId(), matches, orderTotalPrice.toString(), requestTotalAmount.toString());
                    return matches;
                })
                .findFirst()
                .orElse(null);
        
        if (matchingOrder == null) {
            log.error(" No matching order found for request {} - Dealer: {} - Total: {}", 
                    requestId, request.getDealer().getDealerId(), request.getTotalAmount());
            throw new RuntimeException("No order found for this request. Check logs for details.");
        }
        
        log.info(" Found matching order: {}", matchingOrder.getOrderId());
        return matchingOrder;
    }
}