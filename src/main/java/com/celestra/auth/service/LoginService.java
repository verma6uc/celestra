package com.celestra.auth.service;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import com.celestra.model.User;
import com.celestra.model.UserSession;

/**
 * Service for handling user authentication and login-related operations.
 */
public interface LoginService {
    
    /**
     * Authenticate a user with email and password.
     * 
     * @param email The user's email
     * @param password The user's password
     * @param ipAddress The IP address of the client
     * @param metadata Additional metadata about the login attempt
     * @return An Optional containing the authenticated User if successful, or empty if authentication failed
     * @throws SQLException if a database error occurs
     */
    Optional<User> authenticate(String email, String password, String ipAddress, Map<String, String> metadata) 
            throws SQLException;
    
    /**
     * Create a new session for an authenticated user.
     * 
     * @param userId The ID of the authenticated user
     * @param ipAddress The IP address of the client
     * @param userAgent The user agent of the client
     * @param metadata Additional metadata about the session
     * @return The created UserSession
     * @throws SQLException if a database error occurs
     */
    UserSession createSession(Integer userId, String ipAddress, String userAgent, Map<String, String> metadata) 
            throws SQLException;
    
    /**
     * Validate a session token.
     * 
     * @param sessionToken The session token to validate
     * @return An Optional containing the UserSession if valid, or empty if invalid
     * @throws SQLException if a database error occurs
     */
    Optional<UserSession> validateSession(String sessionToken) throws SQLException;
    
    /**
     * End a user session.
     * 
     * @param sessionToken The session token to end
     * @param reason The reason for ending the session
     * @return true if the session was ended successfully, false otherwise
     * @throws SQLException if a database error occurs
     */
    boolean endSession(String sessionToken, String reason) throws SQLException;
    
    /**
     * End all sessions for a user.
     * 
     * @param userId The ID of the user
     * @param reason The reason for ending the sessions
     * @return The number of sessions ended
     * @throws SQLException if a database error occurs
     */
    int endAllSessions(Integer userId, String reason) throws SQLException;
    
    /**
     * Check if a user account is locked.
     * 
     * @param userId The ID of the user
     * @return true if the account is locked, false otherwise
     * @throws SQLException if a database error occurs
     */
    boolean isAccountLocked(Integer userId) throws SQLException;
    
    /**
     * Check if a user account is locked by email.
     * 
     * @param email The email of the user
     * @return true if the account is locked, false otherwise
     * @throws SQLException if a database error occurs
     */
    boolean isAccountLocked(String email) throws SQLException;
    
    /**
     * Record a failed login attempt.
     * 
     * @param email The email used in the attempt
     * @param ipAddress The IP address of the client
     * @param reason The reason for the failure
     * @param metadata Additional metadata about the attempt
     * @throws SQLException if a database error occurs
     */
    void recordFailedLogin(String email, String ipAddress, String reason, Map<String, String> metadata) 
            throws SQLException;
    
    /**
     * Get the number of recent failed login attempts for a user.
     * 
     * @param userId The ID of the user
     * @param minutes The time window in minutes
     * @return The number of failed attempts within the time window
     * @throws SQLException if a database error occurs
     */
    int getRecentFailedLoginCount(Integer userId, int minutes) throws SQLException;
    
    /**
     * Get the number of recent failed login attempts for an IP address.
     * 
     * @param ipAddress The IP address
     * @param minutes The time window in minutes
     * @return The number of failed attempts within the time window
     * @throws SQLException if a database error occurs
     */
    int getRecentFailedLoginCount(String ipAddress, int minutes) throws SQLException;
}