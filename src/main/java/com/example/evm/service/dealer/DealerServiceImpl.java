package com.example.evm.service.dealer;

import com.example.evm.entity.dealer.Dealer;
import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.repository.dealer.DealerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
<<<<<<< HEAD
    public Dealer getDealerById(Long id) {
=======
    public Dealer getDealerById(Integer id) {
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
        return dealerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));
    }

    @Override
    public Dealer createDealer(Dealer dealer) {
        dealer.setDealerId(null);
        return dealerRepository.save(dealer);
    }

    @Override
    public Dealer updateDealer(Dealer dealer) {
<<<<<<< HEAD
        Long id = dealer.getDealerId();
=======
        Integer id = dealer.getDealerId();
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
        if (id == null || !dealerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Dealer not found");
        }
        return dealerRepository.save(dealer);
    }

    @Override
<<<<<<< HEAD
    public void deleteDealer(Long id) {
=======
    public void deleteDealer(Integer id) {
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
        if (!dealerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Dealer not found");
        }
        dealerRepository.deleteById(id);
        log.info("Deleted dealer {}", id);
    }
<<<<<<< HEAD


    @Override
    public Dealer getDealerByName(String dealerName) {
        return dealerRepository.findByDealerName(dealerName)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer not found"));
    }
=======
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
}


