package com.celestra.enums;

/**
 * Categorizes different types of security and audit events.
 * Maps to the audit_event_type enum type in the database.
 */
public enum AuditEventType {
    /**
     * Unsuccessful authentication attempt
     */
    FAILED_LOGIN,
    
    /**
     * Successful user authentication
     */
    SUCCESSFUL_LOGIN,
    
    /**
     * New user session started
     */
    SESSION_STARTED,
    
    /**
     * User session terminated
     */
    SESSION_ENDED,
    
    /**
     * User permission level was modified
     */
    ROLE_ASSIGNMENT_CHANGE,
    
    /**
     * System configuration was altered
     */
    CONFIGURATION_UPDATE,
    
    /**
     * Data was exported from the system
     */
    DATA_EXPORT,
    
    /**
     * Miscellaneous event type for events that don't fit other categories
     */
    OTHER;
    
    /**
     * Converts a string value to the corresponding enum value.
     * 
     * @param value The string representation of the enum
     * @return The corresponding enum value, or null if no match is found
     */
    public static AuditEventType fromString(String value) {
        if (value == null) {
            return null;
        }
        
        try {
            return AuditEventType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}