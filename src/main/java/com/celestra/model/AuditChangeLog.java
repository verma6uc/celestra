package com.celestra.model;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Represents detailed before/after values for changes in audited operations.
 * Maps to the audit_change_logs table in the database.
 */
public class AuditChangeLog {
    private Integer id;
    private Integer auditLogId;
    private String columnName;
    private String oldValue;
    private String newValue;
    private OffsetDateTime createdAt;
    
    // Reference to the associated audit log (not stored in database)
    private AuditLog auditLog;
    
    /**
     * Default constructor
     */
    public AuditChangeLog() {
        // Default constructor
    }
    
    /**
     * Parameterized constructor with required fields
     * 
     * @param auditLogId The ID of the associated audit record
     * @param columnName The name of database column that was modified
     * @param oldValue The value before the change was made
     * @param newValue The value after the change was made
     */
    public AuditChangeLog(Integer auditLogId, String columnName, String oldValue, String newValue) {
        this.auditLogId = auditLogId;
        this.columnName = columnName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
    
    /**
     * Full parameterized constructor
     * 
     * @param id The change log ID
     * @param auditLogId The ID of the associated audit record
     * @param columnName The name of database column that was modified
     * @param oldValue The value before the change was made
     * @param newValue The value after the change was made
     * @param createdAt The timestamp when change was recorded
     */
    public AuditChangeLog(Integer id, Integer auditLogId, String columnName, 
                         String oldValue, String newValue, OffsetDateTime createdAt) {
        this.id = id;
        this.auditLogId = auditLogId;
        this.columnName = columnName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAuditLogId() {
        return auditLogId;
    }

    public void setAuditLogId(Integer auditLogId) {
        this.auditLogId = auditLogId;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public AuditLog getAuditLog() {
        return auditLog;
    }

    public void setAuditLog(AuditLog auditLog) {
        this.auditLog = auditLog;
        if (auditLog != null) {
            this.auditLogId = auditLog.getId();
        }
    }
    
    /**
     * Checks if the value was changed
     * 
     * @return true if the value was changed, false otherwise
     */
    public boolean hasChanged() {
        if (oldValue == null && newValue == null) {
            return false;
        }
        if (oldValue == null || newValue == null) {
            return true;
        }
        return !oldValue.equals(newValue);
    }
    
    /**
     * Gets a formatted representation of the change
     * 
     * @return A string representing the change in format "oldValue -> newValue"
     */
    public String getChangeDescription() {
        String oldDisplay = oldValue != null ? oldValue : "null";
        String newDisplay = newValue != null ? newValue : "null";
        
        // Truncate long values
        if (oldDisplay.length() > 50) {
            oldDisplay = oldDisplay.substring(0, 47) + "...";
        }
        if (newDisplay.length() > 50) {
            newDisplay = newDisplay.substring(0, 47) + "...";
        }
        
        return oldDisplay + " -> " + newDisplay;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditChangeLog that = (AuditChangeLog) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(auditLogId, that.auditLogId) &&
               Objects.equals(columnName, that.columnName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, auditLogId, columnName);
    }

    @Override
    public String toString() {
        return "AuditChangeLog{" +
               "id=" + id +
               ", auditLogId=" + auditLogId +
               ", columnName='" + columnName + '\'' +
               ", oldValue='" + (oldValue != null ? oldValue.substring(0, Math.min(oldValue.length(), 30)) + "..." : null) + '\'' +
               ", newValue='" + (newValue != null ? newValue.substring(0, Math.min(newValue.length(), 30)) + "..." : null) + '\'' +
               ", createdAt=" + createdAt +
               '}';
    }
}