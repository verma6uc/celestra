package com.celestra.model;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

import com.celestra.enums.AuditEventType;

/**
 * Represents a security audit trail record for compliance and investigations.
 * Maps to the audit_logs table in the database.
 */
public class AuditLog {
    private Integer id;
    private Integer userId;
    private AuditEventType eventType;
    private String eventDescription;
    private String ipAddress;
    private Integer signedBy;
    private String digitalSignature;
    private String reason;
    private String tableName;
    private String recordId;
    private UUID groupId;
    private Timestamp createdAt;
    
    // References to associated entities (not stored in database)
    private User user;
    private User signer;
    
    /**
     * Default constructor
     */
    public AuditLog() {
        // Default constructor
    }
    
    /**
     * Parameterized constructor with required fields
     * 
     * @param eventType The category of security event
     */
    public AuditLog(AuditEventType eventType) {
        this.eventType = eventType;
    }
    
    /**
     * Full parameterized constructor
     * 
     * @param id The audit record ID
     * @param userId The ID of the user who performed the action (if applicable)
     * @param eventType The category of security event
     * @param eventDescription Detailed information about what occurred
     * @param ipAddress IP address where action originated
     * @param signedBy The ID of the user who verified/signed this audit record
     * @param digitalSignature Cryptographic signature to ensure audit integrity
     * @param reason Explanation for why the action was performed
     * @param tableName Name of database table that was modified
     * @param recordId Identifier of the specific database record that was modified
     * @param groupId UUID to group related audit events from a single logical operation
     * @param createdAt The timestamp when security event occurred
     */
    public AuditLog(Integer id, Integer userId, AuditEventType eventType, String eventDescription, 
                   String ipAddress, Integer signedBy, String digitalSignature, String reason, 
                   String tableName, String recordId, UUID groupId, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.eventType = eventType;
        this.eventDescription = eventDescription;
        this.ipAddress = ipAddress;
        this.signedBy = signedBy;
        this.digitalSignature = digitalSignature;
        this.reason = reason;
        this.tableName = tableName;
        this.recordId = recordId;
        this.groupId = groupId;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public AuditEventType getEventType() {
        return eventType;
    }

    public void setEventType(AuditEventType eventType) {
        this.eventType = eventType;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getSignedBy() {
        return signedBy;
    }

    public void setSignedBy(Integer signedBy) {
        this.signedBy = signedBy;
    }

    public String getDigitalSignature() {
        return digitalSignature;
    }

    public void setDigitalSignature(String digitalSignature) {
        this.digitalSignature = digitalSignature;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.userId = user.getId();
        }
    }
    
    public User getSigner() {
        return signer;
    }

    public void setSigner(User signer) {
        this.signer = signer;
        if (signer != null) {
            this.signedBy = signer.getId();
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditLog auditLog = (AuditLog) o;
        return Objects.equals(id, auditLog.id) &&
               eventType == auditLog.eventType &&
               Objects.equals(createdAt, auditLog.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, eventType, createdAt);
    }

    @Override
    public String toString() {
        return "AuditLog{" +
               "id=" + id +
               ", userId=" + userId +
               ", eventType=" + eventType +
               ", eventDescription='" + (eventDescription != null ? eventDescription.substring(0, Math.min(eventDescription.length(), 30)) + "..." : null) + '\'' +
               ", ipAddress='" + ipAddress + '\'' +
               ", signedBy=" + signedBy +
               ", tableName='" + tableName + '\'' +
               ", recordId='" + recordId + '\'' +
               ", groupId=" + groupId +
               ", createdAt=" + createdAt +
               '}';
    }
}