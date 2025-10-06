package com.example.evm.service.customer;

import com.example.evm.entity.customer.Customer;
import com.example.evm.repository.customer.CustomerRepository;
import com.example.evm.repository.dealer.DealerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private DealerRepository dealerRepository;

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
<<<<<<< HEAD
    public Customer getCustomerById(Long id) {
=======
    public Customer getCustomerById(Integer id) {
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
        Optional<Customer> customer = customerRepository.findById(id);
        return customer.orElse(null);
    }

    @Override
    public Customer createCustomer(Customer customer) {
        if (customer.getDealerId() != null && !dealerRepository.existsById(customer.getDealerId())) {
            throw new IllegalArgumentException("Invalid dealerId: " + customer.getDealerId());
        }
<<<<<<< HEAD
        // createBy sẽ được controller gán từ header X-Creator-Name nếu có
=======
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
        return customerRepository.save(customer);
    }

    @Override
    public Customer updateCustomer(Customer customer) {
        if (customer.getCustomerId() == null || !customerRepository.existsById(customer.getCustomerId())) {
            throw new IllegalArgumentException("Customer not found");
        }
        if (customer.getDealerId() != null && !dealerRepository.existsById(customer.getDealerId())) {
            throw new IllegalArgumentException("Invalid dealerId: " + customer.getDealerId());
        }
        return customerRepository.save(customer);
    }

    @Override
<<<<<<< HEAD
    public void deleteCustomer(Long id) {
=======
    public void deleteCustomer(Integer id) {
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
        customerRepository.deleteById(id);
    }
}
