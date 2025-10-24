package com.example.evm.service.admin;

import com.example.evm.dto.admin.AdminCreateRequest;
import com.example.evm.dto.admin.AdminUserInfoResponse;
import com.example.evm.entity.user.User;
import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.repository.auth.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service chứa các nghiệp vụ quản trị người dùng (admin).
 * Tất cả các thao tác đều được thực hiện ở đây – controller chỉ gọi service.
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Tạo 1 admin mới.
     *
     * @param req DTO chứa username và password
     * @throws IllegalArgumentException nếu username đã tồn tại
     */
    public void createAdmin(AdminCreateRequest req) {
        // Kiểm tra trùng username
        if (userRepository.findByUserName(req.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Admin user already exists");
        }

        User admin = new User();
        admin.setUserName(req.getUsername());
        admin.setPassword(passwordEncoder.encode(req.getPassword()));
        admin.setRole("ADMIN");
        admin.setCreatedDate(LocalDateTime.now());
        admin.setDateModified(LocalDateTime.now());

        userRepository.save(admin);
    }

    /**
     * Lấy thông tin chi tiết của 1 người dùng theo username.
     *
     * @param username tên đăng nhập
     * @return DTO chứa các trường cần trả về cho front‑end
     * @throws ResourceNotFoundException nếu không tìm thấy người dùng
     */
    public AdminUserInfoResponse getUserInfo(String username) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found: " + username));

        return AdminUserInfoResponse.fromEntity(user);
    }

    /**
     * Trả về danh sách tất cả người dùng (admin sẽ dùng để hiển thị bảng).
     *
     * @return List<AdminUserInfoResponse>
     */
    public List<AdminUserInfoResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(AdminUserInfoResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
