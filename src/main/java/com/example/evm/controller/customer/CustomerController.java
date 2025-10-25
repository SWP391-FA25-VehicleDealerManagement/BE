package com.example.evm.controller.customer;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.dto.auth.UserInfo;
import com.example.evm.entity.customer.Customer;
import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.service.auth.AuthService;
import com.example.evm.service.customer.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final AuthService authService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Customer>>> getAllCustomers(Authentication authentication) {
        // Lấy thông tin user hiện tại
        UserInfo currentUser = authService.getCurrentUser(authentication.getName());
        
        List<Customer> customers;
        
        // Nếu là dealer staff hoặc dealer manager, chỉ lấy customers của dealer đó
        if ("DEALER_STAFF".equals(currentUser.getRole()) || "DEALER_MANAGER".equals(currentUser.getRole())) {
            if (currentUser.getDealer() == null || currentUser.getDealer().getDealerId() == null) {
                log.warn("User {} has dealer role but no dealer assigned", currentUser.getUserName());
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "User is not assigned to any dealer", null));
            }
            Long dealerId = currentUser.getDealer().getDealerId();
            log.info("Fetching customers for dealer: {}", dealerId);
            customers = customerService.getCustomersByDealer(dealerId);
        }
            else if ("EVM_STAFF".equals(currentUser.getRole())) {
            customers = customerService.getCustomersByCreatedBy("evmStaff");
           }
        else {
            // ADMIN hoặc EVM_STAFF có thể xem tất cả customers
            log.info("Fetching all customers for user with role: {}", currentUser.getRole());
            customers = customerService.getAllCustomers();
        }
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Customers retrieved successfully", customers));
    }

    @GetMapping("/dealer/{dealerId}")
    @PreAuthorize("hasAnyAuthority('DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Customer>>> getCustomersByDealer(@PathVariable Long dealerId) {
        List<Customer> customers = customerService.getCustomersByDealer(dealerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Customers retrieved successfully", customers));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Customer>> getCustomerById(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            Customer customer = customerService.getCustomerById(id);
            
            // Lấy thông tin user hiện tại
            UserInfo currentUser = authService.getCurrentUser(authentication.getName());
            
            // Nếu là dealer staff hoặc dealer manager, kiểm tra customer có thuộc dealer của họ không
            if ("DEALER_STAFF".equals(currentUser.getRole()) || "DEALER_MANAGER".equals(currentUser.getRole())) {
                if (currentUser.getDealer() == null || currentUser.getDealer().getDealerId() == null) {
                    return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "User is not assigned to any dealer", null));
                }
                
                if (!currentUser.getDealer().getDealerId().equals(customer.getDealerId())) {
                    log.warn("User {} attempted to view customer {} from different dealer", 
                        currentUser.getUserName(), id);
                    return ResponseEntity.status(403)
                        .body(new ApiResponse<>(false, "You can only view customers from your dealer", null));
                }
            }
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Customer retrieved successfully", customer));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority( 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Customer>> createCustomer(
            @Valid @RequestBody Customer customer,
            Authentication authentication) {
        try {
            // Lấy thông tin user hiện tại
            UserInfo currentUser = authService.getCurrentUser(authentication.getName());
            
            // Nếu là dealer staff hoặc dealer manager, tự động gán dealerId từ user
            if ("DEALER_STAFF".equals(currentUser.getRole()) || "DEALER_MANAGER".equals(currentUser.getRole())) {
                if (currentUser.getDealer() == null || currentUser.getDealer().getDealerId() == null) {
                    return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "User is not assigned to any dealer", null));
                }
                customer.setDealerId(currentUser.getDealer().getDealerId());
                customer.setCreateBy(currentUser.getUserName());

                log.info("Auto-assigned dealer {} to customer", currentUser.getDealer().getDealerId());
            }
            
            Customer createdCustomer = customerService.createCustomer(customer);
            log.info("Customer created: {}", createdCustomer.getCustomerId());
            return ResponseEntity.ok(new ApiResponse<>(true, "Customer created successfully", createdCustomer));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error creating customer", e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to create customer", null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority( 'DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Customer>> updateCustomer(
            @PathVariable Long id, 
            @Valid @RequestBody Customer customer,
            Authentication authentication) {
        try {
            // Lấy thông tin user hiện tại
            UserInfo currentUser = authService.getCurrentUser(authentication.getName());
            
            customer.setCustomerId(id);
            
            // Nếu là dealer staff hoặc dealer manager, đảm bảo chỉ update customer của dealer họ
            if ("DEALER_STAFF".equals(currentUser.getRole()) || "DEALER_MANAGER".equals(currentUser.getRole())) {
                if (currentUser.getDealer() == null || currentUser.getDealer().getDealerId() == null) {
                    return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "User is not assigned to any dealer", null));
                }
                
                // Kiểm tra customer hiện tại có thuộc dealer của user không
                Customer existingCustomer = customerService.getCustomerById(id);
                if (!currentUser.getDealer().getDealerId().equals(existingCustomer.getDealerId())) {
                    log.warn("User {} attempted to update customer {} from different dealer", 
                        currentUser.getUserName(), id);
                    return ResponseEntity.status(403)
                        .body(new ApiResponse<>(false, "You can only update customers from your dealer", null));
                }
                
                // Đảm bảo dealerId không bị thay đổi
                customer.setDealerId(currentUser.getDealer().getDealerId());
            }
            
            Customer updatedCustomer = customerService.updateCustomer(customer);
            log.info("Customer updated: {}", updatedCustomer.getCustomerId());
            return ResponseEntity.ok(new ApiResponse<>(true, "Customer updated successfully", updatedCustomer));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error updating customer", e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to update customer", null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('DEALER_STAFF', 'DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Long id) {
        try {
            customerService.deleteCustomer(id);
            log.info("Customer deleted: {}", id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Customer deleted successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Error deleting customer", e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Failed to delete customer", null));
        }
    }
}
