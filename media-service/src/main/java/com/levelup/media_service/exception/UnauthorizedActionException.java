package com.levelup.media_service.exception;

public class UnauthorizedActionException extends RuntimeException{
    public UnauthorizedActionException(String message) {
        super(message);
    }
}
