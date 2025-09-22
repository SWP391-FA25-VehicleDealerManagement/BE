package com.example.evm.repository;

import com.example.evm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = "SELECT * FROM Users WHERE Username = :username", nativeQuery = true)
    Optional<User> findByUsername(@Param("username") String username);
}
