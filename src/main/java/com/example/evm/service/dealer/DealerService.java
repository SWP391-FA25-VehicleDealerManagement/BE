package com.example.evm.service.dealer;

import com.example.evm.entity.dealer.Dealer;
import java.util.List;

public interface DealerService {

    List<Dealer> getAllDealers();

<<<<<<< HEAD
    Dealer getDealerByName(String dealerName);

    Dealer getDealerById(Long id);
=======
    Dealer getDealerById(Integer id);
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d

    Dealer createDealer(Dealer dealer);

    Dealer updateDealer(Dealer dealer);

<<<<<<< HEAD
    void deleteDealer(Long id);

    
=======
    void deleteDealer(Integer id);
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
}


