package com.example.evm.service.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class TokenBlacklistService {

    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    public void blacklist(String token, Date expiry) {
        if (token == null || token.isBlank()) {
            return;
        }
        long expiresAt = (expiry != null) ? expiry.getTime() : System.currentTimeMillis();
        blacklist.put(token, expiresAt);
        log.debug("Blacklisted token until {}", new Date(expiresAt));
    }

    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        Long expiresAt = blacklist.get(token);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt < System.currentTimeMillis()) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }
}