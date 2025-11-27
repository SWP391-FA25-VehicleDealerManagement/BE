package com.example.evm.controller.order;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.order.OrderRequestDto;
import com.example.evm.entity.order.Order;
import com.example.evm.entity.order.OrderDetail;
import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.service.order.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
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
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Order>>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(new ApiResponse<>(true, "Orders retrieved successfully", orders));
    }

    @GetMapping("/dealer/{dealerId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Order>>> getOrdersByDealer(@PathVariable Long dealerId) {
        List<Order> orders = orderService.getOrdersByDealer(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Orders retrieved successfully", orders));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Order>>> getOrdersByCustomer(@PathVariable Long customerId) {
        List<Order> orders = orderService.getOrdersByCustomer(customerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Orders retrieved successfully", orders));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Order>>> getOrdersByStatus(@PathVariable String status) {
        List<Order> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(new ApiResponse<>(true, "Orders retrieved successfully", orders));
    }

    @GetMapping("/dealer/{dealerId}/status/{status}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Order>>> getOrdersByDealerAndStatus(
            @PathVariable Long dealerId, 
            @PathVariable String status) {
        List<Order> orders = orderService.getOrdersByDealerAndStatus(dealerId, status);
        return ResponseEntity.ok(new ApiResponse<>(true, "Orders retrieved successfully", orders));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Order>> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Order retrieved successfully", order));
    }

    @GetMapping("/{id}/details")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<OrderDetail>>> getOrderDetails(@PathVariable Long id) {
        List<OrderDetail> details = orderService.getOrderDetails(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Order details retrieved successfully", details));
    }

    @GetMapping("/dealer/{dealerId}/no-contract")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    @Operation(summary = "Lấy danh sách đơn hàng chưa có hợp đồng của đại lý ")
    public ResponseEntity<ApiResponse<List<Order>>> getOrdersWithoutContract(
            @PathVariable Long dealerId) {

        List<Order> orders = orderService.getOrdersWithoutContract(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true,
                "Danh sách đơn hàng chưa có hợp đồng của đại lý " + dealerId,
                orders));
    }


    /**
     *  Tạo order mới - Chỉ cần truyền IDs, backend sẽ mock hết thông tin
     * Request body:
     * {
     *   "customerId": 1,
     *   "userId": 1,
     *   "dealerId": 1,
     *   "paymentMethod": "CASH",
     *   "orderDetails": [
     *     {
     *       "vehicleId": 1,
     *       "promotionId": 1,  // optional
     *       "quantity": 1,
     *       "price": 500000000
     *     }
     *   ]
     * }
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Order>> createOrderFromDto(@Valid @RequestBody OrderRequestDto dto) {
        try {
            //  Backend tự lookup entities từ IDs và trả về đầy đủ thông tin
            Order createdOrder = orderService.createOrderFromDto(dto);
            log.info("Order created successfully with ID: {}", createdOrder.getOrderId());
            
            return ResponseEntity.ok(new ApiResponse<>(
                    true, 
                    "Order created successfully", 
                    createdOrder
            ));
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found when creating order: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false, 
                    "Failed to create order: " + e.getMessage(), 
                    null
            ));
        } catch (Exception e) {
            log.error("Error creating order", e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false, 
                    "Failed to create order: " + e.getMessage(), 
                    null
            ));
        }
    }

    /**
     *  API cũ - Deprecated
     * Dùng POST /api/orders thay thế
     */
    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    @Deprecated
    public ResponseEntity<ApiResponse<Order>> createOrder(@RequestBody Order order) {
        try {
            //  Backend validates và tự generate IDs
            Order createdOrder = orderService.createOrder(order);
            log.info("Order created successfully with ID: {}", createdOrder.getOrderId());
            
            return ResponseEntity.ok(new ApiResponse<>(
                    true, 
                    "Order created successfully", 
                    createdOrder
            ));
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found when creating order: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false, 
                    "Failed to create order: " + e.getMessage(), 
                    null
            ));
        } catch (Exception e) {
            log.error("Error creating order", e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false, 
                    "Failed to create order: " + e.getMessage(), 
                    null
            ));
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Order>> updateOrderStatus(
            @PathVariable Long id, 
            @RequestParam String status) {
        Order updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(new ApiResponse<>(true, "Order status updated successfully", updatedOrder));
    }

    @GetMapping("/dealer/{dealerId}/total-sales")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Double>> getTotalSales(@PathVariable Long dealerId) {
        Double totalSales = orderService.getTotalSalesByDealer(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Total sales retrieved", totalSales));
    }

    @GetMapping("/dealer/{dealerId}/count")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF', 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Long>> countOrders(
            @PathVariable Long dealerId,
            @RequestParam String status) {
        Long count = orderService.countOrdersByDealerAndStatus(dealerId, status);
        return ResponseEntity.ok(new ApiResponse<>(true, "Order count retrieved", count));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'EVM_STAFF')")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Order deleted successfully", null));
    }
}
