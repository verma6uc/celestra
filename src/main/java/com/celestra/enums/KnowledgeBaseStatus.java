package com.celestra.enums;

/**
 * Defines the current operational state of a knowledge base.
 * Maps to the knowledge_base_status enum type in the database.
 */
public enum KnowledgeBaseStatus {
    /**
     * Knowledge base is operational and available for use
     */
    ACTIVE,
    
    /**
     * Knowledge base is currently being constructed or updated
     */
    BUILDING,
    
    /**
     * Knowledge base is temporarily unavailable but can be re-enabled
     */
    DISABLED,
    
    /**
     * Knowledge base has been removed from active use but data is preserved
     */
    ARCHIVED,
    
    /**
     * Knowledge base is in development/creation phase and not yet ready for use
     */
    DRAFT;
    
    /**
     * Converts a string value to the corresponding enum value.
     * 
     * @param value The string representation of the enum
     * @return The corresponding enum value, or null if no match is found
     */
    public static KnowledgeBaseStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        
        try {
            return KnowledgeBaseStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}