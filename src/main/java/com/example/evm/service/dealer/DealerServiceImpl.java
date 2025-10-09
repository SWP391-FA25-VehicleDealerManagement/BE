package com.example.evm.service.dealer;

import com.example.evm.entity.dealer.Dealer;
import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.repository.dealer.DealerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealerServiceImpl implements DealerService {

    private final DealerRepository dealerRepository;

    @Override
    public List<Dealer> getAllDealers() {
        return dealerRepository.findAllActiveDealers(); // ✅ chỉ lấy ACTIVE Dealer
    }

    @Override
    public List<Dealer> getInactiveDealers() {
        return dealerRepository.findAllInactiveDealers(); // ✅ chỉ lấy INACTIVE Dealer
    }

    @Override
    public Dealer getDealerById(Long id) {
        return dealerRepository.findById(id)
                .filter(d -> "ACTIVE".equals(d.getStatus()))
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));
    }

    @Override
    public Dealer getDealerByName(String name) {
        return dealerRepository.findByDealerName(name)
                .filter(d -> "ACTIVE".equals(d.getStatus()))
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));
    }

    @Override
    public Dealer createDealer(Dealer dealer) {
        dealer.setDealerId(null);
        dealer.setStatus("ACTIVE");
        dealer.setCreatedDate(LocalDateTime.now());
        return dealerRepository.save(dealer);
    }

    @Override
    public Dealer updateDealer(Dealer dealer) {
        Long id = dealer.getDealerId();
        if (id == null || !dealerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Dealer not found");
        }
        dealer.setStatus("ACTIVE");
        return dealerRepository.save(dealer);
    }

    @Override
    @Transactional
    public void deleteDealer(Long id) {
        if (!dealerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Dealer not found");
        }
        dealerRepository.updateStatus(id, "INACTIVE");
        log.info("🟡 Dealer {} marked as INACTIVE", id);
    }

    @Override
    @Transactional
    public void activateDealer(Long id) {
        if (!dealerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Dealer not found");
        }
        dealerRepository.updateStatus(id, "ACTIVE");
        log.info("🟢 Dealer {} reactivated", id);
    }

}


