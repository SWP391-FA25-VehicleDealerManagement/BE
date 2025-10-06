package com.example.evm.repository.dealer;

<<<<<<< HEAD
import java.util.Optional;

=======
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.evm.entity.dealer.Dealer;

<<<<<<< HEAD
public interface DealerRepository extends JpaRepository<Dealer, Long> {
    Optional<Dealer> findByDealerName(String dealerName);
=======
public interface DealerRepository extends JpaRepository<Dealer, Integer> {
        
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
}
