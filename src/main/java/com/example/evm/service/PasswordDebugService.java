package com.example.evm.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class PasswordDebugService {
    
    // Lưu password gốc trong memory (chỉ để debug)
    private final Map<String, String> originalPasswords = new ConcurrentHashMap<>();
    
    public void storeOriginalPassword(String username, String originalPassword) {
        originalPasswords.put(username, originalPassword);
        System.out.println("DEBUG - Original password for " + username + ": " + originalPassword);
    }
    
    public String getOriginalPassword(String username) {
        return originalPasswords.get(username);
    }
    
    public Map<String, String> getAllOriginalPasswords() {
        return new ConcurrentHashMap<>(originalPasswords);
    }
    
    public void clearPasswords() {
        originalPasswords.clear();
    }
}
