package com.celestra.enums;

/**
 * Defines the current state of a company account.
 * Maps to the company_status enum type in the database.
 */
public enum CompanyStatus {
    /**
     * Company is operational and in good standing
     */
    ACTIVE,
    
    /**
     * Company account has been temporarily disabled
     */
    SUSPENDED,
    
    /**
     * Company has been removed from active use but data is preserved
     */
    ARCHIVED;
    
    /**
     * Converts a string value to the corresponding enum value.
     * 
     * @param value The string representation of the enum
     * @return The corresponding enum value, or null if no match is found
     */
    public static CompanyStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        
        try {
            return CompanyStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}