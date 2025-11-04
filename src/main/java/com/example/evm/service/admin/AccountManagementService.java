package com.example.evm.service.admin;

import com.example.evm.dto.admin.UpdateUserRequest;
import com.example.evm.dto.admin.UpdateUserResponse;
import com.example.evm.entity.user.User;
import com.example.evm.repository.auth.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AccountManagementService {

    private final UserRepository userRepository;

    public AccountManagementService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UpdateUserResponse updateUser(Long userId, UpdateUserRequest request, Authentication auth) {
        User actor = getCurrentUser(auth);
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!canUpdate(actor, target)) {
            throw new AccessDeniedException("You do not have permission to update this user.");
        }

        if (request.fullName() != null && !request.fullName().isBlank()) {
            target.setFullName(request.fullName().trim());
        }
        if (request.phone() != null && !request.phone().isBlank()) {
            target.setPhone(request.phone().trim());
        }
        if (request.email() != null && !request.email().isBlank()) {
            target.setEmail(request.email().trim());
        }

        target.setDateModified(LocalDateTime.now());
        User saved = userRepository.save(target);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteUser(Long userId, Authentication auth) {
        User actor = getCurrentUser(auth);
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!canDelete(actor, target)) {
            throw new AccessDeniedException("You do not have permission to delete this user.");
        }

        userRepository.delete(target);
    }

    // ===================================================================
    // PHÂN QUYỀN
    // ===================================================================

    private boolean canUpdate(User actor, User target) {
        return switch (actor.getRole()) {
            case "ADMIN" -> "EVM_STAFF".equals(target.getRole());
            case "EVM_STAFF" -> "DEALER_MANAGER".equals(target.getRole());
            case "DEALER_MANAGER" -> "DEALER_STAFF".equals(target.getRole())
                    && isSameDealer(actor, target);
            default -> false;
        };
    }

    private boolean canDelete(User actor, User target) {
        return canUpdate(actor, target);
    }

    private boolean isSameDealer(User actor, User target) {
        return actor.getDealer() != null
                && target.getDealer() != null
                && actor.getDealer().getDealerId().equals(target.getDealer().getDealerId());
    }

    // ===================================================================
    // HELPER
    // ===================================================================

    private User getCurrentUser(Authentication auth) {
        String username = auth.getName();
        return userRepository.findByUserName(username)
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
    }

    private UpdateUserResponse mapToResponse(User u) {
        UpdateUserResponse res = new UpdateUserResponse();
        res.setUserId(u.getUserId());
        res.setUserName(u.getUserName());
        res.setFullName(u.getFullName());
        res.setEmail(u.getEmail());
        res.setPhone(u.getPhone());
        res.setRole(u.getRole());
        res.setDealerId(u.getDealer() != null ? u.getDealer().getDealerId() : null);
        res.setCreatedDate(u.getCreatedDate());
        res.setDateModified(u.getDateModified());
        return res;
    }
}