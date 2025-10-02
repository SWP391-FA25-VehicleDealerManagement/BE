package com.example.evm.service.dealer;

import com.example.evm.entity.dealer.Dealer;
import java.util.List;

public interface DealerService {

    List<Dealer> getAllDealers();

    Dealer getDealerById(Integer id);

    Dealer createDealer(Dealer dealer);

    Dealer updateDealer(Dealer dealer);

    void deleteDealer(Integer id);
}


