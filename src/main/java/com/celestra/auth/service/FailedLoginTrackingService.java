package com.celestra.auth.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.celestra.model.FailedLogin;
import com.celestra.model.User;

/**
 * Service for tracking and managing failed login attempts.
 */
public interface FailedLoginTrackingService {
    
    /**
     * Record a failed login attempt.
     * 
     * @param email The email address used in the login attempt
     * @param ipAddress The IP address of the client
     * @param failureReason The reason for the login failure
     * @param metadata Additional metadata about the login attempt
     * @return The created FailedLogin record
     * @throws SQLException if a database error occurs
     */
    FailedLogin recordFailedLogin(String email, String ipAddress, String failureReason, Map<String, String> metadata) 
            throws SQLException;
    
    /**
     * Record a failed login attempt with a known user.
     * 
     * @param user The user who attempted to log in
     * @param ipAddress The IP address of the client
     * @param failureReason The reason for the login failure
     * @param metadata Additional metadata about the login attempt
     * @return The created FailedLogin record
     * @throws SQLException if a database error occurs
     */
    FailedLogin recordFailedLogin(User user, String ipAddress, String failureReason, Map<String, String> metadata) 
            throws SQLException;
    
    /**
     * Get the number of recent failed login attempts for a specific email.
     * 
     * @param email The email address to check
     * @param windowMinutes The time window in minutes
     * @return The number of failed login attempts
     * @throws SQLException if a database error occurs
     */
    int getRecentFailedLoginCount(String email, int windowMinutes) throws SQLException;
    
    /**
     * Get the number of recent failed login attempts from a specific IP address.
     * 
     * @param ipAddress The IP address to check
     * @param windowMinutes The time window in minutes
     * @return The number of failed login attempts
     * @throws SQLException if a database error occurs
     */
    int getRecentFailedLoginCountByIp(String ipAddress, int windowMinutes) throws SQLException;
    
    /**
     * Get recent failed login attempts for a specific email.
     * 
     * @param email The email address to check
     * @param windowMinutes The time window in minutes
     * @return A list of recent failed login attempts
     * @throws SQLException if a database error occurs
     */
    List<FailedLogin> getRecentFailedLogins(String email, int windowMinutes) throws SQLException;
    
    /**
     * Get recent failed login attempts from a specific IP address.
     * 
     * @param ipAddress The IP address to check
     * @param windowMinutes The time window in minutes
     * @return A list of recent failed login attempts
     * @throws SQLException if a database error occurs
     */
    List<FailedLogin> getRecentFailedLoginsByIp(String ipAddress, int windowMinutes) throws SQLException;
    
    /**
     * Check if the number of failed login attempts for an email exceeds the threshold.
     * 
     * @param email The email address to check
     * @return true if the threshold is exceeded, false otherwise
     * @throws SQLException if a database error occurs
     */
    boolean isFailedLoginThresholdExceeded(String email) throws SQLException;
    
    /**
     * Check if the number of failed login attempts from an IP address exceeds the threshold.
     * 
     * @param ipAddress The IP address to check
     * @return true if the threshold is exceeded, false otherwise
     * @throws SQLException if a database error occurs
     */
    boolean isFailedLoginThresholdExceededByIp(String ipAddress) throws SQLException;
    
    /**
     * Clean up old failed login records.
     * 
     * @param olderThanDays The age in days of records to delete
     * @return The number of records deleted
     * @throws SQLException if a database error occurs
     */
    int cleanupOldRecords(int olderThanDays) throws SQLException;
}