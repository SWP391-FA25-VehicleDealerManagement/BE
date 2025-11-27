package com.example.evm.repository.auth;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.evm.entity.user.User;

public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUserName(String userName);
    
    //  Tìm user theo email
    Optional<User> findByEmail(String email);
    
    //  Kiểm tra email đã tồn tại chưa
    boolean existsByEmail(String email);
    
    //  Đếm số DEALER_MANAGER của 1 dealer
    @Query("SELECT COUNT(u) FROM User u WHERE u.dealer.dealerId = :dealerId AND u.role = :role")
    long countByDealerIdAndRole(@Param("dealerId") Long dealerId, @Param("role") String role);
    
    //  Tìm DEALER_MANAGER của dealer (1 kết quả)
    @Query("SELECT u FROM User u WHERE u.dealer.dealerId = :dealerId AND u.role = :role")
    Optional<User> findByDealerIdAndRole(@Param("dealerId") Long dealerId, @Param("role") String role);
    
    //  Lấy danh sách tất cả users của dealer
    @Query("SELECT u FROM User u WHERE u.dealer.dealerId = :dealerId")
    List<User> findByDealerDealerId(@Param("dealerId") Long dealerId);
    
    //  Lấy danh sách users theo dealer và role (nhiều kết quả)
    @Query("SELECT u FROM User u WHERE u.dealer.dealerId = :dealerId AND u.role = :role")
    List<User> findByDealerDealerIdAndRole(@Param("dealerId") Long dealerId, @Param("role") String role);
     List<User> findByRole(String role);
}
