package com.example.evm.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.evm.entity.Role;
import com.example.evm.entity.User;
import com.example.evm.repository.RoleRepository;
import com.example.evm.repository.UserRepository;

@RestController
public class AdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard() {
        return "Welcome to Admin Dashboard!";
    }

    @PostMapping("/admin/create-admin")
    public ResponseEntity<Map<String, String>> createAdmin() {
        try {
            Role adminRole = roleRepository.findByRoleName("ADMIN")
                    .orElseGet(() -> roleRepository.save(new Role("ADMIN", "Administrator role")));

            Optional<User> existingUser = userRepository.findByUsername("admin1");
            if (existingUser.isPresent()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "Admin user already exists"));
            }

            String encodedPassword = passwordEncoder.encode("123456"); // có thể lấy từ config
            User adminUser = new User("admin1", encodedPassword, adminRole);
            userRepository.save(adminUser);

            return ResponseEntity.ok(Map.of("message", "Admin user created successfully"));
        } catch (Exception e) {
            e.printStackTrace(); // log stack trace để debug
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Failed to create admin user",
                    "details", e.getMessage()
                ));
        }
    }


    // Trả về thông tin đối tượng người dùng theo json 
    @GetMapping("/admin/check-user/{username}")
    public ResponseEntity<Map<String, Object>> checkUser(@PathVariable String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "username", user.get().getUsername(),
                    "role", user.get().getRole().getRoleName(),
                    "userId", user.get().getUserId()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "User not found"));
    }

}
