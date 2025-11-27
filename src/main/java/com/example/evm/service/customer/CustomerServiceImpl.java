package com.example.evm.service.customer;

import com.example.evm.entity.customer.Customer;
import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.repository.customer.CustomerRepository;
import com.example.evm.repository.dealer.DealerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final DealerRepository dealerRepository;

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    @Override
    @Transactional
    public Customer createCustomer(Customer customer) {
        //  Backend tự tạo ID
        customer.setCustomerId(null);
        
        // Validate dealer exists
        if (customer.getDealerId() != null && !dealerRepository.existsById(customer.getDealerId())) {
            throw new IllegalArgumentException("Dealer not found with id: " + customer.getDealerId());
        }
        
        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer created: ID {} - Name: {}", savedCustomer.getCustomerId(), savedCustomer.getCustomerName());
        
        return savedCustomer;
    }

    @Override
    @Transactional
    public Customer updateCustomer(Customer customer) {
        if (customer.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID is required for update");
        }
        
        // Check if customer exists
        if (!customerRepository.existsById(customer.getCustomerId())) {
            throw new ResourceNotFoundException("Customer not found with id: " + customer.getCustomerId());
        }
        
        // Validate dealer exists
        if (customer.getDealerId() != null && !dealerRepository.existsById(customer.getDealerId())) {
            throw new IllegalArgumentException("Dealer not found with id: " + customer.getDealerId());
        }
        
        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Customer updated: ID {} - Name: {}", updatedCustomer.getCustomerId(), updatedCustomer.getCustomerName());
        
        return updatedCustomer;
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer not found with id: " + id);
        }
        
        customerRepository.deleteById(id);
        log.info("Customer deleted: ID {}", id);
    }

    @Override
    public List<Customer> getCustomersByDealer(Long dealerId) {
        return customerRepository.findByDealerId(dealerId);
    }
    
    @Override
    public List<Customer> getCustomersByCreatedBy(String createdBy){
        return customerRepository.findByCreateBy(createdBy);
    }
}
