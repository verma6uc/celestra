package com.celestra.dao;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import com.celestra.model.UserSession;

/**
 * Data Access Object (DAO) interface for UserSession entities.
 */
public interface UserSessionDao extends BaseDao<UserSession, Integer> {
    
    /**
     * Find a user session by its token.
     * 
     * @param sessionToken The session token to search for
     * @return An optional containing the user session if found, or empty if not found
     * @throws SQLException if a database access error occurs
     */
    Optional<UserSession> findBySessionToken(String sessionToken) throws SQLException;
    
    /**
     * Find all sessions for a specific user.
     * 
     * @param userId The user ID to search for
     * @return A list of user sessions for the specified user
     * @throws SQLException if a database access error occurs
     */
    List<UserSession> findByUserId(Integer userId) throws SQLException;
    
    /**
     * Find all active (non-expired) sessions for a specific user.
     * 
     * @param userId The user ID to search for
     * @return A list of active user sessions for the specified user
     * @throws SQLException if a database access error occurs
     */
    List<UserSession> findActiveByUserId(Integer userId) throws SQLException;
    
    /**
     * Find all sessions from a specific IP address.
     * 
     * @param ipAddress The IP address to search for
     * @return A list of user sessions from the specified IP address
     * @throws SQLException if a database access error occurs
     */
    List<UserSession> findByIpAddress(String ipAddress) throws SQLException;
    
    /**
     * Find all active (non-expired) sessions.
     * 
     * @return A list of all active user sessions
     * @throws SQLException if a database access error occurs
     */
    List<UserSession> findAllActive() throws SQLException;
    
    /**
     * Find all expired sessions.
     * 
     * @return A list of all expired user sessions
     * @throws SQLException if a database access error occurs
     */
    List<UserSession> findAllExpired() throws SQLException;
    
    /**
     * Update the expiration time of a session.
     * 
     * @param id The ID of the session to update
     * @param expiresAt The new expiration time
     * @return true if the update was successful, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean updateExpiresAt(Integer id, OffsetDateTime expiresAt) throws SQLException;
    
    /**
     * Delete all expired sessions.
     * 
     * @return The number of deleted sessions
     * @throws SQLException if a database access error occurs
     */
    int deleteExpired() throws SQLException;
    
    /**
     * Delete all sessions for a specific user.
     * 
     * @param userId The user ID to delete sessions for
     * @return The number of deleted sessions
     * @throws SQLException if a database access error occurs
     */
    int deleteByUserId(Integer userId) throws SQLException;
    
    /**
     * Delete all sessions except the current one for a specific user.
     * 
     * @param userId The user ID to delete sessions for
     * @param currentSessionId The ID of the session to keep
     * @return The number of deleted sessions
     * @throws SQLException if a database access error occurs
     */
    int deleteOtherSessionsForUser(Integer userId, Integer currentSessionId) throws SQLException;
}