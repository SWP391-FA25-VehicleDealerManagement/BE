package com.example.evm.service.evmstaff;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.example.evm.repository.auth.UserRepository;
import com.example.evm.entity.user.User;
import com.example.evm.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
@Service
@RequiredArgsConstructor
public class EvmStaffServiceImpl implements EvmStaffService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
        
    @Override
    public User createEvmStaff(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("EVM_STAFF");
        user.setCreatedDate(LocalDateTime.now());
        user.setDateModified(LocalDateTime.now());
        return userRepository.save(user);
    }
    @Override
    public User getEvmStaffById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EVM Staff not found"));
    }
    @Override
    public User updateEvmStaff(Long id, User user) {
        return userRepository.findById(id)
        .map(existingUser -> {
            if (user.getPassword() != null) {
                existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            if (user.getFullName() != null) {
                existingUser.setFullName(user.getFullName());
            }
            if (user.getPhone() != null) {
                existingUser.setPhone(user.getPhone());
            }
            if (user.getEmail() != null) {
                existingUser.setEmail(user.getEmail());
            }
            if (user.getRole() != null) {
                existingUser.setRole(user.getRole());
            }
            return userRepository.save(existingUser);
        }).orElseThrow(() -> new ResourceNotFoundException("EVM Staff not found"));
    }
    @Override
    public void deleteEvmStaff(Long id) {
        userRepository.findById(id)
        .map(existingUser -> {
            userRepository.delete(existingUser);
            return existingUser;
        }).orElseThrow(() -> new ResourceNotFoundException("EVM Staff not found"));
    }
    @Override
    public List<User> getAllEvmStaff() {
        return userRepository.findByRole("EVM_STAFF");
    }
}
