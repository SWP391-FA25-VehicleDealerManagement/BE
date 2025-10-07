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
    private Long userId;
    private String username;
    private String role;
    private String email;
    private String phone;
    private LocalDateTime createdDate;
    private LocalDateTime dateModified;
    private LocalDateTime refreshTokenExpiryTime;
    private Long dealerId;
    private String dealerName;

    public static AdminUserInfoResponse fromEntity(User u) {
        AdminUserInfoResponse r = new AdminUserInfoResponse();
        r.setUserId(u.getUserId());
        r.setUsername(u.getUserName());
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
