package com.example.evm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.evm.dto.auth.ApiResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Xử lý các ngoại lệ chung và trả về cùng một cấu trúc JSON:
 * {
 *   "success": false,
 *   "message": "…",
 *   "data": null
 * }
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ----- 404 – resource not found -------------------------------------------------
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        ApiResponse<Void> body = new ApiResponse<>(false, ex.getMessage(), null);
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // ----- 400 – bad request 
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        ApiResponse<Void> body = new ApiResponse<>(false, ex.getMessage(), null);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // ----- 400 – login sai thông tin -----------------------------------------------
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        ApiResponse<Void> body = new ApiResponse<>(false, "Invalid username or password", null);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // ----- 401 – thiếu/không hợp lệ thông tin xác thực (ví dụ: token đã logout) ----
    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthCredentialsNotFound(AuthenticationCredentialsNotFoundException ex) {
        log.warn("Unauthorized: {}", ex.getMessage());
        ApiResponse<Void> body = new ApiResponse<>(false, "Unauthorized", null);
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    // ----- 401 – các lỗi xác thực khác ---------------------------------------------
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
        log.warn("Authentication error: {}", ex.getMessage());
        ApiResponse<Void> body = new ApiResponse<>(false, "Unauthorized", null);
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    // ----- 403 – đã đăng nhập nhưng không đủ quyền ---------------------------------
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        ApiResponse<Void> body = new ApiResponse<>(false, "Forbidden", null);
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    // ----- 500 – các lỗi không mong đợi --------------------------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAll(Exception ex) {
        log.error("Unexpected error", ex);
        ApiResponse<Void> body = new ApiResponse<>(false,
                "Internal server error. Please contact support.", null);
        body.setData(null);
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}