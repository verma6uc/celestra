package com.celestra.dao;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import com.celestra.model.PasswordResetToken;

/**
 * Data Access Object (DAO) interface for PasswordResetToken entities.
 */
public interface PasswordResetTokenDao extends BaseDao<PasswordResetToken, Integer> {
    
    /**
     * Find a password reset token by its token value.
     * 
     * @param token The token value to search for
     * @return An Optional containing the PasswordResetToken if found, or empty if not found
     * @throws SQLException if a database access error occurs
     */
    Optional<PasswordResetToken> findByToken(String token) throws SQLException;
    
    /**
     * Find all active (not expired and not used) tokens for a user.
     * 
     * @param userId The ID of the user
     * @return A list of active PasswordResetToken objects
     * @throws SQLException if a database access error occurs
     */
    List<PasswordResetToken> findActiveByUserId(Integer userId) throws SQLException;
    
    /**
     * Find all tokens for a user.
     * 
     * @param userId The ID of the user
     * @return A list of PasswordResetToken objects
     * @throws SQLException if a database access error occurs
     */
    List<PasswordResetToken> findByUserId(Integer userId) throws SQLException;
    
    /**
     * Mark a token as used.
     * 
     * @param id The ID of the token
     * @param usedAt The timestamp when the token was used
     * @return true if the token was updated, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean markAsUsed(Integer id, Timestamp usedAt) throws SQLException;
    
    /**
     * Mark a token as used by its token value.
     * 
     * @param token The token value
     * @param usedAt The timestamp when the token was used
     * @return true if the token was updated, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean markAsUsed(String token, Timestamp usedAt) throws SQLException;
    
    /**
     * Invalidate all active tokens for a user.
     * 
     * @param userId The ID of the user
     * @return The number of tokens invalidated
     * @throws SQLException if a database access error occurs
     */
    int invalidateAllForUser(Integer userId) throws SQLException;
    
    /**
     * Delete expired tokens.
     * 
     * @param olderThan The timestamp to compare against
     * @return The number of tokens deleted
     * @throws SQLException if a database access error occurs
     */
    int deleteExpiredTokens(Timestamp olderThan) throws SQLException;
}