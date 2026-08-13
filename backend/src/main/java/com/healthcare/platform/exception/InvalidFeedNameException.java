package com.healthcare.platform.exception;

public class InvalidFeedNameException extends RuntimeException {
    public InvalidFeedNameException(String message) {
        super(message);
    }
}