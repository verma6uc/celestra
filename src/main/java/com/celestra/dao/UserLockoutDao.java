package com.celestra.dao;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import com.celestra.model.UserLockout;

/**
 * Data Access Object (DAO) interface for UserLockout entities.
 */
public interface UserLockoutDao extends BaseDao<UserLockout, Integer> {
    
    /**
     * Find the active lockout for a specific user.
     * 
     * @param userId The user ID to search for
     * @return An optional containing the active lockout if found, or empty if not found
     * @throws SQLException if a database access error occurs
     */
    Optional<UserLockout> findActiveByUserId(Integer userId) throws SQLException;
    
    /**
     * Find all lockouts for a specific user.
     * 
     * @param userId The user ID to search for
     * @return A list of lockouts for the specified user
     * @throws SQLException if a database access error occurs
     */
    List<UserLockout> findByUserId(Integer userId) throws SQLException;
    
    /**
     * Find all active lockouts.
     * 
     * @return A list of all active lockouts
     * @throws SQLException if a database access error occurs
     */
    List<UserLockout> findAllActive() throws SQLException;
    
    /**
     * Find all expired lockouts.
     * 
     * @return A list of all expired lockouts
     * @throws SQLException if a database access error occurs
     */
    List<UserLockout> findAllExpired() throws SQLException;
    
    /**
     * Find all permanent lockouts.
     * 
     * @return A list of all permanent lockouts
     * @throws SQLException if a database access error occurs
     */
    List<UserLockout> findAllPermanent() throws SQLException;
    
    /**
     * Find all temporary lockouts.
     * 
     * @return A list of all temporary lockouts
     * @throws SQLException if a database access error occurs
     */
    List<UserLockout> findAllTemporary() throws SQLException;
    
    /**
     * Update the lockout end time.
     * 
     * @param id The ID of the lockout to update
     * @param lockoutEnd The new lockout end time (null for permanent lockout)
     * @return true if the update was successful, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean updateLockoutEnd(Integer id, OffsetDateTime lockoutEnd) throws SQLException;
    
    /**
     * Update the failed attempts count.
     * 
     * @param id The ID of the lockout to update
     * @param failedAttempts The new failed attempts count
     * @return true if the update was successful, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean updateFailedAttempts(Integer id, Integer failedAttempts) throws SQLException;
    
    /**
     * Delete all expired lockouts.
     * 
     * @return The number of deleted lockouts
     * @throws SQLException if a database access error occurs
     */
    int deleteExpired() throws SQLException;
    
    /**
     * Delete all lockouts for a specific user.
     * 
     * @param userId The user ID to delete lockouts for
     * @return The number of deleted lockouts
     * @throws SQLException if a database access error occurs
     */
    int deleteByUserId(Integer userId) throws SQLException;
}