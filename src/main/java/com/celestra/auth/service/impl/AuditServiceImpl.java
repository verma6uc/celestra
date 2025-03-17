package com.celestra.auth.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.celestra.auth.service.AuditService;
import com.celestra.dao.AuditChangeLogDao;
import com.celestra.dao.impl.AuditChangeLogDaoImpl;
import com.celestra.dao.AuditLogDao;
import com.celestra.dao.impl.AuditLogDaoImpl;
import com.celestra.model.AuditChangeLog;
import com.celestra.enums.AuditEventType;
import com.celestra.model.AuditLog;
import com.celestra.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Implementation of the AuditService interface.
 * Provides comprehensive audit logging functionality for security events.
 */
public class AuditServiceImpl implements AuditService {
    
    private static final Logger LOGGER = Logger.getLogger(AuditServiceImpl.class.getName());
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();
    
    private final AuditLogDao auditLogDao;
    private final AuditChangeLogDao auditChangeLogDao;
    
    /**
     * Default constructor.
     * Initializes the DAO with default implementation.
     */
    public AuditServiceImpl() {
        this(new AuditLogDaoImpl());
    }
    
    /**
     * Parameterized constructor for dependency injection.
     * 
     * @param auditLogDao The AuditLogDao implementation to use
     */
    public AuditServiceImpl(AuditLogDao auditLogDao) {
        this(auditLogDao, new AuditChangeLogDaoImpl());
    }
    
    public AuditServiceImpl(AuditLogDao auditLogDao, AuditChangeLogDao auditChangeLogDao) {
        this.auditLogDao = auditLogDao;
        this.auditChangeLogDao = auditChangeLogDao;
    }
    
    @Override
    public AuditLog recordSuccessfulLogin(User user, String ipAddress) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        AuditLog auditLog = new AuditLog(AuditEventType.SUCCESSFUL_LOGIN);
        auditLog.setUserId(user.getId());
        auditLog.setEventDescription("User successfully logged in");
        auditLog.setIpAddress(ipAddress);
        auditLog.setTableName("users");
        auditLog.setRecordId(user.getId().toString());
        auditLog.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        
        // Create digital signature
        signAuditLog(auditLog, null);
        
