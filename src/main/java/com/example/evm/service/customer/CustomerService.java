package com.example.evm.service.customer;

import com.example.evm.entity.customer.Customer;
import com.example.evm.repository.customer.CustomerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(Integer id) {
        return customerRepository.findById(id);
    }

    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Integer id, Customer detail) {
        return customerRepository.findById(id)
                .map(c -> {
                    c.setCustomerName(detail.getCustomerName());
                    c.setEmail(detail.getEmail());
                    c.setPhone(detail.getPhone());
                    c.setDealer(detail.getDealer());
                    return customerRepository.save(c);
                })
                .orElseThrow(() -> new RuntimeException("Customer not found with id " + id));
    }

    public void deleteCustomer(Integer id) {
        customerRepository.deleteById(id);
    }
}

