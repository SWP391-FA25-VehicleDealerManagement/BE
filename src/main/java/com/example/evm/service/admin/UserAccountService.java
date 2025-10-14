package com.example.evm.service.admin;

import com.example.evm.dto.admin.CreateUserAccountRequest;
import com.example.evm.dto.admin.CreateUserAccountResponse;
import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.user.User;
import com.example.evm.repository.auth.UserRepository;
import com.example.evm.repository.dealer.DealerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserAccountService {

    private static final Logger log = LoggerFactory.getLogger(UserAccountService.class);

    private final UserRepository userRepository;
    private final DealerRepository dealerRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(UserRepository userRepository, DealerRepository dealerRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.dealerRepository = dealerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CreateUserAccountResponse createUserAccount(CreateUserAccountRequest request) {
        // Kiểm tra username đã tồn tại chưa
        if (userRepository.findByUserName(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        // Validate role
        if (!isValidRole(request.getRole())) {
            throw new IllegalArgumentException(
                    "Invalid role. Supported roles: ROLE_DEALER_STAFF, ROLE_DEALER_MANAGER, ROLE_EVM_STAFF");
        }

        // Nếu role là DEALER_STAFF hoặc DEALER_MANAGER thì cần dealerId
        if (isDealerRole(request.getRole())) {
            if (request.getDealerId() == null) {
                throw new IllegalArgumentException("DealerId is required for dealer roles");
            }

            // Kiểm tra dealer có tồn tại không
            if (!dealerRepository.existsById(request.getDealerId())) {
                throw new IllegalArgumentException("Dealer not found with id: " + request.getDealerId());
            }
        }

        try {
            // Tạo User account
            User user = new User();
            user.setUserName(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFullName(request.getFullName());
            user.setPhone(request.getPhone());
            user.setEmail(request.getEmail());
            user.setRole(request.getRole());
            user.setCreatedDate(LocalDateTime.now());

            // Nếu là dealer role thì set dealer
            if (isDealerRole(request.getRole()) && request.getDealerId() != null) {
                Optional<Dealer> dealer = dealerRepository.findById(request.getDealerId());
                dealer.ifPresent(user::setDealer);
            }

            User savedUser = userRepository.save(user);
            log.info("Created user: {} with role: {}", savedUser.getUserName(), savedUser.getRole());

            // Tạo response
            CreateUserAccountResponse response = new CreateUserAccountResponse();
            response.setUserId(savedUser.getUserId());
            response.setUsername(savedUser.getUserName());
            response.setRole(savedUser.getRole());
            response.setFullName(savedUser.getFullName());
            response.setEmail(savedUser.getEmail());

            if (savedUser.getDealer() != null) {
                response.setDealerId(savedUser.getDealer().getDealerId());
                response.setDealerName(savedUser.getDealer().getDealerName());
            }

            response.setMessage("User account created successfully");

            return response;

        } catch (Exception e) {
            log.error("Failed to create user account", e);
            throw new RuntimeException("Failed to create user account: " + e.getMessage());
        }
    }

    private boolean isValidRole(String role) {
        return "ROLE_DEALER_STAFF".equals(role) ||
                "ROLE_DEALER_MANAGER".equals(role) ||
                "ROLE_EVM_STAFF".equals(role);
    }

    private boolean isDealerRole(String role) {
        return "ROLE_DEALER_STAFF".equals(role) || "ROLE_DEALER_MANAGER".equals(role);
    }
}