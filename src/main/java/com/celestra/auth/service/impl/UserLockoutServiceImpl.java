package com.celestra.auth.service.impl;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.celestra.auth.config.AuthConfigProvider;
import com.celestra.auth.config.AuthConfigurationManager;
import com.celestra.auth.service.UserLockoutService;
import com.celestra.auth.util.EmailUtil;
import com.celestra.dao.AuditLogDao;
import com.celestra.dao.UserDao;
import com.celestra.dao.UserLockoutDao;
import com.celestra.dao.UserSessionDao;
import com.celestra.dao.impl.AuditLogDaoImpl;
import com.celestra.dao.impl.UserDaoImpl;
import com.celestra.dao.impl.UserLockoutDaoImpl;
import com.celestra.dao.impl.UserSessionDaoImpl;
import com.celestra.enums.AuditEventType;
import com.celestra.model.AuditLog;
import com.celestra.model.User;
import com.celestra.model.UserLockout;

/**
 * Implementation of the UserLockoutService interface.
 */
public class UserLockoutServiceImpl implements UserLockoutService {
    
    private static final Logger LOGGER = Logger.getLogger(UserLockoutServiceImpl.class.getName());
    
    protected final UserLockoutDao userLockoutDao;
    protected final UserDao userDao;
    protected final UserSessionDao userSessionDao;
    protected final AuditLogDao auditLogDao;
    protected final AuthConfigProvider config;
    
    /**
     * Default constructor.
     * Initializes DAOs with default implementations.
     */
    public UserLockoutServiceImpl() {
        this(new UserLockoutDaoImpl(), new UserDaoImpl(), new UserSessionDaoImpl(), 
             new AuditLogDaoImpl(), AuthConfigurationManager.getInstance());
    }
    
    /**
     * Parameterized constructor for dependency injection.
     */
    public UserLockoutServiceImpl(UserLockoutDao userLockoutDao, UserDao userDao, 
                                 UserSessionDao userSessionDao, AuditLogDao auditLogDao, 
                                 AuthConfigProvider config) {
        this.userLockoutDao = userLockoutDao;
        this.userDao = userDao;
        this.userSessionDao = userSessionDao;
        this.auditLogDao = auditLogDao;
        this.config = config;
    }
    
    @Override
    public UserLockout lockAccount(Integer userId, int failedAttempts, String reason, String ipAddress) 
            throws SQLException {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        // Check if the user exists
        Optional<User> userOpt = userDao.findById(userId);
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }
        
        // Create the lockout record
        UserLockout lockout = new UserLockout();
        lockout.setUserId(userId);
        lockout.setFailedAttempts(failedAttempts);
        lockout.setReason(reason);
        lockout.setLockoutStart(new Timestamp(System.currentTimeMillis()));
        
        // Determine if this should be a permanent lockout based on history
        boolean shouldBePermanent = shouldLockoutBePermanent(userId);
        
        // Set lockout duration based on configuration and history
        if (shouldBePermanent || 
            (config.getLockoutPermanentAfterConsecutiveTempLockouts() > 0 && 
             getTemporaryLockoutCount(userId) >= config.getLockoutPermanentAfterConsecutiveTempLockouts() - 1)) {
            // Permanent lockout (null end time)
            lockout.setLockoutEnd(null);
            LOGGER.info("Creating permanent lockout for user ID: " + userId);
        } else {
            // Temporary lockout
            lockout.setLockoutEnd(Timestamp.from(
                Instant.now().plus(config.getLockoutDurationMinutes(), ChronoUnit.MINUTES)
            ));
            LOGGER.info("Creating temporary lockout for user ID: " + userId + 
                       " for " + config.getLockoutDurationMinutes() + " minutes");
        }
        
        // Save the lockout record
        UserLockout createdLockout = userLockoutDao.create(lockout);
        
        // Create audit log
        createAuditLog(userId, AuditEventType.OTHER, "Account locked: " + reason, 
                      ipAddress, "user_lockouts", createdLockout.getId().toString(), reason);
        
