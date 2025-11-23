package com.example.evm.dto.dealer;

import java.time.LocalDateTime;

import com.example.evm.dto.auth.DealerInfo;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response object cho API tạo tài khoản dealer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDealerAccountResponse {

    private boolean success;
    private String message;
    private Long userId;
    private String username;
    private String role;
 
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime userCreatedDate;
    
    // Thông tin của dealer (không bao gồm password)
    private DealerInfo dealerInfo;
}
