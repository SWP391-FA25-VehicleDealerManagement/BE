package com.example.evm.controller.auth;

import com.example.evm.dto.auth.*;
import com.example.evm.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

  

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req) {
        LoginResponse resp = authService.login(req);
        return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", resp));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserInfo>> me(Authentication authentication) {
        UserInfo info = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(true, "User info retrieved", info));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
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
}
