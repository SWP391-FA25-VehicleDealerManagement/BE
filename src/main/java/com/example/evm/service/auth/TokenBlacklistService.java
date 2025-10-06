package com.example.evm.service.auth;

<<<<<<< HEAD
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
=======
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

    // token → expiration timestamp (ms)
    private final ConcurrentMap<String, Long> blacklist = new ConcurrentHashMap<>();

    public void blacklist(String token, Date expiry) {
        blacklist.put(token, expiry.getTime());
    }

    public boolean isTokenBlacklisted(String token) {
        Long exp = blacklist.get(token);
        if (exp == null) return false;
        if (System.currentTimeMillis() > exp) {
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
            blacklist.remove(token);
            return false;
        }
        return true;
    }
}
<<<<<<< HEAD


=======
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
