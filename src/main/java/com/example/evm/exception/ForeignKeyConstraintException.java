package com.example.evm.exception;

/**
 * Exception dùng để báo khi xóa dữ liệu bị ràng buộc FK.
 */
public class ForeignKeyConstraintException extends RuntimeException {
    public ForeignKeyConstraintException(String message) {
        super(message);
    }

    public ForeignKeyConstraintException(String message, Throwable cause) {
        super(message, cause);
    }
}
