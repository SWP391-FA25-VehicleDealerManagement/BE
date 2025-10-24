package com.example.evm.service.auth;

import com.example.evm.dto.auth.ChangePasswordRequest;

import com.example.evm.dto.auth.UpdateProfileRequest;
import com.example.evm.dto.auth.UserInfo;
import com.example.evm.entity.user.User;
import com.example.evm.exception.ResourceNotFoundException;
import com.example.evm.mapper.UserMapper;
import com.example.evm.repository.auth.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserMapper userMapper;

	public void changePassword(String username, ChangePasswordRequest req) {
		User user = userRepository.findByUserName(username)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
			throw new BadCredentialsException("Old password is incorrect");
		}
		user.setPassword(passwordEncoder.encode(req.getNewPassword()));
		user.setDateModified(LocalDateTime.now());
		userRepository.save(user);
	}

	public UserInfo updateProfile(String username, UpdateProfileRequest req) {
		User user = userRepository.findByUserName(username)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		if (req.getFullName() != null) {
			user.setFullName(req.getFullName());
		}
		if (req.getPhone() != null) {
			user.setPhone(req.getPhone());
		}
		if (req.getEmail() != null) {
			user.setEmail(req.getEmail());
		}
		user.setDateModified(LocalDateTime.now());
		userRepository.save(user);
		return userMapper.toUserInfo(user);
	}


	
	

	
}


