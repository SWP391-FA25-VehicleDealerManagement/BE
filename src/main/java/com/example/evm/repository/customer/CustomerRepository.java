package com.example.evm.repository.customer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.evm.entity.customer.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    List<Customer> findByDealerId(Long dealerId);
     List<Customer> findByCreateBy(String createBy);
} 
