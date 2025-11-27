package com.example.evm.dto.admin;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO cho API lấy thông tin user account
 *  KHÔNG bao gồm password
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountInfoResponse {
    
    private Long userId;
    private String username;
    private String role;
    private String fullName;
    private String email;
    private String phone;
    private Long dealerId;
    private String dealerName;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateModified;
}

