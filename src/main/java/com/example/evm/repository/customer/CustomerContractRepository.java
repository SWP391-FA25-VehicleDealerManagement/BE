package com.example.evm.repository.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.evm.entity.customer.CustomerContract;

public interface CustomerContractRepository extends JpaRepository<CustomerContract, Integer>{

}
