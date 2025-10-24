package com.example.evm.controller.auth;

import com.example.evm.dto.auth.*;
import com.example.evm.service.auth.AuthService;
import com.example.evm.service.auth.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserProfileService userProfileService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req) {
        try {
            LoginResponse resp = authService.login(req);
            return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", resp));
        } catch (Exception e) {
            log.error("Login failed for username: {}", req.getUsername(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Login failed: " + e.getMessage(), null));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfo>> me(Authentication authentication,
                                                   @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Unauthorized", null));
        }
        UserInfo info = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "User info retrieved", info));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<LogoutResponse>> logout(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Invalid Authorization header", null));
        }

        String token = authHeader.substring(7);
        authService.logout(token);
        return ResponseEntity.ok(new ApiResponse<>(true,
                "Logout successful", new LogoutResponse("Logout successful")));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        try {
            userProfileService.changePassword(authentication.getName(), request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Password changed successfully", "OK"));
        } catch (Exception e) {
            log.error("Change password failed for user: {}", authentication.getName(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Change password failed: " + e.getMessage(), null));
        }
    }

    @PutMapping("/update-user")
    public ResponseEntity<ApiResponse<UserInfo>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        try {
            UserInfo updated = userProfileService.updateProfile(authentication.getName(), request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Profile updated successfully", updated));
        } catch (Exception e) {
            log.error("Update profile failed for user: {}", authentication.getName(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Update profile failed: " + e.getMessage(), null));
        }
    }

  
}