package com.celestra.auth.service;

import java.sql.SQLException;
import java.util.Map;

/**
 * Service for handling forgot password requests and password reset functionality.
 */
public interface ForgotPasswordService {
    
    /**
     * Initiate a forgot password request for a user.
     * 
     * @param email The email address of the user
     * @param ipAddress The IP address of the client
     * @param metadata Additional metadata about the request
     * @return true if the request was processed successfully, false otherwise
     * @throws SQLException if a database error occurs
     */
    boolean initiatePasswordReset(String email, String ipAddress, Map<String, String> metadata) 
            throws SQLException;
    
    /**
     * Validate a password reset token.
     * 
     * @param token The password reset token
     * @return true if the token is valid, false otherwise
     * @throws SQLException if a database error occurs
     */
    boolean validateResetToken(String token) throws SQLException;
    
    /**
     * Complete a password reset using a valid token.
     * 
     * @param token The password reset token
     * @param newPassword The new password
     * @param ipAddress The IP address of the client
     * @param metadata Additional metadata about the request
     * @return true if the password was reset successfully, false otherwise
     * @throws SQLException if a database error occurs
     */
    boolean resetPassword(String token, String newPassword, String ipAddress, Map<String, String> metadata) 
            throws SQLException;
    
    /**
     * Get the email address associated with a valid reset token.
     * 
     * @param token The password reset token
     * @return The email address, or null if the token is invalid
     * @throws SQLException if a database error occurs
     */
    String getEmailFromToken(String token) throws SQLException;
}