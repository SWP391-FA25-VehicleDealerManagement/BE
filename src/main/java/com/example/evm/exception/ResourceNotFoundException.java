package com.example.evm.exception;

/**
 * Thrown khi một tài nguyên (user, dealer, …) không tồn tại trong DB.
 * Kế thừa RuntimeException để Spring tự chuyển sang HTTP 404 khi
 * được xử lý bởi {@link GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException() {
        super();
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
