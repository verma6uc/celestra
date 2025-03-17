package com.celestra.auth.service.impl;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.celestra.auth.config.AuthConfigProvider;
import com.celestra.auth.service.AuditService;
import com.celestra.auth.service.InvitationAcceptanceService;
import com.celestra.auth.service.InvitationService;
import com.celestra.dao.UserDao;
import com.celestra.email.EmailService;
import com.celestra.email.exception.EmailException;
import com.celestra.enums.AuditEventType;
import com.celestra.enums.UserStatus;
import com.celestra.model.Invitation;
import com.celestra.model.User;

/**
 * Implementation of the InvitationAcceptanceService interface.
 */
public class InvitationAcceptanceServiceImpl implements InvitationAcceptanceService {
    
    private static final Logger LOGGER = Logger.getLogger(InvitationAcceptanceServiceImpl.class.getName());
    
    private final InvitationService invitationService;
    private final UserDao userDao;
    private final AuditService auditService;
    private final EmailService emailService;
    private final AuthConfigProvider config;
    
    /**
     * Constructor with all required dependencies.
     * 
     * @param invitationService The invitation service
     * @param userDao The user data access object
     * @param auditService The audit service
     * @param emailService The email service
     * @param config The authentication configuration provider
     */
    public InvitationAcceptanceServiceImpl(InvitationService invitationService, UserDao userDao, 
            AuditService auditService, EmailService emailService, AuthConfigProvider config) {
        this.invitationService = invitationService;
        this.userDao = userDao;
        this.auditService = auditService;
        this.emailService = emailService;
        this.config = config;
    }
    
    @Override
    public Optional<Invitation> validateInvitationToken(String token) throws SQLException {
        return invitationService.validateInvitationToken(token);
    }
    
    @Override
    public Optional<User> getUserFromInvitation(Invitation invitation) throws SQLException {
        if (invitation == null || invitation.getUserId() == null) {
            return Optional.empty();
        }
        
        return userDao.findById(invitation.getUserId());
    }
    
    @Override
    public Optional<User> completeAccountSetup(String token, String password, String confirmPassword, 
            Map<String, String> userDetails, String ipAddress) 
            throws SQLException, IllegalArgumentException {
        
        // Validate token
        Optional<Invitation> optInvitation = validateInvitationToken(token);
        if (!optInvitation.isPresent()) {
            LOGGER.log(Level.INFO, "Invalid or expired invitation token: {0}", token);
            throw new IllegalArgumentException("Invalid or expired invitation token");
        }
        
        Invitation invitation = optInvitation.get();
        
        // Get user
        Optional<User> optUser = getUserFromInvitation(invitation);
        if (!optUser.isPresent()) {
            LOGGER.log(Level.WARNING, "User not found for invitation ID: {0}", invitation.getId());
            throw new IllegalArgumentException("User not found for invitation");
        }
        
        User user = optUser.get();
        
        // Validate passwords
        if (!password.equals(confirmPassword)) {
            LOGGER.log(Level.INFO, "Passwords do not match for user ID: {0}", user.getId());
            throw new IllegalArgumentException("Passwords do not match");
        }
        
        if (!validatePassword(password)) {
            LOGGER.log(Level.INFO, "Password does not meet requirements for user ID: {0}", user.getId());
            throw new IllegalArgumentException("Password does not meet requirements");
        }
        
        // Update user details
        Map<String, Object> beforeValues = new HashMap<>();
        Map<String, Object> afterValues = new HashMap<>();
        
        // Store original values for audit
        beforeValues.put("name", user.getName());
        beforeValues.put("status", user.getStatus());
        
        // Update user details
        if (userDetails != null) {
            if (userDetails.containsKey("name")) {
                user.setName(userDetails.get("name"));
                afterValues.put("name", user.getName());
            }
            
            // Add other user details as needed
        }
        
        // Set password
        String salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);
        user.setPasswordHash(salt + ":" + hashedPassword);
        
        // Update user status to ACTIVE
        user.setStatus(UserStatus.ACTIVE);
        afterValues.put("status", user.getStatus());
        
        // Update user
        user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        User updatedUser = userDao.update(user);
        
        // Mark invitation as accepted
        invitationService.acceptInvitation(token, ipAddress);
        
        // Record audit log
        auditService.recordUserUpdate(user, ipAddress, user, beforeValues, afterValues);
        
        LOGGER.log(Level.INFO, "Completed account setup for user ID: {0}", user.getId());
        
