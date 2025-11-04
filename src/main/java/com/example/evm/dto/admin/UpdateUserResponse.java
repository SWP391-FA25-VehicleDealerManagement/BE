package com.example.evm.dto.admin;

import java.time.LocalDateTime;

import lombok.Data;
@Data
public class UpdateUserResponse {
private Long userId;
    private String userName;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private Long dealerId;
    private LocalDateTime createdDate;
    private LocalDateTime dateModified;
}
