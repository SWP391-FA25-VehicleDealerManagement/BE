package com.example.evm.controller.auth;

import com.example.evm.dto.auth.*;
import com.example.evm.service.auth.AuthService;
<<<<<<< HEAD
import com.example.evm.service.auth.UserProfileService;

=======
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
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
<<<<<<< HEAD

public class AuthController {

    private final AuthService authService;
    private final UserProfileService userProfileService;

   
=======
public class AuthController {

    private final AuthService authService;

  

>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req) {
        LoginResponse resp = authService.login(req);
        return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", resp));
    }

    @GetMapping("/me")
<<<<<<< HEAD
    public ResponseEntity<ApiResponse<UserInfo>> me(Authentication authentication,
                                                   @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Unauthorized", null));
        }
=======
    public ResponseEntity<ApiResponse<UserInfo>> me(Authentication authentication) {
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
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
<<<<<<< HEAD

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        userProfileService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Password changed successfully", "OK"));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserInfo>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserInfo updated = userProfileService.updateProfile(authentication.getName(), request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile updated successfully", updated));
    }
=======
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
}
