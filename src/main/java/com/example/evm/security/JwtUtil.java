package com.example.evm.security;

import com.example.evm.service.auth.TokenBlacklistService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;

@Slf4j
@Component
public class JwtUtil {

    private final Key key;
    private final long expirationMs;
    private final TokenBlacklistService blacklistService;
    private final JwtParser parser;

    public JwtUtil(TokenBlacklistService blacklistService,
                   @Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expirationMs) {
        this.blacklistService = blacklistService;
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

    public String extractUsername(String token) {
        try {
            return parser.parseClaimsJws(token).getBody().getSubject();
    
    // >>> PHƯƠNG THỨC MỚI: Lấy Claims (Bắt buộc cho Filter)
    public Claims getClaims(String token) {
        try {
            return parser.parseClaimsJws(token).getBody();
        } catch (JwtException e) {
            log.warn("Cannot extract claims: {}", e.getMessage());
            return null;
        }
    }

    // >>> PHƯƠNG THỨC MỚI: Lấy Role (Bắt buộc cho Filter)
    public String getRole(String token) {
        Claims claims = getClaims(token);
        if (claims != null && claims.containsKey("role")) {
            return claims.get("role", String.class);
        }
        return null;
    }

    public String extractUsername(String token) {
        try {
            return getClaims(token).getSubject(); // Sử dụng phương thức getClaims
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
            return parser.parseClaimsJws(token).getBody().getExpiration();
            return getClaims(token).getExpiration(); // Sử dụng phương thức getClaims
        } catch (JwtException e) {
            log.warn("Cannot get expiration: {}", e.getMessage());
            return new Date(System.currentTimeMillis() + expirationMs);
        }
    }

    public long getRemainingTime(String token) {
        return getExpirationDate(token).getTime() - System.currentTimeMillis();
    }

    public long getExpirationInSeconds() {
        return expirationMs / 1000;
    }
}
}
