package com.example.evm.service.dealer;

import com.example.evm.dto.dealer.CreateDealerAccountRequest;
import com.example.evm.dto.dealer.CreateDealerAccountResponse;
import com.example.evm.entity.dealer.Dealer;
import com.example.evm.entity.user.User;
import com.example.evm.repository.UserRepository;
import com.example.evm.repository.dealer.DealerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DealerAccountService {

    private static final Logger log = LoggerFactory.getLogger(DealerAccountService.class);

    private final DealerRepository dealerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DealerAccountService(DealerRepository dealerRepository, UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.dealerRepository = dealerRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CreateDealerAccountResponse createDealerAccount(CreateDealerAccountRequest request) {
        // Kiểm tra username đã tồn tại chưa
        if (userRepository.findByUserName(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        // Kiểm tra dealer name đã tồn tại chưa
        if (dealerRepository.findByDealerName(request.getDealerName()).isPresent()) {
            throw new IllegalArgumentException("Dealer name already exists: " + request.getDealerName());
        }

        // Validate role
        if (!isValidDealerRole(request.getRole())) {
            throw new IllegalArgumentException("Invalid role. Must be ROLE_DEALER_STAFF or ROLE_DEALER_MANAGER");
        }

        try {
            // 1. Tạo Dealer entity
            Dealer dealer = new Dealer();
            dealer.setDealerName(request.getDealerName());
            dealer.setPhone(request.getDealerPhone());
            dealer.setAddress(request.getDealerAddress());
            dealer.setCreatedBy("ADMIN"); // hoặc lấy từ authentication context
            dealer.setCreatedDate(LocalDateTime.now());

            Dealer savedDealer = dealerRepository.save(dealer);
            log.info("Created dealer: {}", savedDealer.getDealerName());

            // 2. Tạo User account
            User user = new User();
            user.setUserName(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFullName(request.getFullName());
            user.setPhone(request.getUserPhone());
            user.setEmail(request.getEmail());
            user.setRole(request.getRole());
            user.setDealer(savedDealer); // Liên kết với dealer
            user.setCreatedDate(LocalDateTime.now());

            User savedUser = userRepository.save(user);
            log.info("Created user: {} for dealer: {}", savedUser.getUserName(), savedDealer.getDealerName());

            // 3. Tạo response
            CreateDealerAccountResponse response = new CreateDealerAccountResponse();
            response.setDealerId(savedDealer.getDealerId());
            response.setDealerName(savedDealer.getDealerName());
            response.setUserId(savedUser.getUserId());
            response.setUsername(savedUser.getUserName());
            response.setRole(savedUser.getRole());
            response.setMessage("Dealer account created successfully");

            return response;

        } catch (Exception e) {
            log.error("Failed to create dealer account", e);
            throw new RuntimeException("Failed to create dealer account: " + e.getMessage());
        }
    }

    private boolean isValidDealerRole(String role) {
        return "ROLE_DEALER_STAFF".equals(role) || "ROLE_DEALER_MANAGER".equals(role);
    }
}