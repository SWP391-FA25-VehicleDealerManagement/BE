package com.example.evm.service.order;


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
import com.example.evm.repository.UserRepository;
import com.example.evm.repository.customer.CustomerRepository;

import com.example.evm.repository.dealer.DealerRepository;
import com.example.evm.repository.vehicle.VehicleRepository;
import com.example.evm.repository.promotion.PromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByDealer(Long dealerId) {
        return orderRepository.findByDealerDealerId(dealerId);
    }

    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerCustomerId(customerId);
    }

    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> getOrdersByDealerAndStatus(Long dealerId, String status) {
        return orderRepository.findByDealerAndStatus(dealerId, status);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    public List<OrderDetail> getOrderDetails(Long orderId) {
        return orderDetailRepository.findByOrderOrderId(orderId);
    }

    @Transactional
    public Order createOrder(Order order) {
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
                savedOrder.getOrderId(), customer.getCustomerName(), totalPrice);
        
        return savedOrder;
    }

    public Order updateOrderStatus(Long id, String status) {
        Order order = getOrderById(id);
        order.setStatus(status);
        
        Order updatedOrder = orderRepository.save(order);
        log.info("Order {} status updated to: {}", id, status);
        
        return updatedOrder;
    }

    public Double getTotalSalesByDealer(Long dealerId) {
        Double totalSales = orderRepository.getTotalSalesByDealer(dealerId);
        return totalSales != null ? totalSales : 0.0;
    }

    public Long countOrdersByDealerAndStatus(Long dealerId, String status) {
        return orderRepository.countByDealerAndStatus(dealerId, status);
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
