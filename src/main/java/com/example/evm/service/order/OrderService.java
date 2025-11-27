package com.example.evm.service.order;


import com.example.evm.dto.order.OrderRequestDto;
import com.example.evm.dto.order.OrderDetailRequestDto;
import com.example.evm.entity.order.Order;
import com.example.evm.entity.order.OrderDetail;
import com.example.evm.entity.customer.Customer;
import com.example.evm.entity.user.User;
import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.vehicle.Vehicle;
import com.example.evm.entity.promotion.Promotion;
import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.repository.order.OrderRepository;
import com.example.evm.repository.order.OrderDetailRepository;
import com.example.evm.repository.auth.UserRepository;
import com.example.evm.repository.customer.CustomerRepository;

import com.example.evm.repository.dealer.DealerRepository;
import com.example.evm.repository.vehicle.VehicleRepository;
import com.example.evm.repository.promotion.PromotionRepository;
import com.example.evm.repository.payment.PaymentRepository;
import com.example.evm.repository.debt.DebtRepository;
import com.example.evm.entity.debt.Debt;
import com.example.evm.service.debt.DebtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final DealerRepository dealerRepository;
    private final VehicleRepository vehicleRepository;
    private final PromotionRepository promotionRepository;
    private final PaymentRepository paymentRepository;
    private final DebtRepository debtRepository;
    private final DebtService debtService;

    public List<Order> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        enrichOrdersWithAmountPaid(orders);
        return orders;
    }

    public List<Order> getOrdersWithoutContract(Long dealerId) {
        List<Order> orders = orderRepository.findOrdersWithoutContractByDealer(dealerId);
        //  Eager load orderDetails để trả về orderDetailId
        orders.forEach(order -> {
            order.getOrderDetails().size(); // Force load orderDetails
        });
        enrichOrdersWithAmountPaid(orders);
        return orders;
    }

    public List<Order> getOrdersByDealer(Long dealerId) {
        List<Order> orders = orderRepository.findByDealerDealerId(dealerId);
        enrichOrdersWithAmountPaid(orders);
        return orders;
    }

    public List<Order> getOrdersByCustomer(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerCustomerId(customerId);
        enrichOrdersWithAmountPaid(orders);
        return orders;
    }

    public List<Order> getOrdersByStatus(String status) {
        List<Order> orders = orderRepository.findByStatus(status);
        enrichOrdersWithAmountPaid(orders);
        return orders;
    }

    public List<Order> getOrdersByDealerAndStatus(Long dealerId, String status) {
        List<Order> orders = orderRepository.findByDealerAndStatus(dealerId, status);
        enrichOrdersWithAmountPaid(orders);
        return orders;
    }

    public Order getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        calculateAmountPaidForOrder(order);
        return order;
    }

    public Double getTotalSalesByDealer(Long dealerId) {
        Double totalSales = orderRepository.getTotalSalesByDealer(dealerId);
        return totalSales != null ? totalSales : 0.0;
    }

    public Long countOrdersByDealerAndStatus(Long dealerId, String status) {
        return orderRepository.countByDealerAndStatus(dealerId, status);
    }

    /**
     *  Tính số tiền đã thanh toán cho một Order
     * Logic: Ưu tiên từ Debt (cho INSTALLMENT), sau đó từ Payment
     */
    private void calculateAmountPaidForOrder(Order order) {
        Double amountPaid = 0.0;
        
        // 1. Kiểm tra Debt trước (cho INSTALLMENT payments - đã được tạo tự động)
        if (order.getCustomer() != null && order.getDealer() != null) {
            try {
                List<Debt> debts = debtRepository.findByCustomerCustomerIdAndDealerDealerIdAndStatus(
                        order.getCustomer().getCustomerId(),
                        order.getDealer().getDealerId(),
                        "ACTIVE"
                );
                // Tìm debt có notes chứa orderId
                String orderIdStr = "Order: " + order.getOrderId();
                for (Debt debt : debts) {
                    if (debt.getNotes() != null && debt.getNotes().contains(orderIdStr)) {
                        //  Nếu Debt có amountPaid > 0, dùng nó (đã bao gồm số tiền ban đầu + DebtPayments)
                        if (debt.getAmountPaid() != null && debt.getAmountPaid().compareTo(BigDecimal.ZERO) > 0) {
                            amountPaid = debt.getAmountPaid().doubleValue();
                            log.debug(" Found Debt for Order {}: amountPaid = {}", order.getOrderId(), amountPaid);
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                log.warn(" Error checking Debt for Order {}: {}", order.getOrderId(), e.getMessage());
            }
        }
        
        // 2. Nếu Debt không tồn tại hoặc amountPaid = 0, kiểm tra Payment trực tiếp
        // (Có thể Payment Pending chưa tạo Debt, hoặc Debt.amountPaid chưa được cập nhật)
        if (amountPaid == 0.0 || amountPaid == null) {
            try {
                BigDecimal completedAmount = paymentRepository.sumCompletedAmountByOrderId(order.getOrderId());
                if (completedAmount != null && completedAmount.compareTo(BigDecimal.ZERO) > 0) {
                    amountPaid = completedAmount.doubleValue();
                    log.debug(" Sum completed payments for Order {} = {}", order.getOrderId(), completedAmount);
                } else {
                    log.debug(" No completed Payment found for Order {}", order.getOrderId());
                }
            } catch (Exception e) {
                log.warn(" Error checking Payment for Order {}: {}", order.getOrderId(), e.getMessage());
            }
        }
        
        // 3. Đảm bảo amountPaid không null
        if (amountPaid == null) {
            amountPaid = 0.0;
        }

        if (order.getTotalPrice() != null && amountPaid > order.getTotalPrice()) {
            log.warn(" Amount paid {} exceeds order total {} for order {}, capping to total.", amountPaid, order.getTotalPrice(), order.getOrderId());
            amountPaid = order.getTotalPrice();
        }
        
        order.setAmountPaid(amountPaid);
        log.debug(" Order {} final amountPaid = {}", order.getOrderId(), amountPaid);
    }

    /**
     *  Tính số tiền đã thanh toán cho danh sách Orders
     */
    private void enrichOrdersWithAmountPaid(List<Order> orders) {
        for (Order order : orders) {
            calculateAmountPaidForOrder(order);
        }
    }

    public List<OrderDetail> getOrderDetails(Long orderId) {
        return orderDetailRepository.findByOrderOrderId(orderId);
    }

    /**
     *  Tạo Order từ DTO - chỉ cần truyền IDs, backend sẽ mock hết thông tin
     */
    @Transactional
    public Order createOrderFromDto(OrderRequestDto dto) {
        // Lookup entities từ IDs
        Customer customer = null;
        if (dto.getCustomerId() != null) {
            customer = customerRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + dto.getCustomerId()));
        }
        
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));
        
        Dealer dealer = dealerRepository.findById(dto.getDealerId())
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found with id: " + dto.getDealerId()));

        // Tạo Order entity
        Order order = new Order();
        order.setCustomer(customer); // Có thể null cho dealer orders
        order.setUser(user);
        order.setDealer(dealer);
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setCreatedDate(LocalDateTime.now());
        order.setStatus("PENDING");

        // Process order details
        List<OrderDetail> orderDetails = new ArrayList<>();
        double totalPrice = 0.0;
        
        for (OrderDetailRequestDto detailDto : dto.getOrderDetails()) {
            Vehicle vehicle = vehicleRepository.findById(detailDto.getVehicleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + detailDto.getVehicleId()));

            // 🚫 NGHIỆP VỤ MỚI: Xe lái thử không thể bán
            if ("TEST_DRIVE".equalsIgnoreCase(vehicle.getStatus())) {
                throw new IllegalStateException("🚫 Xe lái thử (VIN: " + vehicle.getVinNumber() + ") không thể được đặt hàng hoặc bán.");
            }        
            
            OrderDetail detail = new OrderDetail();
            detail.setVehicle(vehicle);
            detail.setQuantity(detailDto.getQuantity());
            detail.setPrice(detailDto.getPrice());
            detail.setOrder(order);

            // Apply promotion if exists
            if (detailDto.getPromotionId() != null) {
                Promotion promotion = promotionRepository.findById(detailDto.getPromotionId())
                        .orElseThrow(() -> new ResourceNotFoundException("Promotion not found with id: " + detailDto.getPromotionId()));
                detail.setPromotion(promotion);
                
                // Apply discount
                if (promotion.getDiscountRate() != null) {
                    double discountedPrice = detail.getPrice() * (1 - promotion.getDiscountRate() / 100);
                    detail.setPrice(discountedPrice);
                }
            }

            orderDetails.add(detail);
            totalPrice += detail.getPrice() * detail.getQuantity();
        }

        order.setOrderDetails(orderDetails);
        order.setTotalPrice(totalPrice);
        
        Order savedOrder = orderRepository.save(order);
        
        log.info("Order created from DTO: ID {} - Customer: {} - Total: {}", 
                savedOrder.getOrderId(), customer != null ? customer.getCustomerName() : "Dealer Order", totalPrice);
        
        return savedOrder;
    }

    /**
     *  API cũ - Deprecated, dùng createOrderFromDto thay thế
     */
    @Transactional
    @Deprecated
    public Order createOrder(Order order) {
        //  Backend tự tạo IDs - Force null
        order.setOrderId(null);
        
        // Validate entities
        Customer customer = customerRepository.findById(order.getCustomer().getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        
        User user = userRepository.findById(order.getUser().getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Dealer dealer = dealerRepository.findById(order.getDealer().getDealerId())
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));

        order.setCustomer(customer);
        order.setUser(user);
        order.setDealer(dealer);
        order.setCreatedDate(LocalDateTime.now());

        // Process order details and calculate total
        double totalPrice = 0.0;
        for (OrderDetail detail : order.getOrderDetails()) {
            //  Backend tự tạo detail IDs - Force null
            detail.setOrderDetailId(null);
            
            Vehicle vehicle = vehicleRepository.findById(detail.getVehicle().getVehicleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
            
            detail.setVehicle(vehicle);
            detail.setOrder(order);

            // Apply promotion if exists
            if (detail.getPromotion() != null && detail.getPromotion().getPromoId() != null) {
                Promotion promotion = promotionRepository.findById(detail.getPromotion().getPromoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));
                detail.setPromotion(promotion);
                
                // Apply discount
                if (promotion.getDiscountRate() != null) {
                    double discountedPrice = detail.getPrice() * (1 - promotion.getDiscountRate() / 100);
                    detail.setPrice(discountedPrice);
                }
            }

            totalPrice += detail.getPrice() * detail.getQuantity();
        }

        order.setTotalPrice(totalPrice);
        Order savedOrder = orderRepository.save(order);
        
        log.info("Order created: ID {} - Customer: {} - Total: {}", 
                savedOrder.getOrderId(), customer != null ? customer.getCustomerName() : "Dealer Order", totalPrice);
        
        return savedOrder;
    }

   @Transactional
   public Order updateOrderStatus(Long id, String status) {
    Order order = getOrderById(id);
    order.setStatus(status);

    try {
        List<com.example.evm.entity.payment.Payment> payments = paymentRepository
            .findAllByOrderId(id);

        if (!payments.isEmpty()) {
            String latestPaymentMethod = payments.get(0).getPaymentMethod();
            order.setPaymentMethod(latestPaymentMethod);
            log.info("Updated order {} paymentMethod to: {}", id, latestPaymentMethod);
        }
    } catch (Exception e) {
        log.warn("Could not update paymentMethod for order {}: {}", id, e.getMessage());
    }

    // Thêm logic: Khi SHIPPED, cập nhật vehicle
    if ("SHIPPED".equals(status)) {
        List<OrderDetail> orderDetails = getOrderDetails(id);
        for (OrderDetail detail : orderDetails) {
            Vehicle vehicle = detail.getVehicle();
            if (vehicle != null) {
                vehicle.setStatus("SOLD");
                vehicle.setInventoryStock(null); // Loại khỏi kho đại lý
                vehicleRepository.save(vehicle);
                log.info("Vehicle {} sold and removed from dealer inventory", vehicle.getVehicleId());
            }
        }
    }
    
    //  FIX: Tạo CUSTOMER_DEBT SAU KHI Order được đánh dấu "Completed"
    if ("Completed".equals(status) && order.getCustomer() != null) {
        log.info(" Order {} marked as Completed - Creating CUSTOMER_DEBT for customer {}", 
                id, order.getCustomer().getCustomerId());
        
        try {
            // Lấy payment mới nhất của order này để tạo debt
            List<com.example.evm.entity.payment.Payment> payments = paymentRepository.findAllByOrderId(id);
            log.info(" Found {} payments for Order {}", payments.size(), id);
            
            if (!payments.isEmpty()) {
                // Lấy payment đầu tiên (hoặc payment có type = INSTALLMENT)
                com.example.evm.entity.payment.Payment payment = payments.stream()
                        .filter(p -> "INSTALLMENT".equals(p.getPaymentType()))
                        .findFirst()
                        .orElse(payments.get(0));
                
                log.info(" Using Payment {} (type={}, amount={}) to create debt", 
                        payment.getPaymentId(), payment.getPaymentType(), payment.getAmount());
                
                debtService.autoCreateDebtFromPayment(payment.getPaymentId());
                log.info(" CUSTOMER_DEBT auto-created from Payment {} for Order {}", 
                        payment.getPaymentId(), id);
            } else {
                log.warn(" No payment found for Order {} - Cannot create CUSTOMER_DEBT", id);
            }
        } catch (Exception e) {
            log.error(" Failed to create CUSTOMER_DEBT for Order {}: {}", id, e.getMessage(), e);
        }
    }

    Order updatedOrder = orderRepository.save(order);
    log.info("Order {} status updated to: {}", id, status);

    return updatedOrder;
}

    @Transactional
    public void deleteOrder(Long id) {
        Order order = getOrderById(id);
        
        // Delete order details first
        List<OrderDetail> details = orderDetailRepository.findByOrderOrderId(id);
        orderDetailRepository.deleteAll(details);
        
        orderRepository.delete(order);
        log.info("Order deleted: {}", id);
    }
}
