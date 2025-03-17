package com.celestra.auth.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.celestra.enums.AuditEventType;
import com.celestra.model.AuditChangeLog;
import com.celestra.model.AuditLog;
import com.celestra.model.User;

/**
 * Service for comprehensive audit logging of security-related events.
 * Provides methods to record various types of security events and retrieve audit logs.
 */
public interface AuditService {
    
    /**
     * Records a successful login event.
     * 
     * @param user The user who logged in
     * @param ipAddress The IP address from which the login occurred
     * @param metadata Additional metadata about the login (e.g., user agent, device info)
     * @return The created audit log entry
     * @throws SQLException if a database error occurs
     */
    AuditLog recordSuccessfulLogin(User user, String ipAddress) throws SQLException;
    
    /**
     * Records a failed login attempt.
     * 
     * @param user The user who attempted to log in (may be null if user doesn't exist)
     * @param email The email used in the login attempt
     * @param ipAddress The IP address from which the login attempt occurred
     * @param reason The reason for the failure (e.g., "Invalid password", "Account locked")
     * @return The created audit log entry
     * @throws SQLException if a database error occurs
     */
    AuditLog recordFailedLogin(User user, String email, String ipAddress, String reason) throws SQLException;
    
    /**
     * Records a logout event.
     * 
     * @param user The user who logged out
     * @param ipAddress The IP address from which the logout occurred
     * @param sessionId The ID of the session that was terminated
     * @return The created audit log entry
     * @throws SQLException if a database error occurs
     */
    AuditLog recordLogout(User user, String ipAddress, String sessionId) throws SQLException;
    
    /**
     * Records a password change event.
     * 
     * @param user The user whose password was changed
     * @param ipAddress The IP address from which the password change occurred
     * @param changedByUser The user who performed the change (may be different from user if admin)
     * @return The created audit log entry
     * @throws SQLException if a database error occurs
     */
    AuditLog recordPasswordChange(User user, String ipAddress, User changedByUser) throws SQLException;
    
    /**
     * Records a password reset request event.
     * 
     * @param user The user who requested a password reset
     * @param ipAddress The IP address from which the request originated
     * @return The created audit log entry
     * @throws SQLException if a database error occurs
     */
    AuditLog recordPasswordResetRequest(User user, String ipAddress) throws SQLException;
    
    /**
     * Records a password reset completion event.
     * 
     * @param user The user whose password was reset
     * @param ipAddress The IP address from which the reset was completed
     * @return The created audit log entry
     * @throws SQLException if a database error occurs
     */
    AuditLog recordPasswordResetCompletion(User user, String ipAddress) throws SQLException;
    
    /**
     * Records a user account creation event.
     * 
     * @param user The user account that was created
     * @param ipAddress The IP address from which the account was created
     * @param createdByUser The user who created the account (may be null for self-registration)
     * @return The created audit log entry
     * @throws SQLException if a database error occurs
     */
    AuditLog recordUserCreation(User user, String ipAddress, User createdByUser) throws SQLException;
    
    /**
     * Records a user account update event with before/after values.
     * 
     * @param user The user account that was updated
     * @param ipAddress The IP address from which the update occurred
     * @param updatedByUser The user who performed the update
     * @param beforeValues Map of field names to their values before the update
     * @param afterValues Map of field names to their values after the update
     * @return The created audit log entry
     * @throws SQLException if a database error occurs
     */
    AuditLog recordUserUpdate(User user, String ipAddress, User updatedByUser,
                             Map<String, Object> beforeValues, Map<String, Object> afterValues) throws SQLException;
    
    /**
     * Records a user account status change event.
     * 
     * @param user The user whose status was changed
     * @param ipAddress The IP address from which the change occurred
     * @param changedByUser The user who performed the change
     * @param oldStatus The previous status
     * @param newStatus The new status
     * @param reason The reason for the status change
     * @return The created audit log entry
     * @throws SQLException if a database error occurs
     */
    AuditLog recordUserStatusChange(User user, String ipAddress, User changedByUser, 
                                   String oldStatus, String newStatus, String reason) throws SQLException;
    
    /**
     * Records a role assignment change event.
     * 
     * @param user The user whose role was changed
     * @param ipAddress The IP address from which the change occurred
     * @param changedByUser The user who performed the change
     * @param oldRole The previous role
     * @param newRole The new role
     * @param reason The reason for the role change
     * @return The created audit log entry
     * @throws SQLException if a database error occurs
     */
    AuditLog recordRoleChange(User user, String ipAddress, User changedByUser, 
                             String oldRole, String newRole, String reason) throws SQLException;
    
