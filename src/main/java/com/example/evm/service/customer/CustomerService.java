package com.example.evm.service.customer;

import com.example.evm.entity.customer.Customer;
import java.util.List;

public interface CustomerService {

    List<Customer> getAllCustomers();

<<<<<<< HEAD
    Customer getCustomerById(Long id);
=======
    Customer getCustomerById(Integer id);
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d

    Customer createCustomer(Customer customer);

    Customer updateCustomer(Customer customer);

<<<<<<< HEAD
    void deleteCustomer(Long id);
=======
    void deleteCustomer(Integer id);
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
}
