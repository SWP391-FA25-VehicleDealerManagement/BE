package com.example.evm.dto.admin;

import java.time.LocalDateTime;

import com.example.evm.entity.user.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserInfoResponse {
<<<<<<< HEAD
    private Long userId;
    private String username;
    private String fullName;
=======
    private Integer userId;
    private String username;
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
    private String role;
    private String email;
    private String phone;
    private LocalDateTime createdDate;
    private LocalDateTime dateModified;
    private LocalDateTime refreshTokenExpiryTime;
<<<<<<< HEAD
    private Long dealerId;
=======
    private Integer dealerId;
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
    private String dealerName;

    public static AdminUserInfoResponse fromEntity(User u) {
        AdminUserInfoResponse r = new AdminUserInfoResponse();
        r.setUserId(u.getUserId());
        r.setUsername(u.getUserName());
<<<<<<< HEAD
        r.setFullName(u.getFullName());
=======
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
        r.setRole(u.getRole());
        r.setEmail(u.getEmail());
        r.setPhone(u.getPhone());
        r.setCreatedDate(u.getCreatedDate());
        r.setDateModified(u.getDateModified());
        r.setRefreshTokenExpiryTime(u.getRefreshTokenExpiryTime());
        if (u.getDealer() != null) {
            r.setDealerId(u.getDealer().getDealerId());
            r.setDealerName(u.getDealer().getDealerName());
        }
        return r;
    }
}
