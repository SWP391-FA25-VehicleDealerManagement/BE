package com.example.evm.controller.order;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.entity.order.Order;
import com.example.evm.entity.order.OrderDetail;
import com.example.evm.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Order>>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(new ApiResponse<>(true, "Orders retrieved successfully", orders));
    }

    @GetMapping("/dealer/{dealerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Order>>> getOrdersByDealer(@PathVariable Long dealerId) {
        List<Order> orders = orderService.getOrdersByDealer(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Orders retrieved successfully", orders));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Order>>> getOrdersByCustomer(@PathVariable Long customerId) {
        List<Order> orders = orderService.getOrdersByCustomer(customerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Orders retrieved successfully", orders));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Order>>> getOrdersByStatus(@PathVariable String status) {
        List<Order> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(new ApiResponse<>(true, "Orders retrieved successfully", orders));
    }

    @GetMapping("/dealer/{dealerId}/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Order>>> getOrdersByDealerAndStatus(
            @PathVariable Long dealerId, 
            @PathVariable String status) {
        List<Order> orders = orderService.getOrdersByDealerAndStatus(dealerId, status);
        return ResponseEntity.ok(new ApiResponse<>(true, "Orders retrieved successfully", orders));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Order>> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Order retrieved successfully", order));
    }

    @GetMapping("/{id}/details")
    @PreAuthorize("hasAnyRole('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<OrderDetail>>> getOrderDetails(@PathVariable Long id) {
        List<OrderDetail> details = orderService.getOrderDetails(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Order details retrieved successfully", details));
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Order>> createOrder(@RequestBody Order order) {
        Order createdOrder = orderService.createOrder(order);
        return ResponseEntity.ok(new ApiResponse<>(true, "Order created successfully", createdOrder));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Order>> updateOrderStatus(
            @PathVariable Long id, 
            @RequestParam String status) {
        Order updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(new ApiResponse<>(true, "Order status updated successfully", updatedOrder));
    }

    @GetMapping("/dealer/{dealerId}/total-sales")
    @PreAuthorize("hasAnyRole('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Double>> getTotalSales(@PathVariable Long dealerId) {
        Double totalSales = orderService.getTotalSalesByDealer(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Total sales retrieved", totalSales));
    }

    @GetMapping("/dealer/{dealerId}/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Long>> countOrders(
            @PathVariable Long dealerId,
            @RequestParam String status) {
        Long count = orderService.countOrdersByDealerAndStatus(dealerId, status);
        return ResponseEntity.ok(new ApiResponse<>(true, "Order count retrieved", count));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Order deleted successfully", null));
    }
}
