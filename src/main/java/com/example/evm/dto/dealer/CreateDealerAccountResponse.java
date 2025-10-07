package com.example.evm.dto.dealer;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDealerAccountResponse {
    
    private Long dealerId;
    private String dealerName;
    private Long userId;
    private String username;
    private String role;
    private String message;
}
