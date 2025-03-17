package com.celestra.enums;

/**
 * Defines the urgency level of notifications.
 * Maps to the notification_priority enum type in the database.
 */
public enum NotificationPriority {
    /**
     * Informational notification with minimal urgency
     */
    LOW,
    
    /**
     * Standard notification with normal urgency
     */
    MEDIUM,
    
    /**
     * Important notification that requires attention
     */
    HIGH,
    
    /**
     * Urgent notification that requires immediate attention
     */
    CRITICAL;
    
    /**
     * Converts a string value to the corresponding enum value.
     * 
     * @param value The string representation of the enum
     * @return The corresponding enum value, or null if no match is found
     */
    public static NotificationPriority fromString(String value) {
        if (value == null) {
            return null;
        }
        
        try {
            return NotificationPriority.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}