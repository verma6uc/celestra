package com.celestra.ai.exception;

/**
 * Base exception class for AI service errors.
 */
public class AIServiceException extends Exception {
    
    private final int statusCode;
    
    /**
     * Create a new AI service exception.
     * 
     * @param message The error message
     */
    public AIServiceException(String message) {
        super(message);
        this.statusCode = 0;
    }
    
    /**
     * Create a new AI service exception.
     * 
     * @param message The error message
     * @param cause The cause of the exception
     */
    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
    }
    
    /**
     * Create a new AI service exception.
     * 
     * @param message The error message
     * @param statusCode The HTTP status code
     */
    public AIServiceException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    /**
     * Create a new AI service exception.
     * 
     * @param message The error message
     * @param cause The cause of the exception
     * @param statusCode The HTTP status code
     */
    public AIServiceException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }
    
    /**
     * Get the HTTP status code.
     * 
     * @return The HTTP status code
     */
    public int getStatusCode() {
        return statusCode;
    }
}