        return auditLogDao.create(auditLog);
    }
    
    @Override
    public AuditLog recordFailedLogin(User user, String email, String ipAddress, String reason) throws SQLException {
        AuditLog auditLog = new AuditLog(AuditEventType.FAILED_LOGIN);
        
        if (user != null) {
            auditLog.setUserId(user.getId());
            auditLog.setTableName("users");
            auditLog.setRecordId(user.getId().toString());
        }
        
        String description = "Failed login attempt";
        if (email != null && !email.isEmpty()) {
            description += " for email: " + email;
        }
        
        if (reason != null && !reason.isEmpty()) {
            description += " | Reason: " + reason;
            auditLog.setReason(reason);
        }
        
        auditLog.setEventDescription(description);
        auditLog.setIpAddress(ipAddress);
        auditLog.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        
        // Create digital signature
        signAuditLog(auditLog, null);
        
        return auditLogDao.create(auditLog);
    }
    
    @Override
    public AuditLog recordLogout(User user, String ipAddress, String sessionId) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        AuditLog auditLog = new AuditLog(AuditEventType.SESSION_ENDED);
        auditLog.setUserId(user.getId());
        
        String description = "User logged out";
        if (sessionId != null && !sessionId.isEmpty()) {
            description += " | Session ID: " + sessionId;
        }
        
        auditLog.setEventDescription(description);
        auditLog.setIpAddress(ipAddress);
        auditLog.setTableName("user_sessions");
        auditLog.setRecordId(sessionId);
        auditLog.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        
        // Create digital signature
        signAuditLog(auditLog, null);
        
        return auditLogDao.create(auditLog);
    }
    
    @Override
    public AuditLog recordPasswordChange(User user, String ipAddress, User changedByUser) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        AuditLog auditLog = new AuditLog(AuditEventType.OTHER);
        auditLog.setUserId(user.getId());
        
        String description = "Password changed";
        if (changedByUser != null && !changedByUser.getId().equals(user.getId())) {
            description += " by administrator";
            auditLog.setSignedBy(changedByUser.getId());
        } else {
            description += " by user";
        }
        
        auditLog.setEventDescription(description);
        auditLog.setIpAddress(ipAddress);
        auditLog.setTableName("users");
        auditLog.setRecordId(user.getId().toString());
        auditLog.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        
        // Create digital signature
        signAuditLog(auditLog, changedByUser);
        
        return auditLogDao.create(auditLog);
    }
    
    @Override
    public AuditLog recordPasswordResetRequest(User user, String ipAddress) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        AuditLog auditLog = new AuditLog(AuditEventType.OTHER);
        auditLog.setUserId(user.getId());
        auditLog.setEventDescription("Password reset requested");
        auditLog.setIpAddress(ipAddress);
        auditLog.setTableName("password_reset_tokens");
        auditLog.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        
        // Create digital signature
        signAuditLog(auditLog, null);
        
        return auditLogDao.create(auditLog);
    }
    
    @Override
    public AuditLog recordPasswordResetCompletion(User user, String ipAddress) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        AuditLog auditLog = new AuditLog(AuditEventType.OTHER);
        auditLog.setUserId(user.getId());
        auditLog.setEventDescription("Password reset completed");
        auditLog.setIpAddress(ipAddress);
        auditLog.setTableName("users");
        auditLog.setRecordId(user.getId().toString());
        auditLog.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        
        // Create digital signature
        signAuditLog(auditLog, null);
        
        return auditLogDao.create(auditLog);
    }
    
    @Override
    public AuditLog recordUserCreation(User user, String ipAddress, User createdByUser) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        AuditLog auditLog = new AuditLog(AuditEventType.OTHER);
        auditLog.setUserId(user.getId());
        
        String description = "User account created";
        if (createdByUser != null) {
            description += " by administrator";
            auditLog.setSignedBy(createdByUser.getId());
        } else {
            description += " via self-registration";
        }
        
        auditLog.setEventDescription(description);
        auditLog.setIpAddress(ipAddress);
        auditLog.setTableName("users");
        auditLog.setRecordId(user.getId().toString());
        auditLog.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        
        // Create digital signature
        signAuditLog(auditLog, createdByUser);
        
        return auditLogDao.create(auditLog);
    }
    
    @Override
    public AuditLog recordUserUpdate(User user, String ipAddress, User updatedByUser, 
                                    Map<String, Object> beforeValues, Map<String, Object> afterValues) throws SQLException {        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        AuditLog auditLog = new AuditLog(AuditEventType.OTHER);
        auditLog.setUserId(user.getId());
        
        String description = "User account updated";
        if (updatedByUser != null && !updatedByUser.getId().equals(user.getId())) {
            description += " by administrator";
            auditLog.setSignedBy(updatedByUser.getId());
        } else {
            description += " by user";
        }
        
        
        auditLog.setEventDescription(description);
        auditLog.setIpAddress(ipAddress);
        auditLog.setTableName("users");
        auditLog.setRecordId(user.getId().toString());
        auditLog.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        
        // Create the audit log first to get its ID
        AuditLog createdLog = auditLogDao.create(auditLog);
        
        // Create audit change logs for each changed field
        if (beforeValues != null && afterValues != null) {
            for (String field : afterValues.keySet()) {
                if (beforeValues.containsKey(field) && !Objects.equals(beforeValues.get(field), afterValues.get(field))) {
                    String oldValue = beforeValues.get(field) != null ? beforeValues.get(field).toString() : null;
                    String newValue = afterValues.get(field) != null ? afterValues.get(field).toString() : null;
                    recordChange(createdLog.getId(), field, oldValue, newValue);
                }
            }
        }
        // Create digital signature
        signAuditLog(auditLog, updatedByUser);
        
        return auditLogDao.create(auditLog);
    }
    
    @Override
    public AuditLog recordUserStatusChange(User user, String ipAddress, User changedByUser, 
                                          String oldStatus, String newStatus, String reason) throws SQLException {        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        AuditLog auditLog = new AuditLog(AuditEventType.OTHER);
        auditLog.setUserId(user.getId());
        
        String description = "User status changed from '" + oldStatus + "' to '" + newStatus + "'";
        if (changedByUser != null) {
            description += " by administrator";
            auditLog.setSignedBy(changedByUser.getId());
        }
        
        if (reason != null && !reason.isEmpty()) {
            description += " | Reason: " + reason;
            auditLog.setReason(reason);
        }
        
        auditLog.setEventDescription(description);
        auditLog.setIpAddress(ipAddress);
        auditLog.setTableName("users");
        auditLog.setRecordId(user.getId().toString());
        auditLog.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        
        // Create the audit log first to get its ID
        AuditLog createdLog = auditLogDao.create(auditLog);
        
        // Create audit change log for status change
        recordChange(createdLog.getId(), "status", oldStatus, newStatus);
        
        
        // Create digital signature
        signAuditLog(auditLog, changedByUser);
        
        return auditLogDao.create(auditLog);
    }
    
    @Override
    public AuditLog recordRoleChange(User user, String ipAddress, User changedByUser, 
                                    String oldRole, String newRole, String reason) throws SQLException {        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        AuditLog auditLog = new AuditLog(AuditEventType.ROLE_ASSIGNMENT_CHANGE);
        auditLog.setUserId(user.getId());
        
        String description = "User role changed from '" + oldRole + "' to '" + newRole + "'";
        if (changedByUser != null) {
            description += " by administrator";
            auditLog.setSignedBy(changedByUser.getId());
        }
        
        if (reason != null && !reason.isEmpty()) {
            description += " | Reason: " + reason;
            auditLog.setReason(reason);
        }
        
        auditLog.setEventDescription(description);
        auditLog.setIpAddress(ipAddress);
        auditLog.setTableName("users");
        auditLog.setRecordId(user.getId().toString());
        auditLog.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        
        // Create the audit log first to get its ID
        AuditLog createdLog = auditLogDao.create(auditLog);
        
        // Create audit change log for role change
        recordChange(createdLog.getId(), "role", oldRole, newRole);
        
        
        // Create digital signature
        signAuditLog(auditLog, changedByUser);
        
        return auditLogDao.create(auditLog);
    }
    
    @Override
    public AuditLog recordAccountLockout(User user, String ipAddress, String reason) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        AuditLog auditLog = new AuditLog(AuditEventType.OTHER);
        auditLog.setUserId(user.getId());
        
        String description = "User account locked";
        if (reason != null && !reason.isEmpty()) {
            description += " | Reason: " + reason;
            auditLog.setReason(reason);
        }
        
        auditLog.setEventDescription(description);
        auditLog.setIpAddress(ipAddress);
        auditLog.setTableName("user_lockouts");
        auditLog.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        
        // Create digital signature
        signAuditLog(auditLog, null);
        
        return auditLogDao.create(auditLog);
    }
    
    @Override
    public AuditLog recordAccountUnlock(User user, String ipAddress, User unlockedByUser, String reason) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        AuditLog auditLog = new AuditLog(AuditEventType.OTHER);
        auditLog.setUserId(user.getId());
        
        String description = "User account unlocked";
        if (unlockedByUser != null) {
            description += " by administrator";
            auditLog.setSignedBy(unlockedByUser.getId());
        } else {
            description += " automatically";
        }
        
        if (reason != null && !reason.isEmpty()) {
            description += " | Reason: " + reason;
            auditLog.setReason(reason);
        }
        
        auditLog.setEventDescription(description);
        auditLog.setIpAddress(ipAddress);
        auditLog.setTableName("user_lockouts");
        auditLog.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        
        // Create digital signature
        signAuditLog(auditLog, unlockedByUser);
        
        return auditLogDao.create(auditLog);
    }
    
    @Override
    public AuditLog recordSecurityEvent(AuditEventType eventType, User user, String ipAddress, 
                                       String description, String tableName, String recordId, 
                                       String reason) throws SQLException {        
        AuditLog auditLog = new AuditLog(eventType);
        
        if (user != null) {
            auditLog.setUserId(user.getId());
        }
        
        auditLog.setEventDescription(description);
        auditLog.setIpAddress(ipAddress);
        auditLog.setTableName(tableName);
        auditLog.setRecordId(recordId);
        auditLog.setReason(reason);
        auditLog.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        
        // Create digital signature
        signAuditLog(auditLog, null);
        
        return auditLogDao.create(auditLog);
    }
    
    @Override
    public AuditChangeLog recordChange(Integer auditLogId, String columnName, String oldValue, String newValue) throws SQLException {
        if (auditLogId == null) {
            throw new IllegalArgumentException("Audit log ID cannot be null");
        }
        if (columnName == null || columnName.trim().isEmpty()) {
            throw new IllegalArgumentException("Column name cannot be null or empty");
        }
        
        AuditChangeLog changeLog = new AuditChangeLog();
        changeLog.setAuditLogId(auditLogId);
        changeLog.setColumnName(columnName);
        changeLog.setOldValue(oldValue);
        changeLog.setNewValue(newValue);
        return auditChangeLogDao.create(changeLog);
    }
    
    @Override
    public AuditLog signAuditLog(AuditLog auditLog, User signingUser) throws SQLException {
        if (auditLog == null) {
            throw new IllegalArgumentException("Audit log cannot be null");
        }
        
        // Set the signing user if provided
        if (signingUser != null) {
            auditLog.setSignedBy(signingUser.getId());
        }
        
        // Create a digital signature based on the audit log content
        try {
            String contentToSign = createContentToSign(auditLog);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(contentToSign.getBytes(StandardCharsets.UTF_8));
            String signature = Base64.getEncoder().encodeToString(hash);
            auditLog.setDigitalSignature(signature);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, "Failed to create digital signature", e);
            throw new RuntimeException("Failed to create digital signature", e);
        }
        
        return auditLog;
    }
    
    @Override
    public boolean verifyAuditLogSignature(AuditLog auditLog) {
        if (auditLog == null || auditLog.getDigitalSignature() == null) {
            return false;
        }
        
        try {
            // Recreate the signature and compare with the stored one
            String contentToSign = createContentToSign(auditLog);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(contentToSign.getBytes(StandardCharsets.UTF_8));
            String calculatedSignature = Base64.getEncoder().encodeToString(hash);
            
            return calculatedSignature.equals(auditLog.getDigitalSignature());
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, "Failed to verify digital signature", e);
            return false;
        }
    }
    
    @Override
    public List<AuditLog> getAuditLogsForUser(Integer userId) throws SQLException {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        return auditLogDao.findByUserId(userId);
    }
    
    @Override
    public List<AuditLog> getAuditLogsByEventType(AuditEventType eventType) throws SQLException {
        if (eventType == null) {
            throw new IllegalArgumentException("Event type cannot be null");
        }
        
        return auditLogDao.findByEventType(eventType);
    }
    
    @Override
    public List<AuditLog> getAuditLogsByDateRange(String startDate, String endDate) throws SQLException {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        
        return auditLogDao.findByDateRange(startDate, endDate);
    }
    
    @Override
    public List<AuditLog> getAuditLogsForRecord(String tableName, String recordId) throws SQLException {
        if (tableName == null || recordId == null) {
            throw new IllegalArgumentException("Table name and record ID cannot be null");
        }
        
        return auditLogDao.findByTableNameAndRecordId(tableName, recordId);
    }
    
    @Override
    public List<AuditLog> getAuditLogsByGroupId(UUID groupId) throws SQLException {
        if (groupId == null) {
            throw new IllegalArgumentException("Group ID cannot be null");
        }
        
        return auditLogDao.findByGroupId(groupId);
    }
    
    @Override
    public Optional<AuditLog> getAuditLogById(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        
        return auditLogDao.findById(id);
    }
    
    /**
     * Creates a string representation of the audit log content for signing.
     * 
     * @param auditLog The audit log to create content for
     * @return A string representation of the audit log content
     */
    private String createContentToSign(AuditLog auditLog) {
        StringBuilder sb = new StringBuilder();
        
        // Include all relevant fields in the content to sign
        sb.append(auditLog.getUserId() != null ? auditLog.getUserId().toString() : "null");
        sb.append(auditLog.getEventType() != null ? auditLog.getEventType().toString() : "null");
        sb.append(auditLog.getEventDescription() != null ? auditLog.getEventDescription() : "null");
        sb.append(auditLog.getIpAddress() != null ? auditLog.getIpAddress() : "null");
        sb.append(auditLog.getSignedBy() != null ? auditLog.getSignedBy().toString() : "null");
        sb.append(auditLog.getReason() != null ? auditLog.getReason() : "null");
        sb.append(auditLog.getTableName() != null ? auditLog.getTableName() : "null");
        sb.append(auditLog.getRecordId() != null ? auditLog.getRecordId() : "null");
        sb.append(auditLog.getGroupId() != null ? auditLog.getGroupId().toString() : "null");
        sb.append(auditLog.getCreatedAt() != null ? auditLog.getCreatedAt().toString() : "null");
        
        return sb.toString();
    }
}