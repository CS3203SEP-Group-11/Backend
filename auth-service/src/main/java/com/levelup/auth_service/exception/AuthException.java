package com.levelup.auth_service.exception;

public class AuthException extends RuntimeException {
    public AuthException(String message, Exception ex) {
        super(message);
    }
}
