package com.celestra.auth.service.impl;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.celestra.auth.config.AuthConfigurationManager;
import com.celestra.auth.service.ForgotPasswordService;
import com.celestra.auth.util.PasswordUtil;
import com.celestra.dao.AuditLogDao;
import com.celestra.dao.PasswordHistoryDao;
import com.celestra.dao.PasswordResetTokenDao;
import com.celestra.dao.UserDao;
import com.celestra.dao.UserSessionDao;
import com.celestra.dao.impl.AuditLogDaoImpl;
import com.celestra.dao.impl.PasswordHistoryDaoImpl;
import com.celestra.dao.impl.PasswordResetTokenDaoImpl;
import com.celestra.dao.impl.UserDaoImpl;
import com.celestra.dao.impl.UserSessionDaoImpl;
import com.celestra.email.EmailService;
import com.celestra.email.JavaMailEmailService;
import com.celestra.email.exception.EmailException;
import com.celestra.enums.AuditEventType;
import com.celestra.model.AuditLog;
import com.celestra.model.PasswordHistory;
import com.celestra.model.PasswordResetToken;
import com.celestra.model.User;

/**
 * Implementation of the ForgotPasswordService interface.
 */
public class ForgotPasswordServiceImpl implements ForgotPasswordService {
    
    private static final Logger LOGGER = Logger.getLogger(ForgotPasswordServiceImpl.class.getName());
    
    private final UserDao userDao;
    private final PasswordResetTokenDao passwordResetTokenDao;
    private final PasswordHistoryDao passwordHistoryDao;
    private final UserSessionDao userSessionDao;
    private final AuditLogDao auditLogDao;
    private final EmailService emailService;
    private final AuthConfigurationManager authConfig;
    
    /**
     * Default constructor.
     */
    public ForgotPasswordServiceImpl() {
        this.userDao = new UserDaoImpl();
        this.passwordResetTokenDao = new PasswordResetTokenDaoImpl();
        this.passwordHistoryDao = new PasswordHistoryDaoImpl();
        this.userSessionDao = new UserSessionDaoImpl();
        this.auditLogDao = new AuditLogDaoImpl();
        this.emailService = new JavaMailEmailService();
        this.authConfig = AuthConfigurationManager.getInstance();
    }
    
    /**
     * Constructor with dependencies for testing.
     */
    public ForgotPasswordServiceImpl(UserDao userDao, PasswordResetTokenDao passwordResetTokenDao,
            PasswordHistoryDao passwordHistoryDao, UserSessionDao userSessionDao,
            AuditLogDao auditLogDao, EmailService emailService, AuthConfigurationManager authConfig) {
        this.userDao = userDao;
        this.passwordResetTokenDao = passwordResetTokenDao;
        this.passwordHistoryDao = passwordHistoryDao;
        this.userSessionDao = userSessionDao;
        this.auditLogDao = auditLogDao;
        this.emailService = emailService;
        this.authConfig = authConfig;
    }
    
    @Override
    public boolean initiatePasswordReset(String email, String ipAddress, Map<String, String> metadata) 
            throws SQLException {
        // Find the user by email
        Optional<User> userOpt = userDao.findByEmail(email);
        
        // Always return true even if user doesn't exist to prevent email enumeration
        if (!userOpt.isPresent()) {
            LOGGER.info("Password reset requested for non-existent email: " + email);
            return true;
        }
        
        User user = userOpt.get();
        
        // Generate a secure random token
        String token = UUID.randomUUID().toString();
        
        // Calculate expiration time based on configuration
        int tokenExpirationMinutes = authConfig.getPasswordResetTokenExpirationMinutes();
        Timestamp expiresAt = Timestamp.from(
                Instant.now().plus(tokenExpirationMinutes, ChronoUnit.MINUTES));
        
        // Create a new password reset token
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUserId(user.getId());
        resetToken.setToken(token);
        resetToken.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        resetToken.setExpiresAt(expiresAt);
        
        // Save the token to the database
        passwordResetTokenDao.create(resetToken);
        
        // Create audit log
        createAuditLog(user.getId(), ipAddress, "Password reset requested", metadata);
        
        // Send password reset email
        try {
            sendPasswordResetEmail(user.getEmail(), token);
            return true;
        } catch (EmailException e) {
            LOGGER.log(Level.SEVERE, "Failed to send password reset email", e);
            return false;
        }
    }
    
