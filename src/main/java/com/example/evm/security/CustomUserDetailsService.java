package com.example.evm.security;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.evm.entity.user.User;
import com.example.evm.repository.auth.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository repo) {
        this.userRepository = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user by username: {}", username);
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> {
                    log.error("User not found: {}", username);
                    return new UsernameNotFoundException("User not found");
                });
        
        // Chuẩn hóa role: viết hoa và thay thế khoảng trắng
        String normalizedRole = user.getRole().toUpperCase().replace("ROLE_", "").replace(" ", "_");
        
        //  KHÔNG thêm ROLE_ prefix vì controller dùng hasAnyAuthority()
        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority(normalizedRole)
        );

        log.info("User {} loaded with authority: {}", username, normalizedRole);

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUserName())
                .password(user.getPassword())
                .authorities(authorities)
                .accountLocked(false)
                .accountExpired(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}