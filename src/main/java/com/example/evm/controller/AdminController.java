package com.example.evm.controller;

import com.example.evm.entity.User;
import com.example.evm.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;


@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, String>> dashboard() {
        return ResponseEntity.ok(Map.of("message", "Welcome to Admin Dashboard!"));
    }

    @PostMapping("/create-admin")
    public ResponseEntity<Map<String, String>> createAdmin() {
        try {
            if (userRepository.findByUserName("admin1").isPresent()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Admin user already exists"));
            }

            String encodedPassword = passwordEncoder.encode("123456");

            User adminUser = new User();
            adminUser.setUserName("admin1");
            adminUser.setPassword(encodedPassword);
            adminUser.setRole("ADMIN");
            adminUser.setCreatedDate(LocalDateTime.now());

            userRepository.save(adminUser);

            log.info("Admin user created: {}", adminUser.getUserName());
            return ResponseEntity.ok(Map.of("message", "Admin user created successfully"));

        } catch (Exception e) {
            log.error("Failed to create admin user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to create admin user",
                            "details", e.getMessage()
                    ));
        }
    }

    @GetMapping("/check-user/{username}")
    public ResponseEntity<Map<String, Object>> checkUser(@PathVariable String username) {
        Optional<User> user = userRepository.findByUserName(username);
        if (user.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "userId", user.get().getUserId(),
                    "username", user.get().getUserName(),
                    "role", user.get().getRole(),
                    "email", user.get().getEmail()
            ));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "User not found"));
    }
}
