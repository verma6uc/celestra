package com.celestra.dao;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import com.celestra.enums.UserRole;
import com.celestra.enums.UserStatus;
import com.celestra.model.User;

/**
 * Data Access Object (DAO) interface for User entities.
 */
public interface UserDao extends BaseDao<User, Integer> {
    
    /**
     * Find a user by email.
     * 
     * @param email The email to search for
     * @return An Optional containing the user if found, or empty if not found
     * @throws SQLException if a database access error occurs
     */
    Optional<User> findByEmail(String email) throws SQLException;
    
    /**
     * Find users by company ID.
     * 
     * @param companyId The company ID to search for
     * @return A list of users belonging to the specified company
     * @throws SQLException if a database access error occurs
     */
    List<User> findByCompanyId(Integer companyId) throws SQLException;
    
    /**
     * Find users by role.
     * 
     * @param role The user role to search for
     * @return A list of users with the specified role
     * @throws SQLException if a database access error occurs
     */
    List<User> findByRole(UserRole role) throws SQLException;
    
    /**
     * Find users by status.
     * 
     * @param status The user status to search for
     * @return A list of users with the specified status
     * @throws SQLException if a database access error occurs
     */
    List<User> findByStatus(UserStatus status) throws SQLException;
    
    /**
     * Find users by company ID and role.
     * 
     * @param companyId The company ID to search for
     * @param role The user role to search for
     * @return A list of users belonging to the specified company with the specified role
     * @throws SQLException if a database access error occurs
     */
    List<User> findByCompanyIdAndRole(Integer companyId, UserRole role) throws SQLException;
    
    /**
     * Update a user's password.
     * 
     * @param id The ID of the user to update
     * @param passwordHash The new password hash
     * @return true if the password was updated, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean updatePassword(Integer id, String passwordHash) throws SQLException;
    
    /**
     * Update a user's status.
     * 
     * @param id The ID of the user to update
     * @param status The new status
     * @return true if the status was updated, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean updateStatus(Integer id, UserStatus status) throws SQLException;
    
    /**
     * Authenticate a user with email and password hash.
     * 
     * @param email The user's email
     * @param passwordHash The password hash to verify
     * @return An Optional containing the user if authentication succeeds, or empty if it fails
     * @throws SQLException if a database access error occurs
     */
    Optional<User> authenticate(String email, String passwordHash) throws SQLException;
    
    /**
     * Authenticate a user with email only (for password reset flows).
     * This method only verifies that the user exists and is active.
     * 
     * @param email The user's email
     * @return An Optional containing the user if found and active, or empty if not found or not active
     * @throws SQLException if a database access error occurs
     */
    Optional<User> findActiveUserByEmail(String email) throws SQLException;
    
    /**
     * Check if a user is locked out.
     * 
     * @param userId The ID of the user to check
     * @return true if the user is locked out, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean isUserLockedOut(Integer userId) throws SQLException;
    
    /**
     * Check if a user has a specific role.
     * 
     * @param userId The ID of the user to check
     * @param role The role to check for
     * @return true if the user has the specified role, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean hasRole(Integer userId, UserRole role) throws SQLException;
    
    /**
     * Update a user's role.
     * 
     * @param id The ID of the user to update
     * @param role The new role
     * @return true if the role was updated, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean updateRole(Integer id, UserRole role) throws SQLException;
    
    /**
     * Count the number of active sessions for a user.
     * 
     * @param userId The ID of the user
     * @return The number of active sessions
     * @throws SQLException if a database access error occurs
     */
    int countActiveSessions(Integer userId) throws SQLException;
    
    /**
     * Invalidate all active sessions for a user.
     * 
     * @param userId The ID of the user
     * @return The number of sessions invalidated
     * @throws SQLException if a database access error occurs
     */
    int invalidateAllSessions(Integer userId) throws SQLException;
    
    /**
     * Find users with a specific role in a company.
     * 
     * @param companyId The company ID
     * @param role The role to search for
     * @param status The status to filter by (optional, can be null)
     * @return A list of users matching the criteria
     * @throws SQLException if a database access error occurs
     */
    List<User> findByCompanyIdAndRoleAndStatus(Integer companyId, UserRole role, UserStatus status) throws SQLException;
    
    /**
     * Find users whose email contains the given search term.
     * 
     * @param searchTerm The search term to look for in email addresses
     * @param limit The maximum number of results to return (optional, can be null)
     * @return A list of users matching the search term
     * @throws SQLException if a database access error occurs
     */
    List<User> findByEmailContaining(String searchTerm, Integer limit) throws SQLException;
    
    /**
     * Find users created after a specific date.
     * 
     * @param date The date to compare against
     * @return A list of users created after the specified date
     * @throws SQLException if a database access error occurs
     */
    List<User> findByCreatedAtAfter(Timestamp date) throws SQLException;
    
    /**
     * Check if a password hash matches any of the user's previous passwords.
     * 
     * @param userId The ID of the user
     * @param passwordHash The password hash to check
     * @param limit The number of previous passwords to check (optional, can be null)
     * @return true if the password hash matches any of the user's previous passwords, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean isPasswordPreviouslyUsed(Integer userId, String passwordHash, Integer limit) throws SQLException;
    
    /**
     * Add a password to the user's password history.
     * 
     * @param userId The ID of the user
     * @param passwordHash The password hash to add to history
     * @return true if the password was added to history, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean addPasswordToHistory(Integer userId, String passwordHash) throws SQLException;
}