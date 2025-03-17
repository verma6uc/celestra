package com.celestra.ai.exception;

/**
 * Exception thrown when the AI service encounters a server error.
 */
public class ServerException extends AIServiceException {
    
    /**
     * Create a new server exception.
     * 
     * @param message The error message
     */
    public ServerException(String message) {
        super(message, 500);
    }
    
    /**
     * Create a new server exception.
     * 
     * @param message The error message
     * @param cause The cause of the exception
     */
    public ServerException(String message, Throwable cause) {
        super(message, cause, 500);
    }
    
    /**
     * Create a new server exception.
     * 
     * @param message The error message
     * @param statusCode The HTTP status code
     */
    public ServerException(String message, int statusCode) {
        super(message, statusCode);
    }
    
    /**
     * Create a new server exception.
     * 
     * @param message The error message
     * @param cause The cause of the exception
     * @param statusCode The HTTP status code
     */
    public ServerException(String message, Throwable cause, int statusCode) {
        super(message, cause, statusCode);
    }
}