package com.example.evm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.evm.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {}
