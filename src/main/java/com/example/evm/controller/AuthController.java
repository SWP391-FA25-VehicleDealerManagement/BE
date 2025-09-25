package com.example.evm.controller;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.evm.entity.User;
import com.example.evm.repository.UserRepository;
import com.example.evm.security.JwtUtil;
import com.example.evm.service.AuthService;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, JwtUtil jwtUtil, UserRepository userRepository) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    /**
     * Test endpoint
     */
    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Auth endpoint is working!", "OK"));
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", java.time.LocalDateTime.now());
        response.put("service", "EVM Authentication Service");
        return ResponseEntity.ok(response);
    }

    /**
     * API login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        try {
            log.info("Login attempt for user: {}", request.getUsername());

            // Validate input
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Username is required", null));
            }
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Password is required", null));
            }

            // Check user login
            User user = authService.login(request.getUsername(), request.getPassword());
            log.info("User authenticated: {} with role: {}", user.getUserName(), user.getRole());

            // Generate JWT
            String token = jwtUtil.generateToken(user.getUserName(), user.getRole());
            Date expirationDate = jwtUtil.getExpirationDate(token);
            long remainingTime = jwtUtil.getRemainingTime(token);

            // Build dealer info if exists
            DealerInfo dealerInfo = null;
            if (user.getDealer() != null) {
                dealerInfo = new DealerInfo(
                    user.getDealer().getDealerId(),
                    user.getDealer().getDealerName(),
                    user.getDealer().getPhone(),
                    user.getDealer().getAddress(),
                    user.getDealer().getCreatedBy(),
                    user.getDealer().getCreatedDate()
                );
            }

            // Build complete user info
            UserInfo userInfo = new UserInfo(
                user.getUserId(),
                user.getUserName(),
                user.getPhone(),
                user.getEmail(),
                user.getRole(),
                dealerInfo,
                user.getCreatedDate(),
                user.getDateModified(),
                user.getRefreshTokenExpiryTime()
            );

            // Build token info
            TokenInfo tokenInfo = new TokenInfo(
                expirationDate,
                remainingTime,
                remainingTime / 1000 // expires in seconds
            );

            // Build response
            LoginResponse loginResponse = new LoginResponse(
                token,
                "Bearer",
                "Login successful",
                userInfo,
                tokenInfo
            );

            return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", loginResponse));

        } catch (BadCredentialsException e) {
            log.warn("Login failed for user: {} - Bad credentials", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(false, "Invalid username or password", null));
        } catch (Exception e) {
            log.error("Login error for user: {}", request.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Login failed due to server error", null));
        }
    }

    /**
     * Get current user info
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfo>> getCurrentUser(Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not authenticated", null));
            }

            String username = authentication.getName();
            User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

            // Build dealer info if exists
            DealerInfo dealerInfo = null;
            if (user.getDealer() != null) {
                dealerInfo = new DealerInfo(
                    user.getDealer().getDealerId(),
                    user.getDealer().getDealerName(),
                    user.getDealer().getPhone(),
                    user.getDealer().getAddress(),
                    user.getDealer().getCreatedBy(),
                    user.getDealer().getCreatedDate()
                );
            }

            // Build complete user info
            UserInfo userInfo = new UserInfo(
                user.getUserId(),
                user.getUserName(),
                user.getPhone(),
                user.getEmail(),
                user.getRole(),
                dealerInfo,
                user.getCreatedDate(),
                user.getDateModified(),
                user.getRefreshTokenExpiryTime()
            );

            return ResponseEntity.ok(new ApiResponse<>(true, "User info retrieved successfully", userInfo));

        } catch (Exception e) {
            log.error("Error getting current user info", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to get user info", null));
        }
    }

    /**
     * API logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<LogoutResponse>> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Invalid authorization header", null));
            }

            String token = authHeader.substring(7);
            authService.logout(token);

            LogoutResponse logoutResponse = new LogoutResponse("Logout successful");
            return ResponseEntity.ok(new ApiResponse<>(true, "Logout successful", logoutResponse));

        } catch (Exception e) {
            log.error("Logout error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Logout failed", null));
        }
    }
}

/* ==============================
   DTOs và Response Wrapper
   ============================== */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class LoginRequest {
    private String username;
    private String password;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class LoginResponse {
    private String accessToken;
    private String tokenType;
    private String message;
    private UserInfo user;
    private TokenInfo tokenInfo;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class TokenInfo {
    private Date expirationDate;
    private long remainingTimeInMs;
    private long expiresIn;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class UserInfo {
    private Integer userId;
    private String userName;
    private String phone;
    private String email;
    private String role;
    private DealerInfo dealer;
    private LocalDateTime createdDate;
    private LocalDateTime dateModified;
    private LocalDateTime refreshTokenExpiryTime;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class DealerInfo {
    private Integer dealerId;
    private String dealerName;
    private String phone;
    private String address;
    private String createdBy;
    private LocalDateTime createdDate;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class LogoutResponse {
    private String message;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
}