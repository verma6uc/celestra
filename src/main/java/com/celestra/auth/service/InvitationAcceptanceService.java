package com.celestra.auth.service;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import com.celestra.email.exception.EmailException;
import com.celestra.model.Invitation;
import com.celestra.model.User;

/**
 * Service interface for handling the invitation acceptance flow.
 */
public interface InvitationAcceptanceService {
    
    /**
     * Validates an invitation token and returns the associated invitation if valid.
     * 
     * @param token The invitation token to validate
     * @return The invitation if valid, empty otherwise
     * @throws SQLException if a database error occurs
     */
    Optional<Invitation> validateInvitationToken(String token) throws SQLException;
    
    /**
     * Retrieves the user associated with an invitation.
     * 
     * @param invitation The invitation
     * @return The user associated with the invitation
     * @throws SQLException if a database error occurs
     */
    Optional<User> getUserFromInvitation(Invitation invitation) throws SQLException;
    
    /**
     * Completes the account setup for a user accepting an invitation.
     * 
     * @param token The invitation token
     * @param password The user's chosen password
     * @param confirmPassword The confirmation of the user's password
     * @param userDetails Additional user details to update (e.g., name, phone number)
     * @param ipAddress The IP address of the user accepting the invitation
     * @return The updated user if successful, empty otherwise
     * @throws SQLException if a database error occurs
     * @throws IllegalArgumentException if the passwords don't match or don't meet requirements
     */
    Optional<User> completeAccountSetup(String token, String password, String confirmPassword, 
            Map<String, String> userDetails, String ipAddress) 
            throws SQLException, IllegalArgumentException;
    
    /**
     * Activates a user account after invitation acceptance.
     * 
     * @param userId The ID of the user to activate
     * @param ipAddress The IP address of the user
     * @return true if the account was activated, false otherwise
     * @throws SQLException if a database error occurs
     */
    boolean activateUserAccount(Integer userId, String ipAddress) throws SQLException;
    
    /**
     * Sends a welcome email to a user after successful invitation acceptance.
     * 
     * @param user The user who accepted the invitation
     * @throws EmailException if an error occurs sending the email
     */
    void sendWelcomeEmail(User user) throws EmailException;
    
    /**
     * Validates a password against the system's password requirements.
     * 
     * @param password The password to validate
     * @return true if the password meets all requirements, false otherwise
     */
    boolean validatePassword(String password);
    
    /**
     * Gets a map of password requirements for display to the user.
     * 
     * @return A map of requirement descriptions and their status (true/false)
     */
    Map<String, Boolean> getPasswordRequirements(String password);
}