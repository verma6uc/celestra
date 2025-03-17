package com.celestra.enums;

/**
 * Tracks the current state of an invitation.
 * Maps to the invitation_status enum type in the database.
 */
public enum InvitationStatus {
    /**
     * Invitation has been created but not yet sent
     */
    PENDING,
    
    /**
     * Invitation has been sent to the recipient
     */
    SENT,
    
    /**
     * Invitation has passed its expiration date without being accepted
     */
    EXPIRED,
    
    /**
     * Invitation was cancelled by the sender
     */
    CANCELLED,
    
    /**
     * Invitation was successfully accepted by the recipient
     */
    ACCEPTED;
    
    /**
     * Converts a string value to the corresponding enum value.
     * 
     * @param value The string representation of the enum
     * @return The corresponding enum value, or null if no match is found
     */
    public static InvitationStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        
        try {
            return InvitationStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}