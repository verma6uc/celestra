package com.celestra.auth.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.celestra.enums.InvitationStatus;
import com.celestra.model.Invitation;
import com.celestra.model.User;
import com.celestra.email.exception.EmailException;

/**
 * Service interface for managing user invitations.
 */
public interface InvitationService {
    
    /**
     * Create a new invitation for a user.
     * 
     * @param user The user to invite
     * @param inviterUserId The ID of the user sending the invitation
     * @param ipAddress The IP address of the inviter
     * @return The created invitation
     * @throws SQLException if a database error occurs
     * @throws EmailException if an error occurs sending the invitation email
     */
    Invitation createInvitation(User user, Integer inviterUserId, String ipAddress) 
            throws SQLException, EmailException;
    
    /**
     * Send an invitation to a user.
     * 
     * @param invitationId The ID of the invitation to send
     * @param inviterUserId The ID of the user sending the invitation
     * @param ipAddress The IP address of the inviter
     * @return The updated invitation
     * @throws SQLException if a database error occurs
     * @throws EmailException if an error occurs sending the invitation email
     */
    Invitation sendInvitation(Integer invitationId, Integer inviterUserId, String ipAddress) 
            throws SQLException, EmailException;
    
    /**
     * Resend an invitation to a user.
     * 
     * @param invitationId The ID of the invitation to resend
     * @param inviterUserId The ID of the user resending the invitation
     * @param ipAddress The IP address of the inviter
     * @return The updated invitation
     * @throws SQLException if a database error occurs
     * @throws EmailException if an error occurs sending the invitation email
     */
    Invitation resendInvitation(Integer invitationId, Integer inviterUserId, String ipAddress) 
            throws SQLException, EmailException;
    
    /**
     * Cancel an invitation.
     * 
     * @param invitationId The ID of the invitation to cancel
     * @param cancellerUserId The ID of the user cancelling the invitation
     * @param ipAddress The IP address of the canceller
     * @param reason The reason for cancellation
     * @return true if the invitation was cancelled, false otherwise
     * @throws SQLException if a database error occurs
     */
    boolean cancelInvitation(Integer invitationId, Integer cancellerUserId, String ipAddress, String reason) 
            throws SQLException;
    
    /**
     * Validate an invitation token.
     * 
     * @param token The invitation token to validate
     * @return The invitation if valid, empty otherwise
     * @throws SQLException if a database error occurs
     */
    Optional<Invitation> validateInvitationToken(String token) throws SQLException;
    
    /**
     * Mark an invitation as accepted.
     * 
     * @param token The invitation token
     * @param ipAddress The IP address of the user accepting the invitation
     * @return true if the invitation was marked as accepted, false otherwise
     * @throws SQLException if a database error occurs
     */
    boolean acceptInvitation(String token, String ipAddress) throws SQLException;
    
    /**
     * Get all invitations for a user.
     * 
     * @param userId The user ID
     * @return A list of invitations for the user
     * @throws SQLException if a database error occurs
     */
    List<Invitation> getInvitationsByUserId(Integer userId) throws SQLException;
    
    /**
     * Get all invitations with a specific status.
     * 
     * @param status The invitation status
     * @return A list of invitations with the specified status
     * @throws SQLException if a database error occurs
     */
    List<Invitation> getInvitationsByStatus(InvitationStatus status) throws SQLException;
    
    /**
     * Get an invitation by ID.
     * 
     * @param invitationId The invitation ID
     * @return The invitation if found, empty otherwise
     * @throws SQLException if a database error occurs
     */
    Optional<Invitation> getInvitationById(Integer invitationId) throws SQLException;
    
    /**
     * Get an invitation by token.
     * 
     * @param token The invitation token
     * @return The invitation if found, empty otherwise
     * @throws SQLException if a database error occurs
     */
    Optional<Invitation> getInvitationByToken(String token) throws SQLException;
    
    /**
     * Clean up expired invitations.
     * 
     * @return The number of invitations marked as expired
     * @throws SQLException if a database error occurs
     */
    int cleanupExpiredInvitations() throws SQLException;
    
    /**
     * Generate a secure invitation token.
     * 
     * @return A secure random token
     */
    String generateInvitationToken();
}