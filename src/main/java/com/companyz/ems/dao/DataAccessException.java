package com.companyz.ems.dao;

/**
 * A database call failed. DAOs wrap {@link java.sql.SQLException} in this unchecked
 * exception so services and screens never import {@code java.sql}.
 * The message is safe to show to a user; the original error is kept as the cause.
 */
public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
