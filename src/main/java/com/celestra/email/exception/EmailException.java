package com.celestra.email.exception;

/**
 * Exception thrown when an error occurs in the email service.
 */
public class EmailException extends Exception {
    
    /**
     * Create a new email exception.
     * 
     * @param message The error message
     */
    public EmailException(String message) {
        super(message);
    }
    
    /**
     * Create a new email exception.
     * 
     * @param message The error message
     * @param cause The cause of the exception
     */
    public EmailException(String message, Throwable cause) {
        super(message, cause);
    }
}