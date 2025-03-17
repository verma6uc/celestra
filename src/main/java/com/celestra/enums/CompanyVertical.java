package com.celestra.enums;

/**
 * Categorizes companies by their industry sector.
 * Maps to the company_vertical enum type in the database.
 */
public enum CompanyVertical {
    /**
     * Technology and software companies
     */
    TECH,
    
    /**
     * Pharmaceutical and healthcare companies
     */
    PHARMACEUTICAL,
    
    /**
     * Financial services and banking companies
     */
    FINANCE,
    
    /**
     * Retail and e-commerce companies
     */
    RETAIL,
    
    /**
     * Companies in industries not covered by other categories
     */
    OTHER;
    
    /**
     * Converts a string value to the corresponding enum value.
     * 
     * @param value The string representation of the enum
     * @return The corresponding enum value, or null if no match is found
     */
    public static CompanyVertical fromString(String value) {
        if (value == null) {
            return null;
        }
        
        try {
            return CompanyVertical.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}