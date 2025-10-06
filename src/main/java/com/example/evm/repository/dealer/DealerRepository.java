package com.example.evm.repository.dealer;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.evm.entity.dealer.Dealer;

public interface DealerRepository extends JpaRepository<Dealer, Integer> {
        
}
