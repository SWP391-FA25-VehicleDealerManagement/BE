package com.example.evm.service.dealer;

import com.example.evm.entity.dealer.Dealer;
import java.util.List;

public interface DealerService {

    List<Dealer> getAllDealers();

    Dealer getDealerByName(String dealerName);

    Dealer getDealerById(Long id);

    Dealer createDealer(Dealer dealer);

    Dealer updateDealer(Dealer dealer);

    void deleteDealer(Long id);

    
}


