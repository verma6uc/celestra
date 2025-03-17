package com.celestra.auth.service;

import java.sql.SQLException;
import java.util.Map;

import com.celestra.enums.UserRole;
import com.celestra.model.User;

/**
 * Service for handling user registration operations.
 * This service encapsulates the business logic for user registration,
 * including validation, password complexity enforcement, and different
 * registration flows.
 */
public interface RegistrationService {
    
    /**
     * Register a new user directly (without invitation).
     * This method is typically used for initial admin setup or when
     * self-registration is allowed.
     * 
     * @param email The user's email address
     * @param name The user's full name
     * @param password The user's plain text password (will be hashed)
     * @param role The user's role
     * @param companyId The ID of the company to associate the user with (null for super admins)
     * @param ipAddress The IP address of the registration request
     * @param metadata Additional metadata for the registration (optional)
     * @return The newly created user
     * @throws SQLException if a database error occurs
     * @throws IllegalArgumentException if the input is invalid
     * @throws SecurityException if the registration violates security policies
     */
    User registerUser(String email, String name, String password, UserRole role, 
                     Integer companyId, String ipAddress, Map<String, String> metadata) 
                     throws SQLException, IllegalArgumentException, SecurityException;
    
    /**
     * Register a new user via invitation.
     * This method is used when a user is invited to join the system.
     * 
     * @param invitationToken The invitation token
     * @param name The user's full name
     * @param password The user's plain text password (will be hashed)
     * @param ipAddress The IP address of the registration request
     * @param metadata Additional metadata for the registration (optional)
     * @return The newly created user
     * @throws SQLException if a database error occurs
     * @throws IllegalArgumentException if the input is invalid
     * @throws SecurityException if the registration violates security policies
     */
    User registerUserViaInvitation(String invitationToken, String name, String password,
                                  String ipAddress, Map<String, String> metadata)
                                  throws SQLException, IllegalArgumentException, SecurityException;
    
    /**
     * Validate a user's email address.
     * 
     * @param email The email address to validate
     * @return true if the email is valid, false otherwise
     */
    boolean validateEmail(String email);
    
    /**
     * Validate a user's password against complexity requirements.
     * 
     * @param password The password to validate
     * @return A map of validation results, with keys indicating the validation rule
     *         and values indicating whether the rule passed (true) or failed (false)
     */
    Map<String, Boolean> validatePassword(String password);
    
    /**
     * Check if an email address is already in use.
     * 
     * @param email The email address to check
     * @return true if the email is already in use, false otherwise
     * @throws SQLException if a database error occurs
     */
    boolean isEmailInUse(String email) throws SQLException;
    
    /**
     * Generate a verification token for a user's email address.
     * 
     * @param user The user to generate a verification token for
     * @return The verification token
     * @throws SQLException if a database error occurs
     */
    String generateEmailVerificationInvitation(User user) throws SQLException;

    /**
     * Verify a user's email address using a verification token.
     * 
     * @param token The verification token
     * @return true if the email was verified, false otherwise
     * @throws SQLException if a database error occurs
     */
    boolean verifyEmail(String token) throws SQLException;
}