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
<<<<<<< HEAD

    private Long userId;
    private String userName;
    private String fullName;
=======
    private Integer userId;
    private String userName;
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
    private String phone;
    private String email;
    private String role;
    private DealerInfo dealer;
    private LocalDateTime createdDate;
    private LocalDateTime dateModified;
    private LocalDateTime refreshTokenExpiryTime;
}
