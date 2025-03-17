package com.celestra.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.celestra.model.FailedLogin;

/**
 * Data Access Object (DAO) interface for FailedLogin entities.
 */
public interface FailedLoginDao extends BaseDao<FailedLogin, Integer> {
    
    /**
     * Find failed login attempts by username.
     * 
     * @param username The username to search for
     * @return A list of failed login attempts for the specified username
     * @throws SQLException if a database access error occurs
     */
    List<FailedLogin> findByUsername(String username) throws SQLException;
    
    /**
     * Find failed login attempts by email.
     * This is useful when a user ID is not available.
     * 
     * @param email The email address to search for
     * @return A list of failed login attempts for the specified email
     * @throws SQLException if a database access error occurs
     */
    List<FailedLogin> findByEmail(String email) throws SQLException;
    
    /**
     * Find failed login attempts by IP address.
     * 
     * @param ipAddress The IP address to search for
     * @return A list of failed login attempts from the specified IP address
     * @throws SQLException if a database access error occurs
     */
    List<FailedLogin> findByIpAddress(String ipAddress) throws SQLException;
    
    /**
     * Find failed login attempts by username and IP address.
     * 
     * @param username The username to search for
     * @param ipAddress The IP address to search for
     * @return A list of failed login attempts for the specified username and IP address
     * @throws SQLException if a database access error occurs
     */
    List<FailedLogin> findByUsernameAndIpAddress(String username, String ipAddress) throws SQLException;
    
    /**
     * Find recent failed login attempts by username within a specified time window.
     * 
     * @param username The username to search for
     * @param minutes The time window in minutes
     * @return A list of recent failed login attempts for the specified username
     * @throws SQLException if a database access error occurs
     */
    List<FailedLogin> findRecentByUsername(String username, int minutes) throws SQLException;
    
    /**
     * Find recent failed login attempts by email within a specified time window.
     * This is useful when a user ID is not available.
     * 
     * @param email The email address to search for
     * @param minutes The time window in minutes
     * @return A list of recent failed login attempts for the specified email
     * @throws SQLException if a database access error occurs
     */
    List<FailedLogin> findRecentByEmail(String email, int minutes) throws SQLException;
    
    /**
     * Count recent failed login attempts by username within a specified time window.
     * 
     * @param username The username to search for
     * @param minutes The time window in minutes
     * @return The number of recent failed login attempts for the specified username
     * @throws SQLException if a database access error occurs
     */
    int countRecentByUsername(String username, int minutes) throws SQLException;
    
    /**
     * Count recent failed login attempts by email within a specified time window.
     * This is useful when a user ID is not available.
     * 
     * @param email The email address to search for
     * @param minutes The time window in minutes
     * @return The number of recent failed login attempts for the specified email
     * @throws SQLException if a database access error occurs
     */
    int countRecentByEmail(String email, int minutes) throws SQLException;
    
    /**
     * Count recent failed login attempts by IP address within a specified time window.
     * 
     * @param ipAddress The IP address to search for
     * @param minutes The time window in minutes
     * @return The number of recent failed login attempts from the specified IP address
     * @throws SQLException if a database access error occurs
     */
    int countRecentByIpAddress(String ipAddress, int minutes) throws SQLException;
    
    /**
     * Delete old failed login attempts older than a specified time window.
     * 
     * @param days The time window in days
     * @return The number of deleted records
     * @throws SQLException if a database access error occurs
     */
    int deleteOlderThan(int days) throws SQLException;
}