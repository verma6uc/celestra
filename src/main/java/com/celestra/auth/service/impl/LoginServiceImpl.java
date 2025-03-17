package com.celestra.auth.service.impl;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.celestra.auth.config.AuthConfigProvider;
import com.celestra.auth.config.AuthConfigurationManager;
import com.celestra.auth.service.LoginService;
import com.celestra.auth.util.EmailUtil;
import com.celestra.auth.util.PasswordUtil;
import com.celestra.dao.AuditLogDao;
import com.celestra.dao.FailedLoginDao;
import com.celestra.dao.UserDao;
import com.celestra.dao.UserLockoutDao;
import com.celestra.dao.UserSessionDao;
import com.celestra.dao.impl.AuditLogDaoImpl;
import com.celestra.dao.impl.FailedLoginDaoImpl;
import com.celestra.dao.impl.UserDaoImpl;
import com.celestra.dao.impl.UserLockoutDaoImpl;
import com.celestra.dao.impl.UserSessionDaoImpl;
import com.celestra.db.TransactionUtil;
import com.celestra.enums.AuditEventType;
import com.celestra.enums.UserStatus;
import com.celestra.model.AuditLog;
import com.celestra.model.FailedLogin;
import com.celestra.model.User;
import com.celestra.model.UserLockout;
import com.celestra.model.UserSession;

/**
 * Implementation of the LoginService interface.
 */
public class LoginServiceImpl implements LoginService {
    
    private static final Logger LOGGER = Logger.getLogger(LoginServiceImpl.class.getName());
    private static final SecureRandom RANDOM = new SecureRandom();
    
    private final UserDao userDao;
    private final UserSessionDao userSessionDao;
    private final FailedLoginDao failedLoginDao;
    private final UserLockoutDao userLockoutDao;
    private final AuditLogDao auditLogDao;
    private final AuthConfigProvider config;
    
    /**
     * Default constructor.
     * Initializes DAOs with default implementations.
     */
    public LoginServiceImpl() {
        this(new UserDaoImpl(), new UserSessionDaoImpl(), new FailedLoginDaoImpl(), 
             new UserLockoutDaoImpl(), new AuditLogDaoImpl(), AuthConfigurationManager.getInstance());
    }
    
    /**
     * Parameterized constructor for dependency injection.
     */
    public LoginServiceImpl(UserDao userDao, UserSessionDao userSessionDao, FailedLoginDao failedLoginDao,
                           UserLockoutDao userLockoutDao, AuditLogDao auditLogDao, AuthConfigProvider config) {
        this.userDao = userDao;
        this.userSessionDao = userSessionDao;
        this.failedLoginDao = failedLoginDao;
        this.userLockoutDao = userLockoutDao;
        this.auditLogDao = auditLogDao;
        this.config = config;
    }
    
    @Override
    public Optional<User> authenticate(String email, String password, String ipAddress, Map<String, String> metadata) 
            throws SQLException {
        
        // Validate input
        if (email == null || email.trim().isEmpty()) {
            recordFailedLogin(null, ipAddress, "Email is required", metadata);
            return Optional.empty();
        }
        
        if (password == null || password.trim().isEmpty()) {
            recordFailedLogin(email, ipAddress, "Password is required", metadata);
            return Optional.empty();
        }
        
        // Normalize email
        email = EmailUtil.normalizeEmail(email);
        
        // Find the user by email
        Optional<User> userOpt = userDao.findByEmail(email);
        if (!userOpt.isPresent()) {
            recordFailedLogin(email, ipAddress, "User not found", metadata);
            return Optional.empty();
        }
        
        User user = userOpt.get();
        
        // Check if the account is locked
        if (isAccountLocked(user.getId())) {
            recordFailedLogin(email, ipAddress, "Account is locked", metadata);
            createAuditLog(user.getId(), AuditEventType.FAILED_LOGIN, "Login attempt on locked account", ipAddress, "users", user.getId().toString(), null);
            return Optional.empty();
        }
        
        // Check if the account is active
        if (user.getStatus() != UserStatus.ACTIVE) {
            recordFailedLogin(email, ipAddress, "Account is not active", metadata);
            createAuditLog(user.getId(), AuditEventType.FAILED_LOGIN, "Login attempt on inactive account", ipAddress, "users", user.getId().toString(), null);
            return Optional.empty();
        }
        
        // Verify the password
        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            recordFailedLogin(email, ipAddress, "Invalid password", metadata);
            
            // Check if we need to lock the account
            int failedAttempts = getRecentFailedLoginCount(user.getId(), config.getLockoutWindowMinutes());
            if (failedAttempts >= config.getLockoutMaxAttempts()) {
                lockAccount(user.getId(), failedAttempts, "Too many failed login attempts", ipAddress);
            }
            
            createAuditLog(user.getId(), AuditEventType.FAILED_LOGIN, "Invalid password", ipAddress, "users", user.getId().toString(), null);
            return Optional.empty();
        }
        
