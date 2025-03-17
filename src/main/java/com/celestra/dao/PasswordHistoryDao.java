package com.celestra.dao;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.celestra.model.PasswordHistory;

/**
 * Data Access Object (DAO) interface for PasswordHistory entities.
 */
public interface PasswordHistoryDao extends BaseDao<PasswordHistory, Integer> {
    
    /**
     * Find all password history entries for a specific user.
     * 
     * @param userId The user ID to search for
     * @return A list of password history entries for the specified user
     * @throws SQLException if a database access error occurs
     */
    List<PasswordHistory> findByUserId(Integer userId) throws SQLException;
    
    /**
     * Find all password history entries for a specific user, ordered by creation date.
     * 
     * @param userId The user ID to search for
     * @param limit The maximum number of entries to return
     * @return A list of password history entries for the specified user
     * @throws SQLException if a database access error occurs
     */
    List<PasswordHistory> findRecentByUserId(Integer userId, int limit) throws SQLException;
    
    /**
     * Check if a password hash exists in the user's password history.
     * 
     * @param userId The user ID to check
     * @param passwordHash The password hash to check
     * @return true if the password hash exists in the user's history, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean existsByUserIdAndPasswordHash(Integer userId, String passwordHash) throws SQLException;
    
    /**
     * Delete all password history entries for a specific user.
     * 
     * @param userId The user ID to delete entries for
     * @return The number of deleted entries
     * @throws SQLException if a database access error occurs
     */
    int deleteByUserId(Integer userId) throws SQLException;
    
    /**
     * Delete password history entries older than a specified date.
     * 
     * @param olderThan The cutoff date
     * @return The number of deleted entries
     * @throws SQLException if a database access error occurs
     */
    int deleteOlderThan(Timestamp olderThan) throws SQLException;
    
    /**
     * Delete the oldest password history entries for a user, keeping only the most recent ones.
     * 
     * @param userId The user ID to delete entries for
     * @param keepCount The number of recent entries to keep
     * @return The number of deleted entries
     * @throws SQLException if a database access error occurs
     */
    int deleteOldestByUserId(Integer userId, int keepCount) throws SQLException;
}