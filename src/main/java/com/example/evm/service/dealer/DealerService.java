package com.example.evm.service.dealer;

import com.example.evm.entity.dealer.Dealer;
import java.util.List;

public interface DealerService {

    List<Dealer> getAllDealers();

    List<Dealer> getInactiveDealers();

    Dealer getDealerById(Long id);

    Dealer getDealerByName(String name);

    Dealer createDealer(Dealer dealer);

    Dealer updateDealer(Dealer dealer);

    void deleteDealer(Long id);

    void activateDealer(Long id);
}


