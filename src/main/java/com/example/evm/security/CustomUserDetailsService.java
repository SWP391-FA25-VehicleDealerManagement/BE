package com.example.evm.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.evm.entity.User;
import com.example.evm.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user by username: {}", username);
        
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> {
                    log.error("User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });
        
        log.info("Found user: {} with role: {}", user.getUserName(), user.getRole());
        
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getUserName())
                .password(user.getPassword())
                .authorities(getAuthorities(user.getRole()))
                .build();
        
        log.info("UserDetails created with authorities: {}", userDetails.getAuthorities());
        return userDetails;
    }

    private Collection<? extends GrantedAuthority> getAuthorities(String role) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (role != null && !role.trim().isEmpty()) {
            String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase();
            authorities.add(new SimpleGrantedAuthority(roleName));
            log.debug("Added authority: {}", roleName);
        } else {
            log.warn("Role is null or empty, no authorities added");
        }
        return authorities;
    }
}