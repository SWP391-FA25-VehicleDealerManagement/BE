package com.example.evm.controller.account;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.example.evm.entity.user.User;
import com.example.evm.repository.auth.UserRepository;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    

    // Kiểm tra quyền ADMIN
    private boolean isAdmin(Authentication authentication) {
        return authentication != null &&
                authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // Tạo admin mới
    @PostMapping("/create-admin")
    public ResponseEntity<Map<String, String>> createAdmin(@RequestBody Map<String, String> request,
            Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied: Admins only"));
        }

        try {
            String username = request.get("username");
            String password = request.get("password");

            if (username == null || password == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Username and password are required"));
            }

            if (userRepository.findByUserName(username).isPresent()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Admin user already exists"));
            }

            User adminUser = new User();
            adminUser.setUserName(username);
            adminUser.setPassword(passwordEncoder.encode(password));
            adminUser.setRole("ADMIN");
            adminUser.setCreatedDate(LocalDateTime.now());

            userRepository.save(adminUser);

            log.info("Admin user created: {}", adminUser.getUserName());
            return ResponseEntity.ok(Map.of("message", "Admin user created successfully"));

        } catch (Exception e) {
            log.error("Failed to create admin user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create admin user", "details", e.getMessage()));
        }
    }

    // Check user
    @GetMapping("/check-user/{username}")
    public ResponseEntity<Map<String, Object>> checkUser(@PathVariable String username,
            Authentication authentication) {
        if (!isAdmin(authentication)) {
            Map<String, Object> forbiddenBody = new HashMap<>();
            forbiddenBody.put("error", "Access denied: Admins only");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(forbiddenBody);
        }

        Optional<User> userOpt = userRepository.findByUserName(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Map<String, Object> body = new HashMap<>();
            body.put("userId", user.getUserId());
            body.put("username", user.getUserName());
            body.put("role", user.getRole());
            body.put("email", user.getEmail());
            body.put("createdDate", user.getCreatedDate());
            return ResponseEntity.ok(body);
        } else {
            Map<String, Object> notFoundBody = new HashMap<>();
            notFoundBody.put("error", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundBody);
        }
    }

    // Danh sách user
    @GetMapping("/all-users")
    public ResponseEntity<Map<String, Object>> getAllUsers(Authentication authentication) {
        if (!isAdmin(authentication)) {
            Map<String, Object> forbiddenBody = new HashMap<>();
            forbiddenBody.put("error", "Access denied: Admins only");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(forbiddenBody);
        }

        try {
            List<User> users = userRepository.findAll();
            List<Map<String, Object>> userList = users.stream().map(user -> {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("userId", user.getUserId());
                userMap.put("username", user.getUserName());
                userMap.put("role", user.getRole());
                userMap.put("email", user.getEmail());
                userMap.put("phone", user.getPhone());
                userMap.put("createdDate", user.getCreatedDate());
                userMap.put("dateModified", user.getDateModified());
                return userMap;
            }).toList();

            Map<String, Object> response = new HashMap<>();
            response.put("totalUsers", users.size());
            response.put("users", userList);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to retrieve users", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve users: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
