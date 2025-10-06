package com.example.evm.repository.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.evm.entity.customer.Customer;

public interface CustomerRepository  extends JpaRepository<Customer,Long>{

    
} 
