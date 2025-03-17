package com.celestra.ai.exception;

/**
 * Exception thrown when the request to the AI service is invalid.
 */
public class InvalidRequestException extends AIServiceException {
    
    /**
     * Create a new invalid request exception.
     * 
     * @param message The error message
     */
    public InvalidRequestException(String message) {
        super(message, 400);
    }
    
    /**
     * Create a new invalid request exception.
     * 
     * @param message The error message
     * @param cause The cause of the exception
     */
    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause, 400);
    }
}