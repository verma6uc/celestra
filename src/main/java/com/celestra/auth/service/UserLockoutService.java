package com.celestra.auth.service;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import com.celestra.model.UserLockout;

/**
 * Service for managing user account lockouts.
 * This service handles the creation, management, and resolution of account lockouts
 * that occur due to security policy violations such as too many failed login attempts.
 */
public interface UserLockoutService {
    
    /**
     * Lock a user account due to security concerns.
     * 
     * @param userId The ID of the user to lock
     * @param failedAttempts The number of failed login attempts
     * @param reason The reason for the lockout
     * @param ipAddress The IP address that triggered the lockout
     * @return The created UserLockout record
     * @throws SQLException if a database error occurs
     */
    UserLockout lockAccount(Integer userId, int failedAttempts, String reason, String ipAddress) 
            throws SQLException;
    
    /**
     * Lock a user account by email.
     * 
     * @param email The email of the user to lock
     * @param failedAttempts The number of failed login attempts
     * @param reason The reason for the lockout
     * @param ipAddress The IP address that triggered the lockout
     * @return The created UserLockout record, or null if the user was not found
     * @throws SQLException if a database error occurs
     */
    UserLockout lockAccountByEmail(String email, int failedAttempts, String reason, String ipAddress) 
            throws SQLException;
    
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
     * Get the active lockout for a user if one exists.
     * 
     * @param userId The ID of the user
     * @return An Optional containing the active lockout if found, or empty if not found
     * @throws SQLException if a database error occurs
     */
    Optional<UserLockout> getActiveLockout(Integer userId) throws SQLException;
    
    /**
     * Get the active lockout for a user by email if one exists.
     * 
     * @param email The email of the user
     * @return An Optional containing the active lockout if found, or empty if not found
     * @throws SQLException if a database error occurs
     */
    Optional<UserLockout> getActiveLockoutByEmail(String email) throws SQLException;
    
    /**
     * Get all lockouts for a user.
     * 
     * @param userId The ID of the user
     * @return A list of all lockouts for the user
     * @throws SQLException if a database error occurs
     */
    List<UserLockout> getLockoutHistory(Integer userId) throws SQLException;
    
    /**
     * Get all lockouts for a user by email.
     * 
     * @param email The email of the user
     * @return A list of all lockouts for the user
     * @throws SQLException if a database error occurs
     */
    List<UserLockout> getLockoutHistoryByEmail(String email) throws SQLException;
    
    /**
     * Unlock a user account manually.
     * 
     * @param userId The ID of the user to unlock
     * @param reason The reason for the unlock
     * @param adminUserId The ID of the admin user who performed the unlock
     * @param ipAddress The IP address of the admin user
     * @return true if the account was unlocked, false if it wasn't locked or the unlock failed
     * @throws SQLException if a database error occurs
     */
    boolean unlockAccount(Integer userId, String reason, Integer adminUserId, String ipAddress) 
            throws SQLException;
    
    /**
     * Unlock a user account by email manually.
     * 
     * @param email The email of the user to unlock
     * @param reason The reason for the unlock
     * @param adminUserId The ID of the admin user who performed the unlock
     * @param ipAddress The IP address of the admin user
     * @return true if the account was unlocked, false if it wasn't locked or the unlock failed
     * @throws SQLException if a database error occurs
     */
    boolean unlockAccountByEmail(String email, String reason, Integer adminUserId, String ipAddress) 
            throws SQLException;
    
    /**
     * Extend the lockout period for a user.
     * 
     * @param userId The ID of the user
     * @param additionalMinutes The number of additional minutes to add to the lockout
     * @param reason The reason for extending the lockout
     * @param adminUserId The ID of the admin user who performed the extension
     * @param ipAddress The IP address of the admin user
     * @return true if the lockout was extended, false if it wasn't locked or the extension failed
     * @throws SQLException if a database error occurs
     */
    boolean extendLockout(Integer userId, int additionalMinutes, String reason, Integer adminUserId, String ipAddress) 
            throws SQLException;
    
    /**
     * Make a temporary lockout permanent.
     * 
     * @param userId The ID of the user
     * @param reason The reason for making the lockout permanent
     * @param adminUserId The ID of the admin user who performed the action
     * @param ipAddress The IP address of the admin user
     * @return true if the lockout was made permanent, false if it wasn't locked or the action failed
     * @throws SQLException if a database error occurs
     */
    boolean makeLockoutPermanent(Integer userId, String reason, Integer adminUserId, String ipAddress) 
            throws SQLException;
    
    /**
     * Get all currently active lockouts.
     * 
     * @return A list of all active lockouts
     * @throws SQLException if a database error occurs
     */
    List<UserLockout> getAllActiveLockouts() throws SQLException;
    
    /**
     * Get all permanent lockouts.
     * 
     * @return A list of all permanent lockouts
     * @throws SQLException if a database error occurs
     */
    List<UserLockout> getAllPermanentLockouts() throws SQLException;
    
    /**
     * Get all temporary lockouts.
     * 
     * @return A list of all temporary lockouts
     * @throws SQLException if a database error occurs
     */
    List<UserLockout> getAllTemporaryLockouts() throws SQLException;
    
    /**
     * Clean up expired lockouts.
     * 
     * @return The number of deleted lockouts
     * @throws SQLException if a database error occurs
     */
    int cleanupExpiredLockouts() throws SQLException;
    
    /**
     * Check if a lockout should be permanent based on the user's lockout history.
     * 
     * @param userId The ID of the user
     * @return true if the next lockout should be permanent, false otherwise
     * @throws SQLException if a database error occurs
     */
    boolean shouldLockoutBePermanent(Integer userId) throws SQLException;
    
    /**
     * Get the number of temporary lockouts a user has had in the past.
     * 
     * @param userId The ID of the user
     * @return The number of temporary lockouts
     * @throws SQLException if a database error occurs
     */
    int getTemporaryLockoutCount(Integer userId) throws SQLException;
}