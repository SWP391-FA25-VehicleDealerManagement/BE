package com.example.evm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.evm.entity.Dealer;

public interface DealerRepository extends JpaRepository<Dealer, Integer> {}
