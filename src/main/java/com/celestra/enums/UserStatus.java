package com.celestra.enums;

/**
 * Defines the current state of a user account.
 * Maps to the user_status enum type in the database.
 */
public enum UserStatus {
    /**
     * User account is operational and in good standing
     */
    ACTIVE,
    
    /**
     * User account has been temporarily disabled
     */
    SUSPENDED,
    
    /**
     * User account has been blocked due to security concerns
     */
    BLOCKED,
    
    /**
     * User has been removed from active use but data is preserved
     */
    ARCHIVED;
    
    /**
     * Converts a string value to the corresponding enum value.
     * 
     * @param value The string representation of the enum
     * @return The corresponding enum value, or null if no match is found
     */
    public static UserStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        
        try {
            return UserStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}