
package com.example.evm.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.evm.service.AuthService;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(new LoginResponse(token, "Login successful"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
            return ResponseEntity.ok(new LogoutResponse("Logout successful"));
        }
        return ResponseEntity.badRequest().body(new LogoutResponse("Invalid token"));
    }
}

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
class LoginRequest {
    private String username;
    private String password;
}

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
class LoginResponse {
    private String token;
    private String message;
}

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
class LogoutResponse {
    private String message;
}
