package com.celestra.ai.exception;

/**
 * Exception thrown when the AI service rate limit is exceeded.
 */
public class RateLimitException extends AIServiceException {
    
    private final long retryAfterMs;
    
    /**
     * Create a new rate limit exception.
     * 
     * @param message The error message
     * @param retryAfterMs The time to wait before retrying in milliseconds
     */
    public RateLimitException(String message, long retryAfterMs) {
        super(message, 429);
        this.retryAfterMs = retryAfterMs;
    }
    
    /**
     * Create a new rate limit exception.
     * 
     * @param message The error message
     * @param cause The cause of the exception
     * @param retryAfterMs The time to wait before retrying in milliseconds
     */
    public RateLimitException(String message, Throwable cause, long retryAfterMs) {
        super(message, cause, 429);
        this.retryAfterMs = retryAfterMs;
    }
    
    /**
     * Get the time to wait before retrying in milliseconds.
     * 
     * @return The time to wait before retrying in milliseconds
     */
    public long getRetryAfterMs() {
        return retryAfterMs;
    }
}