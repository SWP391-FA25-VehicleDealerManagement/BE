package com.example.evm.config;

import com.example.evm.entity.User;
import com.example.evm.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PasswordHashRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Tạo admin user mặc định và băm password
    @Bean
    public CommandLineRunner initializeDefaultUsers() {
        return args -> {
            log.info("Starting user initialization process...");
            
            try {
                // Tạo admin user mặc định nếu chưa có
                if (!userRepository.findByUserName("admin1").isPresent()) {
                    User adminUser = new User();
                    adminUser.setUserName("admin1");
                    adminUser.setPassword(passwordEncoder.encode("123456"));
                    adminUser.setRole("ADMIN");
                    adminUser.setEmail("admin@example.com");
                    adminUser.setPhone("0123456789");
                    adminUser.setCreatedDate(java.time.LocalDateTime.now());
                    adminUser.setDateModified(java.time.LocalDateTime.now());
                    
                    userRepository.save(adminUser);
                    log.info("Created default admin user: admin1 with password: 123456");
                } else {
                    log.info("Admin user already exists");
                }

                // Băm tất cả password plain text hiện tại trong DB
                var users = userRepository.findAll();
                int hashedCount = 0;
                
                for (User user : users) {
                    String currentPassword = user.getPassword();
                    
                    // Kiểm tra xem password đã được hash chưa (BCrypt format)
                    if (currentPassword != null && !currentPassword.startsWith("$2a$") && !currentPassword.startsWith("$2b$")) {
                        // Hash password và lưu lại
                        String hashedPassword = passwordEncoder.encode(currentPassword);
                        user.setPassword(hashedPassword);
                        userRepository.save(user);
                        
                        hashedCount++;
                        log.info("Password hashed for user: {} - Password length: {}", 
                               user.getUserName(), currentPassword.length());
                    } else {
                        log.debug("Password already hashed for user: {}", user.getUserName());
                    }
                }
                
                log.info("User initialization completed. {} passwords were hashed.", hashedCount);
                
            } catch (Exception e) {
                log.error("Error during user initialization process", e);
            }
        };
    }
}
