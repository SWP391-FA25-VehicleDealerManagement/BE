package com.example.evm.service.dealer;

import com.example.evm.entity.dealer.Dealer;
import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.repository.dealer.DealerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealerServiceImpl implements DealerService {

    private final DealerRepository dealerRepository;

    @Override
    public List<Dealer> getAllDealers() {
        return dealerRepository.findAll();
    }

    @Override
    public Dealer getDealerById(Long id) {
        return dealerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));
    }

    @Override
    public Dealer getDealerByName(String name) {
        return dealerRepository.findByDealerName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));
    }

    @Override
    public Dealer createDealer(Dealer dealer) {
        dealer.setDealerId(null);
        dealer.setCreatedDate(LocalDateTime.now());
        return dealerRepository.save(dealer);
    }

    @Override
    public Dealer updateDealer(Dealer dealer) {
        Long id = dealer.getDealerId();
        if (id == null || !dealerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Dealer not found");
        }
        return dealerRepository.save(dealer);
    }

    @Override
    public void deleteDealer(Long id) {
        if (!dealerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Dealer not found");
        }
        dealerRepository.deleteById(id);
        log.info("Deleted dealer {}", id);
    }
}


