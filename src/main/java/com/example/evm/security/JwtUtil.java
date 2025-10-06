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

    public Claims getClaims(String token) {
        try {
            return parser.parseClaimsJws(token).getBody();
        } catch (JwtException e) {
            log.warn("Cannot extract claims: {}", e.getMessage());
            return null;
        }
    }

    public String extractUsername(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    public String getRole(String token) {
        Claims claims = getClaims(token);
        return (claims != null) ? claims.get("role", String.class) : null;
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
        Claims claims = getClaims(token);
        return claims != null ? claims.getExpiration() : null;
    }

    public long getRemainingTime(String token) {
        Date expirationDate = getExpirationDate(token);
        return expirationDate != null
                ? expirationDate.getTime() - System.currentTimeMillis()
                : 0;
    }

    public long getExpirationInSeconds() {
        return expirationMs / 1000;
    }
}
