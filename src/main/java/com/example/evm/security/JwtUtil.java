package com.example.evm.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.evm.service.TokenBlacklistService;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtUtil {

    private final Key key;
    private final long expiration;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtParser parser;

    public JwtUtil(TokenBlacklistService tokenBlacklistService,
                   @Value("${jwt.secret}") String secretKey,
                   @Value("${jwt.expiration}") long expiration) {
        this.tokenBlacklistService = tokenBlacklistService;
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.expiration = expiration;
        this.parser = Jwts.parserBuilder().setSigningKey(key).build();
    }

    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        try {
            return parser.parseClaimsJws(token).getBody().getSubject();
        } catch (JwtException e) {
            log.warn("Failed to extract username from token: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                log.info("Token is blacklisted: {}", token);
                return false;
            }
            parser.parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            log.warn("Token is invalid: {}", e.getMessage());
            return false;
        }
    }

    public Date getExpirationDate(String token) {
        try {
            return parser.parseClaimsJws(token).getBody().getExpiration();
        } catch (JwtException e) {
            log.warn("Failed to get expiration date: {}", e.getMessage());
            return new Date(System.currentTimeMillis() + expiration);
        }
    }

    public long getRemainingTime(String token) {
        try {
            Date expirationDate = getExpirationDate(token);
            return expirationDate.getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            log.warn("Failed to calculate remaining time: {}", e.getMessage());
            return expiration;
        }
    }
}