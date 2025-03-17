package com.celestra.enums;

/**
 * Defines how notifications are delivered to users.
 * Maps to the notification_delivery_method enum type in the database.
 */
public enum NotificationDeliveryMethod {
    /**
     * Notification is delivered within the application interface
     */
    IN_APP,
    
    /**
     * Notification is sent via email
     */
    EMAIL,
    
    /**
     * Notification is sent via text message
     */
    SMS,
    
    /**
     * Notification is delivered via HTTP webhook to an external system
     */
    WEBHOOK,
    
    /**
     * Notification is sent as a mobile push notification
     */
    PUSH;
    
    /**
     * Converts a string value to the corresponding enum value.
     * 
     * @param value The string representation of the enum
     * @return The corresponding enum value, or null if no match is found
     */
    public static NotificationDeliveryMethod fromString(String value) {
        if (value == null) {
            return null;
        }
        
        try {
            return NotificationDeliveryMethod.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}