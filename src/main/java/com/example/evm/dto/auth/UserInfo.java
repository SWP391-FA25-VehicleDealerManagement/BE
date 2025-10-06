package com.example.evm.dto.auth;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {

    private Long userId;
    private String userName;
    private String fullName;
    private String phone;
    private String email;
    private String role;
    private DealerInfo dealer;
    private LocalDateTime createdDate;
    private LocalDateTime dateModified;
    private LocalDateTime refreshTokenExpiryTime;
}
