package com.example.evm;

import com.example.evm.entity.Role;
import com.example.evm.entity.User;
import com.example.evm.repository.RoleRepository;
import com.example.evm.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class EvmApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvmApplication.class, args);
    }
    // Băm tất cả password plain text hiện tại trong DB
    @Bean
    CommandLineRunner encodeExistingPasswords(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            userRepository.findAll().forEach(user -> {
                String raw = user.getPasswordHash();
                if (!raw.startsWith("$2a$")) {
                    user.setPasswordHash(passwordEncoder.encode(raw));
                    userRepository.save(user);
                    System.out.println("Password hashed for user: " + user.getUsername());
                }
            });
        };
    }
}
