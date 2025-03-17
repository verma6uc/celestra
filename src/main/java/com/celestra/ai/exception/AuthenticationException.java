package com.celestra.ai.exception;

/**
 * Exception thrown when authentication fails with the AI service.
 */
public class AuthenticationException extends AIServiceException {
    
    /**
     * Create a new authentication exception.
     * 
     * @param message The error message
     */
    public AuthenticationException(String message) {
        super(message, 401);
    }
    
    /**
     * Create a new authentication exception.
     * 
     * @param message The error message
     * @param cause The cause of the exception
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause, 401);
    }
}