        // End all active sessions for the user
        endAllSessions(userId, "Account locked: " + reason);
        
        return createdLockout;
    }
    
    @Override
    public UserLockout lockAccountByEmail(String email, int failedAttempts, String reason, String ipAddress) 
            throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        
        // Normalize email
        email = EmailUtil.normalizeEmail(email);
        
        // Find the user by email
        Optional<User> userOpt = userDao.findByEmail(email);
        if (!userOpt.isPresent()) {
            LOGGER.warning("Attempted to lock non-existent account with email: " + email);
            return null;
        }
        
        return lockAccount(userOpt.get().getId(), failedAttempts, reason, ipAddress);
    }
    
    @Override
    public boolean isAccountLocked(Integer userId) throws SQLException {
        if (userId == null) {
            return false;
        }
        
        // Check if there's an active lockout for the user
        Optional<UserLockout> lockoutOpt = userLockoutDao.findActiveByUserId(userId);
        return lockoutOpt.isPresent() && lockoutOpt.get().isActive();
    }
    
    @Override
    public boolean isAccountLocked(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        // Normalize email
        email = EmailUtil.normalizeEmail(email);
        
        // Find the user by email
        Optional<User> userOpt = userDao.findByEmail(email);
        if (!userOpt.isPresent()) {
            return false;
        }
        
        return isAccountLocked(userOpt.get().getId());
    }
    
    @Override
    public Optional<UserLockout> getActiveLockout(Integer userId) throws SQLException {
        if (userId == null) {
            return Optional.empty();
        }
        
        Optional<UserLockout> lockoutOpt = userLockoutDao.findActiveByUserId(userId);
        if (lockoutOpt.isPresent() && lockoutOpt.get().isActive()) {
            return lockoutOpt;
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<UserLockout> getActiveLockoutByEmail(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        
        // Normalize email
        email = EmailUtil.normalizeEmail(email);
        
        // Find the user by email
        Optional<User> userOpt = userDao.findByEmail(email);
        if (!userOpt.isPresent()) {
            return Optional.empty();
        }
        
        return getActiveLockout(userOpt.get().getId());
    }
    
    @Override
    public List<UserLockout> getLockoutHistory(Integer userId) throws SQLException {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        return userLockoutDao.findByUserId(userId);
    }
    
    @Override
    public List<UserLockout> getLockoutHistoryByEmail(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        
        // Normalize email
        email = EmailUtil.normalizeEmail(email);
        
        // Find the user by email
        Optional<User> userOpt = userDao.findByEmail(email);
        if (!userOpt.isPresent()) {
            return List.of(); // Return empty list if user not found
        }
        
        return getLockoutHistory(userOpt.get().getId());
    }
    
    @Override
    public boolean unlockAccount(Integer userId, String reason, Integer adminUserId, String ipAddress) 
            throws SQLException {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        // Check if the account is locked
        Optional<UserLockout> lockoutOpt = userLockoutDao.findActiveByUserId(userId);
        if (!lockoutOpt.isPresent() || !lockoutOpt.get().isActive()) {
            LOGGER.info("Attempted to unlock account that is not locked. User ID: " + userId);
            return false;
        }
        
        UserLockout lockout = lockoutOpt.get();
        
        // Set the lockout to expire immediately
        Timestamp now = new Timestamp(System.currentTimeMillis());
        boolean updated = userLockoutDao.updateLockoutEnd(lockout.getId(), now);
        
        if (updated) {
            // Create audit log
            String description = "Account unlocked: " + reason;
            createAuditLog(userId, AuditEventType.OTHER, description, 
                          ipAddress, "user_lockouts", lockout.getId().toString(), reason);
            
            // If admin user is provided, create another audit log for the admin action
            if (adminUserId != null) {
                String adminDescription = "Admin unlocked account for user ID: " + userId + " - " + reason;
                createAuditLog(adminUserId, AuditEventType.OTHER, adminDescription, 
                              ipAddress, "user_lockouts", lockout.getId().toString(), reason);
            }
            
            LOGGER.info("Account unlocked for user ID: " + userId);
        }
        
        return updated;
    }
    
    @Override
    public boolean unlockAccountByEmail(String email, String reason, Integer adminUserId, String ipAddress) 
            throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        
        // Normalize email
        email = EmailUtil.normalizeEmail(email);
        
        // Find the user by email
        Optional<User> userOpt = userDao.findByEmail(email);
        if (!userOpt.isPresent()) {
            LOGGER.warning("Attempted to unlock non-existent account with email: " + email);
            return false;
        }
        
        return unlockAccount(userOpt.get().getId(), reason, adminUserId, ipAddress);
    }
    
    @Override
    public boolean extendLockout(Integer userId, int additionalMinutes, String reason, Integer adminUserId, String ipAddress) 
            throws SQLException {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        if (additionalMinutes <= 0) {
            throw new IllegalArgumentException("Additional minutes must be positive");
        }
        
        // Check if the account is locked
        Optional<UserLockout> lockoutOpt = userLockoutDao.findActiveByUserId(userId);
        if (!lockoutOpt.isPresent() || !lockoutOpt.get().isActive()) {
            LOGGER.info("Attempted to extend lockout for account that is not locked. User ID: " + userId);
            return false;
        }
        
        UserLockout lockout = lockoutOpt.get();
        
        // If it's a permanent lockout, it can't be extended
        if (lockout.isPermanent()) {
            LOGGER.info("Attempted to extend permanent lockout. User ID: " + userId);
            return false;
        }
        
        // Calculate new end time
        Timestamp newEndTime = Timestamp.from(
            lockout.getLockoutEnd().toInstant().plus(additionalMinutes, ChronoUnit.MINUTES)
        );
        
        // Update the lockout end time
        boolean updated = userLockoutDao.updateLockoutEnd(lockout.getId(), newEndTime);
        
        if (updated) {
            // Create audit log
            String description = "Lockout extended by " + additionalMinutes + " minutes: " + reason;
            createAuditLog(userId, AuditEventType.OTHER, description, 
                          ipAddress, "user_lockouts", lockout.getId().toString(), reason);
            
            // If admin user is provided, create another audit log for the admin action
            if (adminUserId != null) {
                String adminDescription = "Admin extended lockout for user ID: " + userId + 
                                         " by " + additionalMinutes + " minutes - " + reason;
                createAuditLog(adminUserId, AuditEventType.OTHER, adminDescription, 
                              ipAddress, "user_lockouts", lockout.getId().toString(), reason);
            }
            
            LOGGER.info("Lockout extended for user ID: " + userId + " by " + additionalMinutes + " minutes");
        }
        
        return updated;
    }
    
    @Override
    public boolean makeLockoutPermanent(Integer userId, String reason, Integer adminUserId, String ipAddress) 
            throws SQLException {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        // Check if the account is locked
        Optional<UserLockout> lockoutOpt = userLockoutDao.findActiveByUserId(userId);
        if (!lockoutOpt.isPresent() || !lockoutOpt.get().isActive()) {
            LOGGER.info("Attempted to make permanent lockout for account that is not locked. User ID: " + userId);
            return false;
        }
        
        UserLockout lockout = lockoutOpt.get();
        
        // If it's already a permanent lockout, nothing to do
        if (lockout.isPermanent()) {
            LOGGER.info("Account is already permanently locked. User ID: " + userId);
            return true;
        }
        
        // Update the lockout to be permanent (null end time)
        boolean updated = userLockoutDao.updateLockoutEnd(lockout.getId(), null);
        
        if (updated) {
            // Create audit log
            String description = "Lockout made permanent: " + reason;
            createAuditLog(userId, AuditEventType.OTHER, description, 
                          ipAddress, "user_lockouts", lockout.getId().toString(), reason);
            
            // If admin user is provided, create another audit log for the admin action
            if (adminUserId != null) {
                String adminDescription = "Admin made lockout permanent for user ID: " + userId + " - " + reason;
                createAuditLog(adminUserId, AuditEventType.OTHER, adminDescription, 
                              ipAddress, "user_lockouts", lockout.getId().toString(), reason);
            }
            
            LOGGER.info("Lockout made permanent for user ID: " + userId);
        }
        
        return updated;
    }
    
    @Override
    public List<UserLockout> getAllActiveLockouts() throws SQLException {
        return userLockoutDao.findAllActive();
    }
    
    @Override
    public List<UserLockout> getAllPermanentLockouts() throws SQLException {
        return userLockoutDao.findAllPermanent();
    }
    
    @Override
    public List<UserLockout> getAllTemporaryLockouts() throws SQLException {
        return userLockoutDao.findAllTemporary();
    }
    
    @Override
    public int cleanupExpiredLockouts() throws SQLException {
        int count = userLockoutDao.deleteExpired();
        LOGGER.info("Cleaned up " + count + " expired lockouts");
        return count;
    }
    
    @Override
    public boolean shouldLockoutBePermanent(Integer userId) throws SQLException {
        if (userId == null) {
            return false;
        }
        
        // Get the number of temporary lockouts
        int temporaryLockoutCount = getTemporaryLockoutCount(userId);
        
        // Check if we've reached the threshold for permanent lockout
        int threshold = config.getLockoutPermanentAfterConsecutiveTempLockouts();
        if (threshold > 0 && temporaryLockoutCount >= threshold) {
            LOGGER.info("User ID: " + userId + " has reached the threshold for permanent lockout. " +
                       "Temporary lockout count: " + temporaryLockoutCount + ", Threshold: " + threshold);
            return true;
        }
        
        return false;
    }
    
    @Override
    public int getTemporaryLockoutCount(Integer userId) throws SQLException {
        if (userId == null) {
            return 0;
        }
        
        // Get all lockouts for the user
        List<UserLockout> lockouts = userLockoutDao.findByUserId(userId);
        
        // Count temporary lockouts
        return (int) lockouts.stream()
                .filter(lockout -> !lockout.isPermanent())
                .count();
    }
    
    /**
     * End all active sessions for a user.
     * 
     * @param userId The ID of the user
     * @param reason The reason for ending the sessions
     * @return The number of sessions ended
     * @throws SQLException if a database error occurs
     */
    protected int endAllSessions(Integer userId, String reason) throws SQLException {
        if (userId == null) {
            return 0;
        }
        
        int count = 0;
        Timestamp now = new Timestamp(System.currentTimeMillis());
        
        // Get all active sessions for the user
        for (var session : userSessionDao.findActiveByUserId(userId)) {
            // Set the session to expire immediately
            boolean updated = userSessionDao.updateExpiresAt(session.getId(), now);
            
            if (updated) {
                // Create audit log
                createAuditLog(userId, AuditEventType.SESSION_ENDED, "Session ended: " + reason, 
                              session.getIpAddress(), "user_sessions", session.getId().toString(), reason);
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Create an audit log entry.
     */
    protected void createAuditLog(Integer userId, AuditEventType eventType, String eventDescription, 
                                String ipAddress, String tableName, String recordId, String reason) {
        try {
            AuditLog auditLog = new AuditLog(eventType);
            auditLog.setUserId(userId);
            auditLog.setEventDescription(eventDescription);
            auditLog.setIpAddress(ipAddress);
            auditLog.setTableName(tableName);
            auditLog.setRecordId(recordId);
            auditLog.setReason(reason);
            auditLog.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            
            auditLogDao.create(auditLog);
            
            LOGGER.info("Audit log created: " + eventDescription);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating audit log: " + eventDescription, e);
        }
    }
}