    /**
     * Records a user account lockout event.
     * 
     * @param user The user who was locked out
     * @param ipAddress The IP address associated with the lockout
     * @param reason The reason for the lockout
     * @return The created audit log entry
     * @throws SQLException if a database error occurs
     */
    AuditLog recordAccountLockout(User user, String ipAddress, String reason) throws SQLException;
    
    /**
     * Records a user account unlock event.
     * 
     * @param user The user who was unlocked
     * @param ipAddress The IP address from which the unlock occurred
     * @param unlockedByUser The user who performed the unlock (may be null for automatic unlocks)
     * @param reason The reason for the unlock
     * @return The created audit log entry
     * @throws SQLException if a database error occurs
     */
    AuditLog recordAccountUnlock(User user, String ipAddress, User unlockedByUser, String reason) throws SQLException;
    
    /**
     * Records a generic security event.
     * 
     * @param eventType The type of security event
     * @param user The user associated with the event (may be null)
     * @param ipAddress The IP address associated with the event
     * @param description A detailed description of the event
     * @param tableName The name of the database table affected (if applicable)
     * @param recordId The ID of the database record affected (if applicable)
     * @param reason The reason for the event
     * @return The created audit log entry
     * @throws SQLException if a database error occurs
     */
    AuditLog recordSecurityEvent(AuditEventType eventType, User user, String ipAddress, 
                                String description, String tableName, String recordId, 
                                String reason) throws SQLException;
    
    /**
     * Creates a digital signature for an audit log entry to ensure integrity.
     * 
     * @param auditLog The audit log entry to sign
     * @param signingUser The user who is signing the entry (may be null for system-generated signatures)
     * @return The updated audit log with digital signature
     * @throws SQLException if a database error occurs
     */
    AuditLog signAuditLog(AuditLog auditLog, User signingUser) throws SQLException;
    
    /**
     * Creates audit change log entries for a field that has changed.
     * 
     * @param auditLogId The ID of the parent audit log
     * @param columnName The name of the column/field that changed
     * @param oldValue The old value
     * @param newValue The new value
     * @return The created audit change log entry
     * @throws SQLException if a database error occurs
     */
    AuditChangeLog recordChange(Integer auditLogId, String columnName, String oldValue, String newValue) throws SQLException;
    
    /**
     * Verifies the digital signature of an audit log entry.
     * 
     * @param auditLog The audit log entry to verify
     * @return true if the signature is valid, false otherwise
     */
    boolean verifyAuditLogSignature(AuditLog auditLog);
    
    /**
     * Retrieves audit logs for a specific user.
     * 
     * @param userId The ID of the user
     * @return A list of audit logs for the user
     * @throws SQLException if a database error occurs
     */
    List<AuditLog> getAuditLogsForUser(Integer userId) throws SQLException;
    
    /**
     * Retrieves audit logs of a specific event type.
     * 
     * @param eventType The type of event
     * @return A list of audit logs of the specified type
     * @throws SQLException if a database error occurs
     */
    List<AuditLog> getAuditLogsByEventType(AuditEventType eventType) throws SQLException;
    
    /**
     * Retrieves audit logs within a date range.
     * 
     * @param startDate The start date in ISO format (yyyy-MM-dd)
     * @param endDate The end date in ISO format (yyyy-MM-dd)
     * @return A list of audit logs within the date range
     * @throws SQLException if a database error occurs
     */
    List<AuditLog> getAuditLogsByDateRange(String startDate, String endDate) throws SQLException;
    
    /**
     * Retrieves audit logs for a specific database table and record.
     * 
     * @param tableName The name of the database table
     * @param recordId The ID of the database record
     * @return A list of audit logs for the specified table and record
     * @throws SQLException if a database error occurs
     */
    List<AuditLog> getAuditLogsForRecord(String tableName, String recordId) throws SQLException;
    
    /**
     * Retrieves audit logs that are part of the same logical operation.
     * 
     * @param groupId The UUID that groups related audit events
     * @return A list of audit logs with the specified group ID
     * @throws SQLException if a database error occurs
     */
    List<AuditLog> getAuditLogsByGroupId(UUID groupId) throws SQLException;
    
    /**
     * Retrieves an audit log by its ID.
     * 
     * @param id The ID of the audit log
     * @return An Optional containing the audit log if found, or empty if not found
     * @throws SQLException if a database error occurs
     */
    Optional<AuditLog> getAuditLogById(Integer id) throws SQLException;
}