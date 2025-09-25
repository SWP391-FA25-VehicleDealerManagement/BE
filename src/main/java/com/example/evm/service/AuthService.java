package com.example.evm.service;

import com.example.evm.entity.User;
import com.example.evm.repository.UserRepository;
import com.example.evm.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.security.core.AuthenticationException;

@Slf4j
@Service
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserRepository userRepository;

    public AuthService(AuthenticationManager authManager,
                       JwtUtil jwtUtil,
                       UserDetailsService userDetailsService,
                       TokenBlacklistService tokenBlacklistService,
                       UserRepository userRepository) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.userRepository = userRepository;
    }

    public User login(String username, String password) {
        try {
            log.info("Authenticating user: {}", username);

            // Bắt tất cả lỗi xác thực và chuyển về BadCredentialsException
            try {
                authManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
                log.info("Authentication successful for user: {}", username);
            } catch (AuthenticationException e) {
                log.warn("Authentication failed for user: {} - {}", username, e.getClass().getSimpleName());
                throw new BadCredentialsException("Invalid credentials", e);
            }

            // Lấy thông tin từ UserDetails
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            String role = userDetails.getAuthorities().iterator().next().getAuthority();
            log.info("User role: {}", role);

            // Lấy thông tin đầy đủ từ database
            User user = userRepository.findByUserName(username)
                    .orElseThrow(() -> new BadCredentialsException("User not found in database: " + username));

            log.info("User found in database: {} with ID: {}", user.getUserName(), user.getUserId());
            return user;

        } catch (BadCredentialsException e) {
            throw e; // Để controller xử lý và trả về 401
        } catch (Exception e) {
            log.error("Unexpected error during login for user: {}", username, e);
            throw new RuntimeException("Login failed due to server error", e);
        }
    }

    public void logout(String token) {
        try {
            log.info("Logging out token");
            tokenBlacklistService.blacklistToken(token);
            log.info("Token blacklisted successfully");
        } catch (Exception e) {
            log.error("Error during logout", e);
            throw new RuntimeException("Logout failed: " + e.getMessage(), e);
        }
    }
}