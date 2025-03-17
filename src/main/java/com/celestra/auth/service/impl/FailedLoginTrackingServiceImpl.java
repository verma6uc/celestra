package com.celestra.auth.service.impl;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.celestra.auth.config.AuthConfigProvider;
import com.celestra.auth.config.AuthConfigurationManager;
import com.celestra.auth.service.FailedLoginTrackingService;
import com.celestra.dao.AuditLogDao;
import com.celestra.dao.FailedLoginDao;
import com.celestra.dao.UserDao;
import com.celestra.dao.impl.AuditLogDaoImpl;
import com.celestra.dao.impl.FailedLoginDaoImpl;
import com.celestra.dao.impl.UserDaoImpl;
import com.celestra.enums.AuditEventType;
import com.celestra.model.AuditLog;
import com.celestra.model.FailedLogin;
import com.celestra.model.User;

/**
 * Implementation of the FailedLoginTrackingService interface.
 */
public class FailedLoginTrackingServiceImpl implements FailedLoginTrackingService {
    
    private static final Logger LOGGER = Logger.getLogger(FailedLoginTrackingServiceImpl.class.getName());
    
    private final FailedLoginDao failedLoginDao;
    private final UserDao userDao;
    private final AuditLogDao auditLogDao;
    private final AuthConfigProvider authConfig;
    
    /**
     * Default constructor.
     */
    public FailedLoginTrackingServiceImpl() {
        this.failedLoginDao = new FailedLoginDaoImpl();
        this.userDao = new UserDaoImpl();
        this.auditLogDao = new AuditLogDaoImpl();
        this.authConfig = AuthConfigurationManager.getInstance();
    }
    
    /**
     * Constructor with dependencies for testing.
     */
    public FailedLoginTrackingServiceImpl(FailedLoginDao failedLoginDao, UserDao userDao, 
            AuditLogDao auditLogDao, AuthConfigProvider authConfig) {
        this.failedLoginDao = failedLoginDao;
        this.userDao = userDao;
        this.auditLogDao = auditLogDao;
        this.authConfig = authConfig;
    }
    
    @Override
    public FailedLogin recordFailedLogin(String email, String ipAddress, String failureReason, 
            Map<String, String> metadata) throws SQLException {
        
        // Create a new failed login record
        FailedLogin failedLogin = new FailedLogin();
        failedLogin.setEmail(email);
        failedLogin.setIpAddress(ipAddress);
        failedLogin.setFailureReason(failureReason);
        failedLogin.setAttemptedAt(new Timestamp(System.currentTimeMillis()));
        
        // Try to find the user by email
        try {
            userDao.findByEmail(email).ifPresent(user -> {
                failedLogin.setUserId(user.getId());
            });
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error finding user by email: " + email, e);
            // Continue without setting user ID
        }
        
        // Save the failed login record
        FailedLogin savedFailedLogin = failedLoginDao.create(failedLogin);
        
        // Create audit log
        createAuditLog(failedLogin.getUserId(), ipAddress, "Failed login attempt: " + failureReason, 
                email, metadata);
        
        return savedFailedLogin;
    }
    
    @Override
    public FailedLogin recordFailedLogin(User user, String ipAddress, String failureReason, 
            Map<String, String> metadata) throws SQLException {
        
        // Create a new failed login record
        FailedLogin failedLogin = new FailedLogin();
        failedLogin.setUserId(user.getId());
        failedLogin.setEmail(user.getEmail());
        failedLogin.setIpAddress(ipAddress);
        failedLogin.setFailureReason(failureReason);
        failedLogin.setAttemptedAt(new Timestamp(System.currentTimeMillis()));
        
        // Save the failed login record
        FailedLogin savedFailedLogin = failedLoginDao.create(failedLogin);
        
        // Create audit log
        createAuditLog(user.getId(), ipAddress, "Failed login attempt: " + failureReason, 
                user.getEmail(), metadata);
        
        return savedFailedLogin;
    }
    
    @Override
    public int getRecentFailedLoginCount(String email, int windowMinutes) throws SQLException {
        return failedLoginDao.countRecentByEmail(email, windowMinutes);
    }
    
    @Override
    public int getRecentFailedLoginCountByIp(String ipAddress, int windowMinutes) throws SQLException {
        return failedLoginDao.countRecentByIpAddress(ipAddress, windowMinutes);
    }
    
    @Override
    public List<FailedLogin> getRecentFailedLogins(String email, int windowMinutes) throws SQLException {
        return failedLoginDao.findRecentByEmail(email, windowMinutes);
    }
    
    @Override
    public List<FailedLogin> getRecentFailedLoginsByIp(String ipAddress, int windowMinutes) throws SQLException {
        // The FailedLoginDao doesn't have a findRecentByIpAddress method, so we'll use findByIpAddress
        // and filter the results in memory
        List<FailedLogin> allFailedLogins = failedLoginDao.findByIpAddress(ipAddress);
        
        // Calculate the cutoff time
        long cutoffTime = System.currentTimeMillis() - (windowMinutes * 60 * 1000L);
        
        // Filter the results
        return allFailedLogins.stream()
                .filter(login -> login.getAttemptedAt().getTime() >= cutoffTime)
                .toList();
    }
    
    @Override
    public boolean isFailedLoginThresholdExceeded(String email) throws SQLException {
        int lockoutWindowMinutes = authConfig.getLockoutWindowMinutes();
        int lockoutMaxAttempts = authConfig.getLockoutMaxAttempts();
        
        int recentFailedLogins = getRecentFailedLoginCount(email, lockoutWindowMinutes);
        
        return recentFailedLogins >= lockoutMaxAttempts;
    }
    
    @Override
    public boolean isFailedLoginThresholdExceededByIp(String ipAddress) throws SQLException {
        int lockoutWindowMinutes = authConfig.getLockoutWindowMinutes();
        int lockoutMaxAttempts = authConfig.getLockoutMaxAttempts();
        
        int recentFailedLogins = getRecentFailedLoginCountByIp(ipAddress, lockoutWindowMinutes);
        
        return recentFailedLogins >= lockoutMaxAttempts;
    }
    
    @Override
    public int cleanupOldRecords(int olderThanDays) throws SQLException {
        return failedLoginDao.deleteOlderThan(olderThanDays);
    }
    
    /**
     * Create an audit log for failed login attempts.
     * 
     * @param userId The ID of the user (may be null)
     * @param ipAddress The IP address of the client
     * @param description The description of the action
     * @param email The email address used in the login attempt
     * @param metadata Additional metadata about the action
     * @throws SQLException if a database error occurs
     */
    private void createAuditLog(Integer userId, String ipAddress, String description, 
            String email, Map<String, String> metadata) throws SQLException {
        
        // Skip audit logging if it's disabled
        if (!authConfig.isAuditLogFailedLoginsEnabled()) {
            return;
        }
        
        Map<String, String> auditMetadata = new HashMap<>();
        if (metadata != null) {
            auditMetadata.putAll(metadata);
        }
        
        // Add email to metadata if not null
        if (email != null) {
            auditMetadata.put("email", email);
        }
        
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(userId);
        auditLog.setEventType(AuditEventType.FAILED_LOGIN);
        auditLog.setEventDescription(description);
        auditLog.setIpAddress(ipAddress);
        auditLog.setReason("Failed login tracking");
        auditLog.setTableName("users");
        auditLog.setRecordId(userId != null ? userId.toString() : "unknown");
        auditLog.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        
        auditLogDao.create(auditLog);
    }
}