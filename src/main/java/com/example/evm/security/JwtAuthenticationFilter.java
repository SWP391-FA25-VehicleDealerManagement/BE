
    package com.example.evm.security;

    import jakarta.servlet.*;
    import jakarta.servlet.http.*;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.security.core.userdetails.*;
    import org.springframework.stereotype.Component;
    import org.springframework.web.filter.OncePerRequestFilter;

    import java.io.IOException;

    @Slf4j
    @Component
    public class JwtAuthenticationFilter extends OncePerRequestFilter {

        private final JwtUtil jwtUtil;
        private final CustomUserDetailsService userDetailsService;

        public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
            this.jwtUtil = jwtUtil;
            this.userDetailsService = userDetailsService;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
        
            String requestPath = request.getRequestURI();
            String method = request.getMethod();
        
            log.info("🔐 Processing request: {} {}", method, requestPath);
        
            // Bỏ qua JWT filter cho các endpoint không cần authentication
            if (shouldSkipFilter(requestPath)) {
                log.info("⏭️ Skipping JWT filter for: {}", requestPath);
                filterChain.doFilter(request, response);
                return;
            }
        
            String authHeader = request.getHeader("Authorization");
        
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                log.info("🧾 Found Authorization header. Validating token...");
        
                try {
                    String username = jwtUtil.extractUsername(jwt);
                    if (username == null) {
                        log.warn("⚠️ Failed to extract username from token.");
                    } else if (SecurityContextHolder.getContext().getAuthentication() != null) {
                        log.info("🔒 SecurityContext already set. Skipping authentication for user: {}", username);
                    } else if (!jwtUtil.validateToken(jwt)) {
                        log.warn("❌ Token validation failed for user: {}", username);
                    } else {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.info("✅ Successfully authenticated user: {}", username);
                    }
                } catch (Exception e) {
                    log.error("🔥 JWT authentication error: {}", e.getMessage());
                }
            } else {
                log.info("🚫 No valid Authorization header found for: {}", requestPath);
            }
        
            filterChain.doFilter(request, response);
        }   
        private boolean shouldSkipFilter(String requestPath) {
            return requestPath.startsWith("/api/auth/") || 
                requestPath.startsWith("/api/test/") ||
                requestPath.equals("/") ||
                requestPath.startsWith("/static/") ||
                requestPath.startsWith("/public/");
        }
    }