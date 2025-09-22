package com.example.evm.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

@Service
public class TokenBlacklistService {
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }

    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    public void removeExpiredTokens() {
        // Có thể thêm logic để xóa token hết hạn khỏi blacklist
        // Để đơn giản, chúng ta sẽ giữ tất cả token trong blacklist
        // Trong thực tế, nên có cơ chế cleanup định kỳ
    }
}
