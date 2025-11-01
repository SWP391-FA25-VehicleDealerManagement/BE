package com.example.evm.service.customer;

import com.example.evm.entity.customer.Customer;
import java.util.List;

public interface CustomerService {

    List<Customer> getAllCustomers();

    Customer getCustomerById(Long id);

    Customer createCustomer(Customer customer);

    Customer updateCustomer(Customer customer);

    void deleteCustomer(Long id);
     List<Customer> getCustomersByDealer(Long dealerId);
        List<Customer> getCustomersByCreatedBy(String createdBy);
}
