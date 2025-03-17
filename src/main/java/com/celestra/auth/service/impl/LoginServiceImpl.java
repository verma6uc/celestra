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
import com.celestra.auth.service.AuditService;
import com.celestra.auth.service.FailedLoginTrackingService;
import com.celestra.auth.service.LoginService;
import com.celestra.auth.service.UserLockoutService;
import com.celestra.auth.util.EmailUtil;
import com.celestra.auth.util.PasswordUtil;
import com.celestra.dao.AuditLogDao;
import com.celestra.dao.FailedLoginDao;
import com.celestra.dao.CompanyDao;
import com.celestra.dao.UserDao;
import com.celestra.dao.UserLockoutDao;
import com.celestra.dao.UserSessionDao;
import com.celestra.dao.impl.AuditLogDaoImpl;
import com.celestra.auth.service.impl.AuditServiceImpl;
import com.celestra.dao.impl.FailedLoginDaoImpl;
import com.celestra.dao.impl.CompanyDaoImpl;
import com.celestra.dao.impl.UserDaoImpl;
import com.celestra.dao.impl.UserLockoutDaoImpl;
import com.celestra.auth.service.impl.FailedLoginTrackingServiceImpl;
import com.celestra.auth.service.impl.UserLockoutServiceImpl;
import com.celestra.dao.impl.UserSessionDaoImpl;
import com.celestra.db.TransactionUtil;
import com.celestra.enums.AuditEventType;
import com.celestra.enums.CompanyStatus;
import com.celestra.enums.UserStatus;
import com.celestra.model.AuditLog;
import com.celestra.model.Company;
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
    
    protected final UserDao userDao;
    protected final UserSessionDao userSessionDao;
    protected final FailedLoginTrackingService failedLoginTrackingService;
    protected final UserLockoutService userLockoutService;
    protected final FailedLoginDao failedLoginDao; // Kept for backward compatibility
    protected final UserLockoutDao userLockoutDao;
    protected final CompanyDao companyDao;
    protected final AuditLogDao auditLogDao;
    protected final AuditService auditService;
    protected final AuthConfigProvider config;
    
    /**
     * Default constructor.
     * Initializes DAOs with default implementations.
     */
    public LoginServiceImpl() {
        this.userDao = new UserDaoImpl();
        this.userSessionDao = new UserSessionDaoImpl();
        FailedLoginDao failedLoginDao = new FailedLoginDaoImpl();
        this.failedLoginDao = failedLoginDao;
        this.userLockoutDao = new UserLockoutDaoImpl();
        this.companyDao = new CompanyDaoImpl();
        this.auditLogDao = new AuditLogDaoImpl();
        this.auditService = new AuditServiceImpl(this.auditLogDao);
        this.config = AuthConfigurationManager.getInstance();
        this.failedLoginTrackingService = new FailedLoginTrackingServiceImpl(
            failedLoginDao, this.userDao, this.auditLogDao, this.config);
        this.userLockoutService = new UserLockoutServiceImpl(this.userLockoutDao, this.userDao, 
            this.userSessionDao, this.auditLogDao, this.config);
        // Other initializations will be done in the parameterized constructor
    }
    
    /**
     * Parameterized constructor for dependency injection.
     */
    public LoginServiceImpl(UserDao userDao, UserSessionDao userSessionDao, FailedLoginDao failedLoginDao,
                           UserLockoutDao userLockoutDao, CompanyDao companyDao, AuditLogDao auditLogDao,
                            AuditService auditService,
                           AuthConfigProvider config) {
        this(userDao, userSessionDao, 
             new FailedLoginTrackingServiceImpl(failedLoginDao, userDao, auditLogDao, config),
             new UserLockoutServiceImpl(userLockoutDao, userDao, userSessionDao, auditLogDao, config),
             failedLoginDao, userLockoutDao, companyDao, auditLogDao, auditService, config);
    }
    
    public LoginServiceImpl(UserDao userDao, UserSessionDao userSessionDao, FailedLoginTrackingService failedLoginTrackingService,
                           UserLockoutService userLockoutService, FailedLoginDao failedLoginDao, UserLockoutDao userLockoutDao, 
                           CompanyDao companyDao, AuditLogDao auditLogDao, AuditService auditService, AuthConfigProvider config) {
        this.userDao = userDao;
        this.userSessionDao = userSessionDao;
        this.failedLoginTrackingService = failedLoginTrackingService;
        this.userLockoutService = userLockoutService;
        this.failedLoginDao = failedLoginDao;
        this.userLockoutDao = userLockoutDao;
        this.companyDao = companyDao;
        this.auditLogDao = auditLogDao;
        this.auditService = auditService != null ? auditService : new AuditServiceImpl(auditLogDao);
        this.config = config;
    }
    
    @Override
    public Optional<User> authenticate(String email, String password, String ipAddress, Map<String, String> metadata) 
            throws SQLException {
        
        // Validate input
        if (email == null || email.trim().isEmpty()) {
            failedLoginTrackingService.recordFailedLogin((String)null, ipAddress, "Email is required", metadata);
            return Optional.empty();
        }
        
        if (password == null || password.trim().isEmpty()) {
            failedLoginTrackingService.recordFailedLogin((String)email, ipAddress, "Password is required", metadata);
            return Optional.empty();
        }
        
        // Normalize email
        email = EmailUtil.normalizeEmail(email);
        
        // Find the user by email
        Optional<User> userOpt = userDao.findByEmail(email);
        if (!userOpt.isPresent()) {
            failedLoginTrackingService.recordFailedLogin((String)email, ipAddress, "User not found", metadata);
            return Optional.empty();
        }
        
        User user = userOpt.get();
        
        // Check if the account is locked
        if (userLockoutService.isAccountLocked(user.getId())) {
            failedLoginTrackingService.recordFailedLogin(user, ipAddress, "Account is locked", metadata);
            auditService.recordFailedLogin(user, user.getEmail(), ipAddress, "Account is locked");
            return Optional.empty();
        }
        
        // Check if the account is active
        if (user.getStatus() != UserStatus.ACTIVE) {
            failedLoginTrackingService.recordFailedLogin(user, ipAddress, "Account is not active", metadata);
            auditService.recordFailedLogin(user, user.getEmail(), ipAddress, "Account is not active");
            return Optional.empty();
        }
        
        // If the user is not a super admin, check if their company is active
        if (!user.isSuperAdmin() && user.getCompanyId() != null) {
            Optional<Company> companyOpt = companyDao.findById(user.getCompanyId());
            
            // If company doesn't exist or is not active, deny login
            if (!companyOpt.isPresent() || companyOpt.get().getStatus() != CompanyStatus.ACTIVE) {
                String reason = !companyOpt.isPresent() ? 
                    "Company not found" : 
                    "Company is not active";
                
                failedLoginTrackingService.recordFailedLogin(user, ipAddress, reason, metadata);
                auditService.recordFailedLogin(user, user.getEmail(), ipAddress, reason);
                return Optional.empty();
            }
        }
        
        // Verify the password
        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            failedLoginTrackingService.recordFailedLogin(user, ipAddress, "Invalid password", metadata);
            
            // Check if we need to lock the account
            if (failedLoginTrackingService.isFailedLoginThresholdExceeded(email)) {
                int failedAttempts = failedLoginTrackingService.getRecentFailedLoginCount(email, config.getLockoutWindowMinutes());
                userLockoutService.lockAccount(user.getId(), failedAttempts, "Too many failed login attempts", ipAddress);
            }
            
            auditService.recordFailedLogin(user, user.getEmail(), ipAddress, "Invalid password");
            return Optional.empty();
        }
        
        // Authentication successful
        auditService.recordSuccessfulLogin(user, ipAddress);
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
        auditService.recordSecurityEvent(AuditEventType.SESSION_STARTED, userDao.findById(userId).orElse(null), ipAddress, "Session created", "user_sessions", createdSession.getId().toString(), null);
        
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
        if (userLockoutService.isAccountLocked(session.getUserId())) {
            return Optional.empty();
        }
        
        // Check if the user is active
        Optional<User> userOpt = userDao.findById(session.getUserId());
        if (!userOpt.isPresent() || userOpt.get().getStatus() != UserStatus.ACTIVE) {
            return Optional.empty();
        }

        User user = userOpt.get();
        
        // If the user is not a super admin, check if their company is active
        if (!user.isSuperAdmin() && user.getCompanyId() != null) {
            Optional<Company> companyOpt = companyDao.findById(user.getCompanyId());
            
            // If company doesn't exist or is not active, invalidate session
            if (!companyOpt.isPresent() || companyOpt.get().getStatus() != CompanyStatus.ACTIVE) {
                // End the session
                endSession(sessionToken, "Company is not active");
                
                // Return empty to indicate invalid session
                return Optional.empty();
            }
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
           Optional<User> userOpt = userDao.findById(session.getUserId());
            if (userOpt.isPresent()) {
                auditService.recordLogout(userOpt.get(), session.getIpAddress(), session.getId().toString());
            }
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
            throw new IllegalArgumentException("User ID is required");
        }
        
        return userLockoutService.isAccountLocked(userId);
    }
    
    @Override
    public boolean isAccountLocked(String email) throws SQLException {
        return userLockoutService.isAccountLocked(email);
    }
    
    @Override
    public void recordFailedLogin(String email, String ipAddress, String reason, Map<String, String> metadata) 
            throws SQLException {
        // Delegate to the FailedLoginTrackingService
        failedLoginTrackingService.recordFailedLogin(email, ipAddress, reason, metadata);
    }
    
    @Override
    public int getRecentFailedLoginCount(Integer userId, int minutes) throws SQLException {
        // Find the user's email and delegate to the FailedLoginTrackingService
        Optional<User> userOpt = userId != null ? userDao.findById(userId) : Optional.empty();
        return userOpt.isPresent() 
            ? failedLoginTrackingService.getRecentFailedLoginCount(userOpt.get().getEmail(), minutes)
            : 0;
    }
    
    @Override
    public int getRecentFailedLoginCount(String ipAddress, int minutes) throws SQLException {
        // Delegate to the FailedLoginTrackingService
        return failedLoginTrackingService.getRecentFailedLoginCountByIp(ipAddress, minutes);
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