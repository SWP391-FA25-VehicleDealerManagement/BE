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
        LoginResponse resp = authService.login(req);
        return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", resp));
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
        userProfileService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Password changed successfully", "OK"));
    }
    // Update user's profile
    @PutMapping("/update-user")
    public ResponseEntity<ApiResponse<UserInfo>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserInfo updated = userProfileService.updateProfile(authentication.getName(), request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile updated successfully", updated));
    }

    // Forgot password
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<ForgotPasswordResponse>> forgotPassword(
        @Valid @RequestBody ForgotPasswordRequest request) {
        try {
            ForgotPasswordResponse response = userProfileService.forgotPassword(request);
            log.info("Forgot password request processed successfully for username: {}", request.getUsername());
            return ResponseEntity.ok(new ApiResponse<>(true, "Password reset successfully", response));
        } catch (Exception e) {
            log.error("Forgot password failed for username: {} and email: {}", 
            request.getUsername(), request.getEmail(), e);
    
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false, 
                    "Password reset failed: " + e.getMessage(), 
                    null
            ));
        }
    }
}
