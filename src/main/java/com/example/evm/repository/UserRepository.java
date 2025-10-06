package com.example.evm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.evm.entity.user.User;

<<<<<<< HEAD
public interface UserRepository extends JpaRepository<User, Long> {
=======
public interface UserRepository extends JpaRepository<User, Integer> {
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
    Optional<User> findByUserName(String userName);
}
