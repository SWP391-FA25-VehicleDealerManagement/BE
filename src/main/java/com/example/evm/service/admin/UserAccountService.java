package com.example.evm.service.admin;

import com.example.evm.dto.admin.CreateUserAccountRequest;
import com.example.evm.dto.admin.CreateUserAccountResponse;
import com.example.evm.dto.admin.UserAccountInfoResponse;
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

        // Validate email format if provided
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (!isValidEmail(request.getEmail())) {
                throw new IllegalArgumentException("Invalid email format: " + request.getEmail());
            }
            //  Kiểm tra email đã tồn tại chưa
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + request.getEmail());
            }
        }

        // Validate role (accept both with or without ROLE_ prefix)
        if (!isValidRole(request.getRole())) {
            throw new IllegalArgumentException(
                    "Invalid role. Supported roles: DEALER_STAFF, DEALER_MANAGER, EVM_STAFF");
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
            user.setRole(normalizeRole(request.getRole()));
            user.setCreatedDate(LocalDateTime.now());

            // Nếu là dealer role thì set dealer
            if (isDealerRole(request.getRole()) && request.getDealerId() != null) {
                Optional<Dealer> dealer = dealerRepository.findById(request.getDealerId());
                dealer.ifPresent(user::setDealer);
            }

            User savedUser = userRepository.save(user);
            log.info("Created user: {} with role: {}", savedUser.getUserName(), savedUser.getRole());

            // Tạo response -  KHÔNG bao gồm password
            CreateUserAccountResponse response = new CreateUserAccountResponse();
            response.setUserId(savedUser.getUserId());
            response.setUsername(savedUser.getUserName());
            response.setRole(savedUser.getRole());
            response.setFullName(savedUser.getFullName());
            response.setEmail(savedUser.getEmail());
            response.setPhone(savedUser.getPhone());  //  Thêm phone

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
        String r = normalizeRole(role);
        return "DEALER_STAFF".equals(r) || "DEALER_MANAGER".equals(r) || "EVM_STAFF".equals(r);
    }

    private boolean isDealerRole(String role) {
        String r = normalizeRole(role);
        return "DEALER_STAFF".equals(r) || "DEALER_MANAGER".equals(r);
    }

    private String normalizeRole(String role) {
        if (role == null) return null;
        String r = role.trim();
        if (r.startsWith("ROLE_")) r = r.substring(5);
        return r.toUpperCase();
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        // Simple email validation regex
        String emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
        return email.matches(emailRegex);
    }

    // ================== LẤY THÔNG TIN USER ACCOUNT ==================

    /**
     * Lấy thông tin user account theo ID
     */
    public UserAccountInfoResponse getUserAccountById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        return convertToInfoResponse(user);
    }

    /**
     * Lấy danh sách tất cả user accounts của dealer
     */
    public java.util.List<UserAccountInfoResponse> getDealerAccounts(Long dealerId) {
        // Verify dealer exists
        dealerRepository.findById(dealerId)
                .orElseThrow(() -> new IllegalArgumentException("Dealer not found with id: " + dealerId));
        
        java.util.List<User> users = userRepository.findByDealerDealerId(dealerId);
        
        return users.stream()
                .map(this::convertToInfoResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Lấy thông tin DEALER_MANAGER của dealer
     */
    public UserAccountInfoResponse getDealerManager(Long dealerId) {
        // Verify dealer exists
        dealerRepository.findById(dealerId)
                .orElseThrow(() -> new IllegalArgumentException("Dealer not found with id: " + dealerId));
        
        User manager = userRepository.findByDealerIdAndRole(dealerId, "DEALER_MANAGER")
                .orElseThrow(() -> new IllegalArgumentException("Dealer manager not found for dealer id: " + dealerId));
        
        return convertToInfoResponse(manager);
    }

    /**
     * Lấy danh sách DEALER_STAFF của dealer
     */
    public java.util.List<UserAccountInfoResponse> getDealerStaff(Long dealerId) {
        // Verify dealer exists
        dealerRepository.findById(dealerId)
                .orElseThrow(() -> new IllegalArgumentException("Dealer not found with id: " + dealerId));
        
        java.util.List<User> staff = userRepository.findByDealerDealerIdAndRole(dealerId, "DEALER_STAFF");
        
        return staff.stream()
                .map(this::convertToInfoResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Convert User entity to UserAccountInfoResponse (không bao gồm password)
     */
    private UserAccountInfoResponse convertToInfoResponse(User user) {
        UserAccountInfoResponse response = new UserAccountInfoResponse();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUserName());
        response.setRole(user.getRole());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setCreatedDate(user.getCreatedDate());
        response.setDateModified(user.getDateModified());
        
        if (user.getDealer() != null) {
            response.setDealerId(user.getDealer().getDealerId());
            response.setDealerName(user.getDealer().getDealerName());
        }
        
        return response;
    }
}