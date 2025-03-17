package com.celestra.dao;

import java.sql.SQLException;
import java.util.List;

import com.celestra.enums.InvitationStatus;
import com.celestra.model.Invitation;

/**
 * Data Access Object interface for Invitation entities.
 */
public interface InvitationDao extends BaseDao<Invitation, Integer> {
    
    /**
     * Find invitations by user ID.
     * 
     * @param userId The user ID
     * @return A list of invitations for the user
     * @throws SQLException if a database access error occurs
     */
    List<Invitation> findByUserId(Integer userId) throws SQLException;
    
    /**
     * Find invitations by status.
     * 
     * @param status The invitation status
     * @return A list of invitations with the specified status
     * @throws SQLException if a database access error occurs
     */
    List<Invitation> findByStatus(InvitationStatus status) throws SQLException;
    
    /**
     * Find an invitation by token.
     * 
     * @param token The invitation token
     * @return The invitation with the specified token, or empty if not found
     * @throws SQLException if a database access error occurs
     */
    java.util.Optional<Invitation> findByToken(String token) throws SQLException;
    
    /**
     * Find expired invitations.
     * 
     * @return A list of expired invitations
     * @throws SQLException if a database access error occurs
     */
    List<Invitation> findExpired() throws SQLException;
    
    /**
     * Update the status of an invitation.
     * 
     * @param id The invitation ID
     * @param status The new status
     * @return true if the update was successful, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean updateStatus(Integer id, InvitationStatus status) throws SQLException;
    
    /**
     * Increment the resend count for an invitation.
     * 
     * @param id The invitation ID
     * @return true if the update was successful, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean incrementResendCount(Integer id) throws SQLException;
}