package com.celestra.enums;

/**
 * Defines the delivery status of notifications.
 * Maps to the notification_status enum type in the database.
 */
public enum NotificationStatus {
    /**
     * Notification is queued for delivery but has not been sent yet
     */
    PENDING,
    
    /**
     * Notification has been successfully delivered to the recipient
     */
    DELIVERED,
    
    /**
     * Notification delivery attempt failed
     */
    FAILED;
    
    /**
     * Converts a string value to the corresponding enum value.
     * 
     * @param value The string representation of the enum
     * @return The corresponding enum value, or null if no match is found
     */
    public static NotificationStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        
        try {
            return NotificationStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}