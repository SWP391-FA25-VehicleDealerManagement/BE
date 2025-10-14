package com.example.evm.service.init;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.evm.entity.user.User;
import com.example.evm.repository.auth.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initDefaultAdmin() {
        return args -> {
            // Tạo admin mặc định nếu chưa tồn tại
            if (userRepository.findByUserName("admin1").isEmpty()) {
                User admin = new User();
                admin.setUserName("admin1");
                admin.setPassword(passwordEncoder.encode("123456"));
                admin.setRole("ADMIN");
                admin.setEmail("admin@example.com");
                admin.setPhone("0123456789");
                admin.setCreatedDate(LocalDateTime.now());
                admin.setDateModified(LocalDateTime.now());
                userRepository.save(admin);
                log.info("Created default admin user (admin1/123456)");
            } else {
                log.info("Default admin already exists");
            }

            // Hash any plain‑text passwords that may have been added manually
            userRepository.findAll().forEach(u -> {
                if (u.getPassword() != null && !u.getPassword().startsWith("$2a$")) {
                    u.setPassword(passwordEncoder.encode(u.getPassword()));
                    userRepository.save(u);
                    log.info("Hashed password for user {}", u.getUserName());
                }
            });
        };
    }
}
