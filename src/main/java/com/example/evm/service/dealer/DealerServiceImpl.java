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
        return dealerRepository.findAllActiveDealers();
    }

    @Override
    public List<Dealer> getInactiveDealers() {
        return dealerRepository.findAllInactiveDealers();
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
    @Transactional
    public Dealer createDealer(Dealer dealer) {
        // ✅ Tính toán ID tiếp theo (sequential)
        Long nextId = dealerRepository.findMaxActiveId().orElse(0L) + 1;
        
        dealer.setDealerId(nextId);
        dealer.setStatus("ACTIVE");
        dealer.setCreatedDate(LocalDateTime.now());
        
        log.info("🆕 Creating dealer with sequential ID: {}", nextId);
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
        
        // ⚠️ HARD DELETE để compact IDs
        dealerRepository.deleteById(id);
        log.warn("🗑️ Dealer {} permanently deleted", id);
        
        // ✅ Compact IDs - shift tất cả ID > deletedId xuống 1 đơn vị
        compactIds(id);
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

   
    @Transactional
    private void compactIds(Long deletedId) {
        List<Dealer> dealersToShift = dealerRepository.findDealersToShift(deletedId);
        
        log.warn("🔄 Compacting {} dealer IDs after deleting ID {}", dealersToShift.size(), deletedId);
        
        for (Dealer dealer : dealersToShift) {
            Long oldId = dealer.getDealerId();
            Long newId = oldId - 1;
            
            
            dealer.setDealerId(null);
            dealerRepository.save(dealer);
            dealerRepository.flush();
            
            // Set ID mới
            dealer.setDealerId(newId);
            dealerRepository.save(dealer);
            
            log.info("Shifted dealer ID: {} → {}", oldId, newId);
        }
        
        log.warn("ID compaction completed");
    }
}