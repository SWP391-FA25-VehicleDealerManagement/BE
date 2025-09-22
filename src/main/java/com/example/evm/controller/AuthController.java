package com.example.evm.controller;

import com.example.evm.service.AuthService;
import lombok.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
