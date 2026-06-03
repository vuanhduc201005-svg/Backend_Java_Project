package com.dducwsjvbe.backendjava.exception;


public class JwtAuthenticationException extends RuntimeException {
    public JwtAuthenticationException(String message) {
        super(message);
    }
}