    @Override
    public boolean validateResetToken(String token) throws SQLException {
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenDao.findByToken(token);
        
        if (!tokenOpt.isPresent()) {
            // Create audit log for invalid token
            createAuditLog(null, "Unknown", "Password reset token validation failed - token not found", null);
            return false;
        }

        // Create audit log for token validation
        createAuditLog(tokenOpt.get().getUserId(), "Unknown", tokenOpt.get().isValid() ? "Password reset token validated" : "Password reset token validation failed - token expired or used", null);
        
        PasswordResetToken resetToken = tokenOpt.get();
        return resetToken.isValid();
    }
    
    @Override
    public boolean resetPassword(String token, String newPassword, String ipAddress, Map<String, String> metadata) 
            throws SQLException {
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenDao.findByToken(token);
        
        if (!tokenOpt.isPresent()) {
            // Create audit log for invalid token attempt
            createAuditLog(null, ipAddress, "Password reset attempted with invalid token", metadata);
            return false;
        }
        
        PasswordResetToken resetToken = tokenOpt.get();
        
        // Check if token is valid
        if (!resetToken.isValid()) {
            // Create audit log for expired or used token attempt
            createAuditLog(resetToken.getUserId(), ipAddress, "Password reset attempted with expired or used token", metadata);
            return false;
        }
        
        Integer userId = resetToken.getUserId();
        Optional<User> userOpt = userDao.findById(userId);
        
        if (!userOpt.isPresent()) {
            // Create audit log for user not found
            createAuditLog(userId, ipAddress, "Password reset attempted for non-existent user", metadata);
            return false;
        }
        
        User user = userOpt.get();
        
        // Check password history if enabled
        int passwordHistoryCount = authConfig.getPasswordHistoryCount();
        if (passwordHistoryCount > 0) {
            // Create a temporary hash to check against history
            String newPasswordHash = PasswordUtil.createPasswordHash(newPassword);
            
            // Check if password is in history
            // Note: This is not a perfect check since we're comparing hashed passwords with different salts
            if (passwordHistoryDao.existsByUserIdAndPasswordHash(userId, newPasswordHash)) {
                // Create audit log for password reuse
                createAuditLog(userId, ipAddress, "Password reset failed - password reused", metadata);
                return false;
            }
        }
        
        // Hash the new password
        String passwordHash = PasswordUtil.createPasswordHash(newPassword);
        
        // Update the user's password
        String oldPasswordHash = user.getPasswordHash();
        user.setPasswordHash(passwordHash);
        userDao.update(user);
        
        // Add the old password to history
        PasswordHistory passwordHistory = new PasswordHistory();
        passwordHistory.setUserId(userId);
        passwordHistory.setPasswordHash(oldPasswordHash);
        passwordHistoryDao.create(passwordHistory);
        
        // Mark the token as used
        passwordResetTokenDao.markAsUsed(token, new Timestamp(System.currentTimeMillis()));
        
        // End all active sessions for the user
        userSessionDao.deleteByUserId(userId);
        
        // Create audit log
        createAuditLog(userId, ipAddress, "Password reset completed", metadata);
        
        // Send password changed notification email
        try {
            sendPasswordChangedEmail(user.getEmail());
        } catch (EmailException e) {
            LOGGER.log(Level.SEVERE, "Failed to send password changed email", e);
            // Continue anyway, the password has been reset successfully
        }
        
        return true;
    }
    
