package com.example.evm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.evm.entity.User;
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    @Query(value = "SELECT * FROM [User] WHERE userName = :userName", nativeQuery = true)
    Optional<User> findByUserName(String userName);
}
