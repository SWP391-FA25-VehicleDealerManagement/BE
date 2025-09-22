package com.example.evm.controller;

import com.example.evm.entity.Role;
import com.example.evm.entity.User;
import com.example.evm.repository.RoleRepository;
import com.example.evm.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class AdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard() {
        return "Welcome to Admin Dashboard!";
    }

    @PostMapping("/admin/create-admin")
    public String createAdmin() {
        try {
            // Tạo role ADMIN nếu chưa có
            Role adminRole = roleRepository.findByRoleName("ADMIN")
                    .orElseGet(() -> {
                        Role role = new Role("ADMIN", "Administrator role");
                        return roleRepository.save(role);
                    });

            // Kiểm tra user admin1 đã tồn tại chưa
            Optional<User> existingUser = userRepository.findByUsername("admin1");
            if (existingUser.isPresent()) {
                return "User admin1 already exists. Password: " + existingUser.get().getPasswordHash();
            }

            // Tạo user admin1
            String encodedPassword = passwordEncoder.encode("123456");
            User adminUser = new User("admin1", encodedPassword, adminRole);
            userRepository.save(adminUser);

            return "Admin user created successfully. Username: admin1, Password: 123456, Encoded: " + encodedPassword;
        } catch (Exception e) {
            return "Error creating admin user: " + e.getMessage();
        }
    }

    @GetMapping("/admin/check-user/{username}")
    public String checkUser(@PathVariable String username) {
        try {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                return "User found: " + username + ", Password Hash: " + user.get().getPasswordHash() + 
                    ", Role: " + user.get().getRole().getRoleName();
            } else {
                return "User not found: " + username;
            }
        } catch (Exception e) {
            return "Error checking user: " + e.getMessage();
        }
    }
}
