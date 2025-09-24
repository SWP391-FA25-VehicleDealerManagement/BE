package com.example.evm.config;

import com.example.evm.repository.UserRepository;
import com.example.evm.service.PasswordDebugService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordHashRunner {

    // Băm tất cả password plain text hiện tại trong DB khi ứng dụng khởi động
    @Bean
    public CommandLineRunner encodeExistingPasswords(UserRepository userRepository, PasswordEncoder passwordEncoder, PasswordDebugService passwordDebugService) {
        return args -> {
            userRepository.findAll().forEach(user -> {
                String raw = user.getPassword();
                if (!raw.startsWith("$2a$")) {
                    // Lưu password gốc vào debug service
                    passwordDebugService.storeOriginalPassword(user.getUserName(), raw);
                    // Hash password và lưu vào password
                    user.setPassword(passwordEncoder.encode(raw));
                    userRepository.save(user);
                    System.out.println("Password hashed for user: " + user.getUserName() + " - Original: " + raw);
                }
            });
        };
    }
}
