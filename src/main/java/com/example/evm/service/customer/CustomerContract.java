package com.example.evm.service.customer;


import com.example.evm.entity.customer.CustomerContract;
import com.example.evm.repository.customer.CustomerContractRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerContractService {

    @Autowired
    private CustomerContractRepository customerContractRepository;

    public List<CustomerContract> getAllCustomerContracts() {
        return customerContractRepository.findAll();
    }

    public Optional<CustomerContract> getCustomerContractById(Integer id) {
        return customerContractRepository.findById(id);
    }

    public CustomerContract createCustomerContract(CustomerContract contract) {
        return customerContractRepository.save(contract);
    }

    public CustomerContract updateCustomerContract(Integer id, CustomerContract detail) {
        return customerContractRepository.findById(id)
                .map(cc -> {
                    cc.setOrderid(detail.getOrderid());
                    cc.setTerms(detail.getTerms());
                    cc.setPaymentmethod(detail.getPaymentmethod());
                    cc.setStatus(detail.getStatus());
                    return customerContractRepository.save(cc);
                })
                .orElseThrow(() -> new RuntimeException("CustomerContract not found with id " + id));
    }

    public void deleteCustomerContract(Integer id) {
        customerContractRepository.deleteById(id);
    }
}
