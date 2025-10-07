package com.example.evm.security;

import com.example.evm.service.auth.TokenBlacklistService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    private final Key key; // Key ký JWT
    private final long expirationMs; // Thời gian hết hạn token (ms)
    private final TokenBlacklistService blacklistService;
    private final JwtParser parser; // Parser để parse token

    public JwtUtil(TokenBlacklistService blacklistService,
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMs) {

        this.blacklistService = blacklistService;
        if (secret == null || secret.isEmpty()) {
            throw new IllegalArgumentException("JWT secret is not configured!");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
        this.parser = Jwts.parserBuilder().setSigningKey(key).build();
    }

    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    // PHƯƠNG THỨC CHUNG: Lấy Claims từ Token
    public Claims getClaims(String token) {
        try {
            return parser.parseClaimsJws(token).getBody();
        } catch (JwtException e) {
            log.warn("Cannot extract claims: {}", e.getMessage());
            return null;
        }
    }

    // Lấy Role từ Claims
    public String getRole(String token) {
        Claims claims = getClaims(token);
        if (claims != null && claims.containsKey("role")) {
            return claims.get("role", String.class);
        }
        return null;
    }

    // Lấy Username từ Claims
    public String extractUsername(String token) {
        try {
            Claims claims = getClaims(token);
            if (claims != null) {
                 return claims.getSubject();
            }
            return null;
        } catch (JwtException e) {
            log.warn("Cannot extract username: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        if (blacklistService.isTokenBlacklisted(token)) {
            log.info("Token is blacklisted");
            return false;
        }
        try {
            parser.parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            log.warn("Invalid token: {}", e.getMessage());
            return false;
        }
    }

    public Date getExpirationDate(String token) {
        try {
            Claims claims = getClaims(token);
            if (claims != null) {
                return claims.getExpiration();
            }
            return null;
        } catch (JwtException e) {
            log.warn("Cannot get expiration: {}", e.getMessage());
            // Trả về một giá trị mặc định để tránh null, hoặc xử lý bằng try-catch bên ngoài
            return new Date(System.currentTimeMillis() + expirationMs); 
        }
    }

    public long getRemainingTime(String token) {
        Date expirationDate = getExpirationDate(token);
        if (expirationDate == null) {
            return 0; // Xử lý nếu ngày hết hạn không thể lấy được
        }
        return expirationDate.getTime() - System.currentTimeMillis();
    }

    public long getExpirationInSeconds() {
        return expirationMs / 1000;
    }
}
