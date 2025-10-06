package com.example.evm.security;

import java.io.IOException;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
<<<<<<< HEAD
=======
import org.springframework.security.core.userdetails.UserDetails;
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    // Hợp nhất constructors: Giữ lại constructor đầy đủ và loại bỏ các khối code thừa
    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                   CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
<<<<<<< HEAD
        this.userDetailsService = userDetailsService; // retained for potential future use
=======
        this.userDetailsService = userDetailsService;
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();
        log.info(" JWT filter - {} {}", method, path);

        // Skip auth‑free endpoints
        if (shouldSkip(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.debug("Found JWT token, validating...");

            // Loại bỏ khối try/if bị cắt ngang và chỉ giữ lại khối try/catch hoàn chỉnh
            try {
                String username = jwtUtil.extractUsername(token);
                
                // Lấy vai trò (role) từ JWT Claim
                String role = jwtUtil.getRole(token); 
                
                if (username != null 
                    && SecurityContextHolder.getContext().getAuthentication() == null
                    && jwtUtil.validateToken(token) 
                    && role != null) {

                    String normalizedRole = role.toUpperCase().replace(" ", "_");
                    
                    // 2. Tạo GrantedAuthorities: Sử dụng ROLE_ chuẩn của Spring Security.
                    List<GrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + normalizedRole)
                    );

                    // 3. Thiết lập Authentication trực tiếp bằng thông tin từ Token
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info(" Authenticated user {} with role {}", username, normalizedRole);
                }
            } catch (Exception e) {
                log.error("Jwt authentication error: {}", e.getMessage());
            }
        } else {
            log.debug("No JWT token supplied for {}", path);
        }
        
        filterChain.doFilter(request, response);
    }

    private boolean shouldSkip(String path) {
<<<<<<< HEAD
        // Only skip login and public assets; require JWT for /api/auth/me
        return path.equals("/api/auth/login") ||
=======
        return path.startsWith("/api/auth/") ||
>>>>>>> 54ac894e9c24c5857ad6736606c5e3f39b001e8d
               path.startsWith("/api/test/") ||
               path.equals("/") ||
               path.startsWith("/static/") ||
               path.startsWith("/public/");
    }
}