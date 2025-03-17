package com.celestra.dao;

import java.sql.SQLException;
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
}