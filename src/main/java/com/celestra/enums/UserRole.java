package com.celestra.enums;

/**
 * Defines the permission levels for users in the system.
 * Maps to the user_role enum type in the database.
 */
public enum UserRole {
    /**
     * Highest permission level with access to all system functions and companies
     */
    SUPER_ADMIN,
    
    /**
     * Administrator for a specific company with full access to that company's resources
     */
    COMPANY_ADMIN,
    
    /**
     * Administrator for a specific workspace/space within a company
     */
    SPACE_ADMIN,
    
    /**
     * Standard user with limited permissions based on assigned roles
     */
    REGULAR_USER;
    
    /**
     * Converts a string value to the corresponding enum value.
     * 
     * @param value The string representation of the enum
     * @return The corresponding enum value, or null if no match is found
     */
    public static UserRole fromString(String value) {
        if (value == null) {
            return null;
        }
        
        try {
            return UserRole.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}