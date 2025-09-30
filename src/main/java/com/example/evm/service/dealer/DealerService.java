package com.example.evm.service.dealer;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.evm.entity.Dealer;
import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.repository.DealerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DealerService {
    private final DealerRepository dealerRepository;

    public List<Dealer> getAllDealers() {
        return dealerRepository.findAll();
    }

    public Dealer getDealerById(Integer id) {
        return dealerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found with id: " + id));
    }
    public Dealer createDealer(Dealer dealer) {
        if (dealer.getCreatedDate() == null) {
            dealer.setCreatedDate(LocalDateTime.now());
        }
        return dealerRepository.save(dealer);
    }

    public Dealer updateDealer(Dealer dealer) {
        if (dealer.getDealerId() == null) {
            throw new IllegalArgumentException("Dealer ID is required for update");
        }
        Dealer existing = getDealerById(dealer.getDealerId());
        existing.setDealerName(dealer.getDealerName());
        existing.setPhone(dealer.getPhone());
        existing.setAddress(dealer.getAddress());
        existing.setCreatedBy(dealer.getCreatedBy());
        return dealerRepository.save(existing);
    }

    public void deleteDealer(Integer id) {
        Dealer dealer = getDealerById(id);
        dealerRepository.delete(dealer);
    }
}