        // Authentication successful
        createAuditLog(user.getId(), AuditEventType.SUCCESSFUL_LOGIN, "Successful login", ipAddress, "users", user.getId().toString(), null);
        return Optional.of(user);
    }
    
    @Override
    public UserSession createSession(Integer userId, String ipAddress, String userAgent, Map<String, String> metadata) 
            throws SQLException {
        
        // Validate input
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        // Generate a secure session token
        String sessionToken = generateSessionToken();
        
        // Calculate expiration time
        Timestamp expiresAt = Timestamp.from(
            Instant.now().plus(config.getSessionExpirationMinutes(), ChronoUnit.MINUTES)
        );
        
        // Create the session
        UserSession session = new UserSession();
        session.setUserId(userId);
        session.setSessionToken(sessionToken);
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        session.setExpiresAt(expiresAt);
        
        // Save the session
        UserSession createdSession = userSessionDao.create(session);
        
        // Create audit log
        createAuditLog(userId, AuditEventType.SESSION_STARTED, "Session created", ipAddress, "user_sessions", createdSession.getId().toString(), null);
        
        return createdSession;
    }
    
    @Override
    public Optional<UserSession> validateSession(String sessionToken) throws SQLException {
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            return Optional.empty();
        }
        
        // Find the session
        Optional<UserSession> sessionOpt = userSessionDao.findBySessionToken(sessionToken);
        if (!sessionOpt.isPresent()) {
            return Optional.empty();
        }
        
        UserSession session = sessionOpt.get();
        
        // Check if the session is expired
        if (session.isExpired()) {
            return Optional.empty();
        }
        
        // Check if the user is locked
        if (isAccountLocked(session.getUserId())) {
            return Optional.empty();
        }
        
        // Check if the user is active
        Optional<User> userOpt = userDao.findById(session.getUserId());
        if (!userOpt.isPresent() || userOpt.get().getStatus() != UserStatus.ACTIVE) {
            return Optional.empty();
        }
        
        // Session is valid
        return Optional.of(session);
    }
    
    @Override
    public boolean endSession(String sessionToken, String reason) throws SQLException {
        if (sessionToken == null || sessionToken.trim().isEmpty()) {
            return false;
        }
        
        // Find the session
        Optional<UserSession> sessionOpt = userSessionDao.findBySessionToken(sessionToken);
        if (!sessionOpt.isPresent()) {
            return false;
        }
        
        UserSession session = sessionOpt.get();
        
        // Set the session to expire immediately
        Timestamp now = new Timestamp(System.currentTimeMillis());
        boolean updated = userSessionDao.updateExpiresAt(session.getId(), now);
        
        if (updated) {
            // Create audit log
            createAuditLog(session.getUserId(), AuditEventType.SESSION_ENDED, "Session ended: " + reason, 
                          session.getIpAddress(), "user_sessions", session.getId().toString(), reason);
        }
        
        return updated;
    }
    
    @Override
    public int endAllSessions(Integer userId, String reason) throws SQLException {
        if (userId == null) {
            return 0;
        }
        
        // Get all active sessions for the user
        int count = 0;
        for (UserSession session : userSessionDao.findActiveByUserId(userId)) {
            if (endSession(session.getSessionToken(), reason)) {
                count++;
            }
        }
        
        return count;
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
    public void recordFailedLogin(String email, String ipAddress, String reason, Map<String, String> metadata) 
            throws SQLException {
        
        // Create the failed login record
        FailedLogin failedLogin = new FailedLogin();
        failedLogin.setEmail(email);
        failedLogin.setIpAddress(ipAddress);
        failedLogin.setFailureReason(reason);
        failedLogin.setAttemptedAt(new Timestamp(System.currentTimeMillis()));
        
        // If email is provided, try to find the user ID
        if (email != null && !email.trim().isEmpty()) {
            Optional<User> userOpt = userDao.findByEmail(EmailUtil.normalizeEmail(email));
            if (userOpt.isPresent()) {
                failedLogin.setUserId(userOpt.get().getId());
            }
        }
        
        // Save the failed login record
        failedLoginDao.create(failedLogin);
    }
    
    @Override
    public int getRecentFailedLoginCount(Integer userId, int minutes) throws SQLException {
        if (userId == null) {
            return 0;
        }
        
        // Find the user's email
        Optional<User> userOpt = userDao.findById(userId);
        if (!userOpt.isPresent()) {
            return 0;
        }
        
        return failedLoginDao.countRecentByEmail(userOpt.get().getEmail(), minutes);
    }
    
    @Override
    public int getRecentFailedLoginCount(String ipAddress, int minutes) throws SQLException {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return 0;
        }
        
        return failedLoginDao.countRecentByIpAddress(ipAddress, minutes);
    }
    
    /**
     * Lock a user account due to security concerns.
     * 
     * @param userId The ID of the user to lock
     * @param failedAttempts The number of failed login attempts
     * @param reason The reason for the lockout
     * @param ipAddress The IP address that triggered the lockout
     * @throws SQLException if a database error occurs
     */
    private void lockAccount(Integer userId, int failedAttempts, String reason, String ipAddress) throws SQLException {
        // Create the lockout record
        UserLockout lockout = new UserLockout();
        lockout.setUserId(userId);
        lockout.setFailedAttempts(failedAttempts);
        lockout.setReason(reason);
        lockout.setLockoutStart(new Timestamp(System.currentTimeMillis()));
        
        // Set lockout duration based on configuration
        if (config.getLockoutPermanentAfterConsecutiveTempLockouts() > 0 && failedAttempts >= config.getLockoutPermanentAfterConsecutiveTempLockouts()) {
            // Permanent lockout (null end time)
            lockout.setLockoutEnd(null);
        } else {
            // Temporary lockout
            lockout.setLockoutEnd(Timestamp.from(
                Instant.now().plus(config.getLockoutDurationMinutes(), ChronoUnit.MINUTES)
            ));
        }
        
        // Save the lockout record
        UserLockout createdLockout = userLockoutDao.create(lockout);
        
        // Create audit log
        createAuditLog(userId, AuditEventType.OTHER, "Account locked: " + reason, 
                      ipAddress, "user_lockouts", createdLockout.getId().toString(), reason);
        
        // End all active sessions for the user
        endAllSessions(userId, "Account locked: " + reason);
    }
    
    /**
     * Create an audit log entry.
     */
    private void createAuditLog(Integer userId, AuditEventType eventType, String eventDescription, 
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
    
    /**
     * Generate a secure random session token.
     * 
     * @return A random session token
     */
    private String generateSessionToken() {
        byte[] randomBytes = new byte[32];
        RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}