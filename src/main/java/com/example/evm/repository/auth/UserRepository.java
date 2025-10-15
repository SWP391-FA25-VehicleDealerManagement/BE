package com.example.evm.repository.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.evm.entity.user.User;

public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUserName(String userName);
    
    // ✅ Đếm số DEALER_MANAGER của 1 dealer
    @Query("SELECT COUNT(u) FROM User u WHERE u.dealer.dealerId = :dealerId AND u.role = :role")
    long countByDealerIdAndRole(@Param("dealerId") Long dealerId, @Param("role") String role);
    
    // ✅ Tìm DEALER_MANAGER của dealer
    @Query("SELECT u FROM User u WHERE u.dealer.dealerId = :dealerId AND u.role = :role")
    Optional<User> findByDealerIdAndRole(@Param("dealerId") Long dealerId, @Param("role") String role);
}