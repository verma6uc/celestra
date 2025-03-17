package com.celestra.enums;

/**
 * Categorizes companies by their approximate number of employees.
 * Maps to the company_size enum type in the database.
 */
public enum CompanySize {
    /**
     * Small companies (typically 1-50 employees)
     */
    SMALL,
    
    /**
     * Medium-sized companies (typically 51-250 employees)
     */
    MEDIUM,
    
    /**
     * Large companies (typically 251-1000 employees)
     */
    LARGE,
    
    /**
     * Enterprise-level companies (typically 1000+ employees)
     */
    ENTERPRISE;
    
    /**
     * Converts a string value to the corresponding enum value.
     * 
     * @param value The string representation of the enum
     * @return The corresponding enum value, or null if no match is found
     */
    public static CompanySize fromString(String value) {
        if (value == null) {
            return null;
        }
        
        try {
            return CompanySize.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}