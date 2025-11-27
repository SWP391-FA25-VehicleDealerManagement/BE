package com.example.evm.service.auth;

import com.example.evm.dto.auth.*;
import com.example.evm.entity.user.User;
import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.mapper.UserMapper;
import com.example.evm.repository.auth.UserRepository;
import com.example.evm.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Business‑logic cho các thao tác: login, logout, lấy thông tin hiện tại.
 * Tất cả các exception được ném ra để {@link com.example.evm.exception.GlobalExceptionHandler}
 * xử lý và trả về JSON chuẩn.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserMapper userMapper;                     // <-- inject mapper

    /**
     * Xác thực username / password, sinh JWT và trả về DTO đầy đủ.
     */
    public LoginResponse login(LoginRequest request) {
        //  Xác thực bằng Spring AuthenticationManager
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(),
                                                          request.getPassword()));
        } catch (AuthenticationException ex) {
            log.warn("Bad credentials for user {}", request.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        }

        //  Tìm user trong DB
        User user = userRepository.findByUserName(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        //  Tạo JWT
        String token = jwtUtil.generateToken(user.getUserName(), user.getRole());

        //  Cập nhật thời gian hết hạn refresh‑token (tùy chọn)
        user.setRefreshTokenExpiryTime(LocalDateTime.now()
                .plusSeconds(jwtUtil.getExpirationInSeconds()));
        userRepository.save(user);

        //  Build DTOs (UserInfo + TokenInfo)
        UserInfo userInfo = userMapper.toUserInfo(user);
        TokenInfo tokenInfo = new TokenInfo(
                jwtUtil.getExpirationDate(token),
                jwtUtil.getRemainingTime(token),
                jwtUtil.getRemainingTime(token) / 1000L
        );

        log.info("User {} logged in, role {}", user.getUserName(), user.getRole());
        return new LoginResponse(token, "Bearer", "Login successful", userInfo, tokenInfo);
    }

    /**
     * Đưa token vào blacklist → token không còn hiệu lực.
     */
    public void logout(String token) {
        if (token == null || token.isBlank()) {
            log.warn("Logout called with empty token");
            return;
        }
        tokenBlacklistService.blacklist(token, jwtUtil.getExpirationDate(token));
        log.info("Token blacklisted (logout)");
    }

    /**
     * Trả về thông tin người dùng hiện tại (được lấy từ SecurityContext
     * trong AuthController → truyền username vào đây).
     */
    public UserInfo getCurrentUser(String username) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toUserInfo(user);
    }
}
