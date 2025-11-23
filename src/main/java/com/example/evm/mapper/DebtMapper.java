package com.example.evm.mapper;

import com.example.evm.dto.debt.DebtResponse;
import com.example.evm.entity.customer.Customer;
import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.debt.Debt;
import com.example.evm.entity.user.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DebtMapper {
    
    /**
     * Convert Debt entity to DebtResponse DTO
     */
    public DebtResponse toResponse(Debt debt) {
        if (debt == null) {
            return null;
        }
        
        return DebtResponse.builder()
                .debtId(debt.getDebtId())
                .debtType(debt.getDebtType())
                .user(mapUserInfo(debt.getUser()))
                .dealer(mapDealerInfo(debt.getDealer()))
                .customer(mapCustomerInfo(debt.getCustomer()))
                .amountDue(debt.getAmountDue())
                .amountPaid(debt.getAmountPaid())
                .remainingAmount(debt.getRemainingAmount())
                .interestRate(debt.getInterestRate())
                .startDate(debt.getStartDate())
                .dueDate(debt.getDueDate())
                .status(debt.getStatus())
                .paymentMethod(debt.getPaymentMethod())
                .notes(debt.getNotes())
                .createdDate(debt.getCreatedDate())
                .updatedDate(debt.getUpdatedDate())
                .overdue(debt.isOverdue())
                .totalInterest(debt.getTotalInterest())
                .build();
    }
    
    /**
     * Convert list of Debt entities to list of DebtResponse DTOs
     */
    public List<DebtResponse> toResponseList(List<Debt> debts) {
        if (debts == null) {
            return null;
        }
        return debts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Map User entity to UserInfo DTO
     */
    private DebtResponse.UserInfo mapUserInfo(User user) {
        if (user == null) {
            return null;
        }
        
        return DebtResponse.UserInfo.builder()
                .userId(user.getUserId())
                .username(user.getUserName()) // Field is 'userName' not 'username'
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhone()) // Field is 'phone' not 'phoneNumber'
                .role(user.getRole())
                .build();
    }
    
    /**
     * Map Dealer entity to DealerInfo DTO
     */
    private DebtResponse.DealerInfo mapDealerInfo(Dealer dealer) {
        if (dealer == null) {
            return null;
        }
        
        return DebtResponse.DealerInfo.builder()
                .dealerId(dealer.getDealerId())
                .dealerName(dealer.getDealerName())
                .dealerCode(null) // Dealer entity doesn't have dealerCode field
                .contactPerson(null) // Dealer entity doesn't have contactPerson field
                .email(null) // Dealer entity doesn't have email field
                .phoneNumber(dealer.getPhone()) // Field is 'phone' not 'phoneNumber'
                .address(dealer.getAddress())
                .city(null) // Dealer entity doesn't have city field
                .status(dealer.getStatus())
                .build();
    }
    
    /**
     * Map Customer entity to CustomerInfo DTO
     */
    private DebtResponse.CustomerInfo mapCustomerInfo(Customer customer) {
        if (customer == null) {
            return null;
        }
        
        return DebtResponse.CustomerInfo.builder()
                .customerId(customer.getCustomerId())
                .customerName(customer.getCustomerName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhone()) // Field is 'phone' not 'phoneNumber'
                .address(null) // Customer entity doesn't have address field
                .city(null) // Customer entity doesn't have city field
                .identityCard(null) // Customer entity doesn't have identityCard field
                .customerType(null) // Customer entity doesn't have customerType field
                .build();
    }
}

