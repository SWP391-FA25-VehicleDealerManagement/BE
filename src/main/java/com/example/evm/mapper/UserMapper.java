package com.example.evm.mapper;

import com.example.evm.dto.admin.AdminUserInfoResponse;
import com.example.evm.dto.auth.DealerInfo;
import com.example.evm.dto.auth.UserInfo;
import com.example.evm.entity.user.User;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserInfo toUserInfo(User u) {
        if (u == null) {
            return null;
        }
        
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(u.getUserId());
        userInfo.setUserName(u.getUserName());
        userInfo.setFullName(u.getFullName());
        userInfo.setPhone(u.getPhone());
        userInfo.setEmail(u.getEmail());
        userInfo.setRole(u.getRole());
        userInfo.setCreatedDate(u.getCreatedDate());
        userInfo.setDateModified(u.getDateModified());
        userInfo.setRefreshTokenExpiryTime(u.getRefreshTokenExpiryTime());
        
        // Map dealer info if exists
        if (u.getDealer() != null) {
            DealerInfo dealerInfo = new DealerInfo();
            dealerInfo.setDealerId(u.getDealer().getDealerId());
            dealerInfo.setDealerName(u.getDealer().getDealerName());
            dealerInfo.setPhone(u.getDealer().getPhone());
            dealerInfo.setAddress(u.getDealer().getAddress());
            dealerInfo.setCreatedBy(u.getDealer().getCreatedBy());
            dealerInfo.setCreatedDate(u.getDealer().getCreatedDate());
            userInfo.setDealer(dealerInfo);
        }
        
        return userInfo;
    }

    public AdminUserInfoResponse toAdminInfo(User u) {
        if (u == null) {
            return null;
        }
        
        AdminUserInfoResponse adminInfo = new AdminUserInfoResponse();
        adminInfo.setUserId(u.getUserId());
        adminInfo.setUsername(u.getUserName());
        adminInfo.setRole(u.getRole());
        adminInfo.setEmail(u.getEmail());
        adminInfo.setPhone(u.getPhone());
        adminInfo.setCreatedDate(u.getCreatedDate());
        adminInfo.setDateModified(u.getDateModified());
        adminInfo.setRefreshTokenExpiryTime(u.getRefreshTokenExpiryTime());
        
        // Map dealer info if exists
        if (u.getDealer() != null) {
            adminInfo.setDealerId(u.getDealer().getDealerId());
            adminInfo.setDealerName(u.getDealer().getDealerName());
        }
        
        return adminInfo;
    }
}
