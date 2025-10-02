package com.example.evm.service.auth;

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
            blacklist.remove(token);
            return false;
        }
        return true;
    }
}