        return Optional.of(updatedUser);
    }
    
    @Override
    public boolean activateUserAccount(Integer userId, String ipAddress) throws SQLException {
        // Get user
        Optional<User> optUser = userDao.findById(userId);
        if (!optUser.isPresent()) {
            LOGGER.log(Level.WARNING, "User not found for activation: {0}", userId);
            return false;
        }
        
        User user = optUser.get();
        
        // Check if already active
        if (user.getStatus() == UserStatus.ACTIVE) {
            LOGGER.log(Level.INFO, "User ID: {0} is already active", userId);
            return true;
        }
        
        // Store original values for audit
        String oldStatus = user.getStatus() != null ? user.getStatus().toString() : null;
        
        // Update user status
        user.setStatus(UserStatus.ACTIVE);
        user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        
        // Update user
        userDao.update(user);
        
        // Record audit log
        auditService.recordUserStatusChange(
                user, 
                ipAddress, 
                user, 
                oldStatus, 
                UserStatus.ACTIVE.toString(), 
                "Account activated after invitation acceptance");
        
        LOGGER.log(Level.INFO, "Activated account for user ID: {0}", userId);
        
        return true;
    }
    
    @Override
    public void sendWelcomeEmail(User user) throws EmailException {
        if (user == null || user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("User email is required to send welcome email");
        }
        
        String subject = "Welcome to Celestra";
        String htmlBody = buildWelcomeEmailBody(user);
        
        emailService.sendHtmlEmail(user.getEmail(), subject, htmlBody);
        
        LOGGER.log(Level.INFO, "Sent welcome email to user ID: {0}", user.getId());
    }
    
    @Override
    public boolean validatePassword(String password) {
        if (password == null) {
            return false;
        }
        
        // Check length
        if (password.length() < config.getPasswordMinLength() || 
            password.length() > config.getPasswordMaxLength()) {
            return false;
        }
        
        // Check uppercase
        if (config.isPasswordUppercaseRequired() && !Pattern.compile("[A-Z]").matcher(password).find()) {
            return false;
        }
        
        // Check lowercase
        if (config.isPasswordLowercaseRequired() && !Pattern.compile("[a-z]").matcher(password).find()) {
            return false;
        }
        
        // Check digit
        if (config.isPasswordDigitRequired() && !Pattern.compile("\\d").matcher(password).find()) {
            return false;
        }
        
        // Check special character
        if (config.isPasswordSpecialCharRequired()) {
            String specialChars = config.getPasswordSpecialChars();
            if (specialChars == null || specialChars.isEmpty()) {
                specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?";
            }
            
            boolean hasSpecialChar = false;
            for (char c : password.toCharArray()) {
                if (specialChars.indexOf(c) >= 0) {
                    hasSpecialChar = true;
                    break;
                }
            }
            
            if (!hasSpecialChar) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public Map<String, Boolean> getPasswordRequirements(String password) {
        Map<String, Boolean> requirements = new HashMap<>();
        
        // Length requirement
        requirements.put("Length (min " + config.getPasswordMinLength() + ", max " + config.getPasswordMaxLength() + ")", 
                password != null && 
                password.length() >= config.getPasswordMinLength() && 
                password.length() <= config.getPasswordMaxLength());
        
        // Uppercase requirement
        if (config.isPasswordUppercaseRequired()) {
            requirements.put("At least one uppercase letter", 
                    password != null && Pattern.compile("[A-Z]").matcher(password).find());
        }
        
        // Lowercase requirement
        if (config.isPasswordLowercaseRequired()) {
            requirements.put("At least one lowercase letter", 
                    password != null && Pattern.compile("[a-z]").matcher(password).find());
        }
        
        // Digit requirement
        if (config.isPasswordDigitRequired()) {
            requirements.put("At least one digit", 
                    password != null && Pattern.compile("\\d").matcher(password).find());
        }
        
        // Special character requirement
        if (config.isPasswordSpecialCharRequired()) {
            String specialChars = config.getPasswordSpecialChars();
            if (specialChars == null || specialChars.isEmpty()) {
                specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?";
            }
            
            boolean hasSpecialChar = false;
            if (password != null) {
                for (char c : password.toCharArray()) {
                    if (specialChars.indexOf(c) >= 0) {
                        hasSpecialChar = true;
                        break;
                    }
                }
            }
            
            requirements.put("At least one special character", hasSpecialChar);
        }
        
        return requirements;
    }
    
    /**
     * Builds the welcome email body.
     * 
     * @param user The user to send the welcome email to
     * @return The HTML email body
     */
    private String buildWelcomeEmailBody(User user) {
        StringBuilder builder = new StringBuilder();
        
        builder.append("<html><body>");
        builder.append("<h1>Welcome to Celestra!</h1>");
        builder.append("<p>Hello ");
        builder.append(user.getName() != null ? user.getName() : user.getEmail());
        builder.append(",</p>");
        builder.append("<p>Your account has been successfully set up. You can now log in to Celestra using your email address and the password you created.</p>");
        builder.append("<p><a href=\"https://app.celestra.com/login\" style=\"display: inline-block; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 4px;\">Log In to Celestra</a></p>");
        builder.append("<p>If you have any questions or need assistance, please contact our support team.</p>");
        builder.append("<p>Thank you,<br>The Celestra Team</p>");
        builder.append("</body></html>");
        
        return builder.toString();
    }
    
    /**
     * Generates a random salt for password hashing.
     * 
     * @return A random salt
     */
    private String generateSalt() {
        // In a real implementation, this would use a secure random generator
        // For simplicity, we're using a placeholder implementation
        return java.util.UUID.randomUUID().toString();
    }
    
    /**
     * Hashes a password with a salt.
     * 
     * @param password The password to hash
     * @param salt The salt to use
     * @return The hashed password
     */
    private String hashPassword(String password, String salt) {
        // In a real implementation, this would use a secure hashing algorithm like bcrypt
        // For simplicity, we're using a placeholder implementation
        return java.util.Base64.getEncoder().encodeToString(
                (salt + password).getBytes());
    }
}