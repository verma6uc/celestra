package com.celestra.enums;

/**
 * Defines the current operational state of an agent.
 * Maps to the agent_status enum type in the database.
 */
public enum AgentStatus {
    /**
     * Agent is operational and available for use
     */
    ACTIVE,
    
    /**
     * Agent is temporarily unavailable but can be re-enabled
     */
    DISABLED,
    
    /**
     * Agent has been removed from active use but data is preserved
     */
    ARCHIVED,
    
    /**
     * Agent is in development/creation phase and not yet ready for use
     */
    DRAFT;
    
    /**
     * Converts a string value to the corresponding enum value.
     * 
     * @param value The string representation of the enum
     * @return The corresponding enum value, or null if no match is found
     */
    public static AgentStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        
        try {
            return AgentStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}