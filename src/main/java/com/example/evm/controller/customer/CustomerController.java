package com.example.evm.controller.customer;

import com.example.evm.dto.auth.ApiResponse;
import com.example.evm.entity.customer.Customer;
import com.example.evm.service.customer.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EVM_STAFF', 'ROLE_DEALER_STAFF', 'ROLE_DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<List<Customer>>> getAllCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(new ApiResponse<>(true, "Customers retrieved successfully", customers));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EVM_STAFF', 'ROLE_DEALER_STAFF', 'ROLE_DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Customer>> getCustomerById(@PathVariable Long id) {
        Customer customer = customerService.getCustomerById(id);
        return customer != null 
                ? ResponseEntity.ok(new ApiResponse<>(true, "Customer retrieved successfully", customer))
                : ResponseEntity.ok(new ApiResponse<>(false, "Customer not found", null));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EVM_STAFF', 'ROLE_DEALER_STAFF', 'ROLE_DEALER_MANAGER')")
    public ResponseEntity<ApiResponse<Customer>> createCustomer(@RequestBody Customer customer) {
        try {
            Customer createdCustomer = customerService.createCustomer(customer);
            return ResponseEntity.ok(new ApiResponse<>(true, "Customer created successfully", createdCustomer));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EVM_STAFF')")
    public ResponseEntity<ApiResponse<Customer>> updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
        try {
            customer.setCustomerId(id);
            Customer updatedCustomer = customerService.updateCustomer(customer);
            return ResponseEntity.ok(new ApiResponse<>(true, "Customer updated successfully", updatedCustomer));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Long id) {
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Customer deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}