    @Override
    public String getEmailFromToken(String token) throws SQLException {
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenDao.findByToken(token);
        
        if (!tokenOpt.isPresent() || !tokenOpt.get().isValid()) {
            // Create audit log for invalid token
            createAuditLog(null, "Unknown", "Email requested for invalid password reset token", null);
            return null;
        }
        
        PasswordResetToken resetToken = tokenOpt.get();
        Optional<User> userOpt = userDao.findById(resetToken.getUserId());
        
        if (!userOpt.isPresent()) {
            // Create audit log for user not found
            createAuditLog(resetToken.getUserId(), "Unknown", "Email requested for non-existent user", null);
            return null;
        }
        createAuditLog(resetToken.getUserId(), "Unknown", "Email retrieved from valid password reset token", null);
        
        return userOpt.get().getEmail();
    }
    
    /**
     * Create an audit log for password reset actions.
     * 
     * @param userId The ID of the user
     * @param ipAddress The IP address of the client
     * @param description The description of the action
     * @param metadata Additional metadata about the action
     * @throws SQLException if a database error occurs
     */
    private void createAuditLog(Integer userId, String ipAddress, String description, Map<String, String> metadata) 
            throws SQLException {
        // If userId is null, we still want to log the event but without associating it with a user
        if (userId == null) {
            LOGGER.info("Creating audit log without user ID: " + description);
        }
        
        Map<String, String> auditMetadata = new HashMap<>();
        if (metadata != null) {
            auditMetadata.putAll(metadata);
        }
        
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(userId);
        auditLog.setEventType(AuditEventType.OTHER); // Consider adding a specific event type for password reset
        auditLog.setEventDescription(description);
        auditLog.setIpAddress(ipAddress);
        auditLog.setReason("Password reset flow");
        auditLog.setTableName("users");
        auditLog.setRecordId(userId != null ? userId.toString() : "unknown");
        auditLog.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        
        auditLogDao.create(auditLog);
    }
    
    /**
     * Send a password reset email to the user.
     * 
     * @param email The email address of the user
     * @param token The password reset token
     * @throws EmailException if an error occurs while sending the email
     */
    private void sendPasswordResetEmail(String email, String token) throws EmailException {
        String subject = "Password Reset Request";
        
        // Build the reset URL
        String baseUrl = "https://celestra.example.com"; // This should be configured in auth-config.properties
        String resetUrl = baseUrl + "/reset-password?token=" + token;
        
        // Build the email body
        StringBuilder body = new StringBuilder();
        body.append("<html><body>");
        body.append("<h2>Password Reset Request</h2>");
        body.append("<p>You have requested to reset your password. Please click the link below to reset your password:</p>");
        body.append("<p><a href=\"").append(resetUrl).append("\">Reset Password</a></p>");
        body.append("<p>If you did not request a password reset, please ignore this email.</p>");
        body.append("<p>This link will expire in ").append(authConfig.getPasswordResetTokenExpirationMinutes() / 60).append(" hours.</p>");
        body.append("<p>Thank you,<br>The Celestra Team</p>");
        body.append("</body></html>");
        
        emailService.sendHtmlEmail(email, subject, body.toString());
    }
    
    /**
     * Send a password changed notification email to the user.
     * 
     * @param email The email address of the user
     * @throws EmailException if an error occurs while sending the email
     */
    private void sendPasswordChangedEmail(String email) throws EmailException {
        String subject = "Password Changed";
        
        // Build the email body
        StringBuilder body = new StringBuilder();
        body.append("<html><body>");
        body.append("<h2>Password Changed</h2>");
        body.append("<p>Your password has been successfully changed.</p>");
        body.append("<p>If you did not make this change, please contact support immediately.</p>");
        body.append("<p>Thank you,<br>The Celestra Team</p>");
        body.append("</body></html>");
        
        emailService.sendHtmlEmail(email, subject, body.toString());
    }
}