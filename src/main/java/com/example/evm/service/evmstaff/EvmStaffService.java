package com.example.evm.service.evmstaff;

import com.example.evm.entity.user.User;
import java.util.List;
public interface EvmStaffService {

    User createEvmStaff(User user);
    User getEvmStaffById(Long id);
    User updateEvmStaff(Long id,User user);
    void deleteEvmStaff(Long id);
    List<User> getAllEvmStaff();
} 
