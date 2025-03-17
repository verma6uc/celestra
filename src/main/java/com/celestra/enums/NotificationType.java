package com.celestra.enums;

/**
 * Categorizes different types of system notifications.
 * Maps to the notification_type enum type in the database.
 */
public enum NotificationType {
    /**
     * Notification about unsuccessful authentication attempt
     */
    FAILED_LOGIN_NOTIFICATION,
    
    /**
     * Notification about user session expiration
     */
    SESSION_EXPIRY,
    
    /**
     * Notification about password reset request or completion
     */
    PASSWORD_RESET,
    
    /**
     * Notification about system access invitation
     */
    INVITATION,
    
    /**
     * Notification about system configuration changes
     */
    CONFIG_CHANGE,
    
    /**
     * Notification about billing events (payments, invoices, etc.)
     */
    BILLING_EVENT,
    
    /**
     * Notification about changes to agent operational status
     */
    AGENT_STATUS_CHANGE,
    
    /**
     * Notification about updates to knowledge base content
     */
    KNOWLEDGE_BASE_UPDATE,
    
    /**
     * Notification about security-related events
     */
    SECURITY_ALERT,
    
    /**
     * Notification about task assignments or updates
     */
    TASK_ASSIGNMENT,
    
    /**
     * Notification about scheduled system maintenance
     */
    SYSTEM_MAINTENANCE,
    
    /**
     * General notification that doesn't fit other categories
     */
    GENERAL;
    
    /**
     * Converts a string value to the corresponding enum value.
     * 
     * @param value The string representation of the enum
     * @return The corresponding enum value, or null if no match is found
     */
    public static NotificationType fromString(String value) {
        if (value == null) {
            return null;
        }
        
        try {
            return NotificationType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}