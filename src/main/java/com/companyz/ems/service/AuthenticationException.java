package com.companyz.ems.service;

/** Login failed. The message is safe to show to the user. */
public class AuthenticationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message);
    }
}
