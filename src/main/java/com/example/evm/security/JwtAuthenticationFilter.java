package com.example.evm.security;

import java.io.IOException;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();
        log.info("JWT filter - {} {}", method, path);

        // Skip auth-free endpoints
        if (shouldSkip(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.debug("Found JWT token, validating...");

            try {
                String username = jwtUtil.extractUsername(token);
                
                // Lấy vai trò (role) từ JWT Claim
                String role = jwtUtil.getRole(token); 
                
                if (username != null 
                    && SecurityContextHolder.getContext().getAuthentication() == null
                    && jwtUtil.validateToken(token) 
                    && role != null) {

                    // Chuẩn hóa role
                    String normalizedRole = role.toUpperCase().replace(" ", "_");
                    
                    //  KHÔNG thêm ROLE_ prefix vì controller dùng hasAnyAuthority()
                    List<GrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority(normalizedRole)
                    );

                    // Thiết lập Authentication
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("Authenticated user {} with authority: {}", username, normalizedRole);
                }
            } catch (Exception e) {
                log.error("JWT authentication error: {}", e.getMessage());
            }
        } else {
            log.debug("No JWT token supplied for {}", path);
        }
        
        filterChain.doFilter(request, response);
    }

    private boolean shouldSkip(String path) {
        return path.equals("/api/auth/login") ||
               path.startsWith("/api/test/") ||
               path.equals("/") ||
               path.startsWith("/static/") ||
               path.startsWith("/public/");
    }
}