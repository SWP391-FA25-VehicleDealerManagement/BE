package dealermanagementsystem.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dealermanagementsystem.project.dto.request.CustomersRequest;
import dealermanagementsystem.project.entity.Customers;
import dealermanagementsystem.project.repository.CustomersRepository;

@Service
public class CustomersService {

    @Autowired
    private CustomersRepository customersRepository;

    public Customers createCustomers(CustomersRequest request) {
        Customers customers = new Customers();
        customers.setFullname(request.getFullname());
        customers.setPhone(request.getPhone());
        customers.setEmail(request.getEmail());
        customers.setAddress(request.getAddress());
        customers.setNationalID(request.getNationalID());
        return customersRepository.save(customers);
    }
}
