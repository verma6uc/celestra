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
import java.util.regex.Pattern;

import com.celestra.auth.config.AuthConfigProvider;
import com.celestra.auth.config.AuthConfigurationManager;
import com.celestra.auth.service.RegistrationService;
import com.celestra.auth.util.EmailUtil;
import com.celestra.auth.util.PasswordUtil;
import com.celestra.dao.AuditLogDao;
import com.celestra.dao.InvitationDao;
import com.celestra.dao.UserDao;
import com.celestra.dao.impl.AuditLogDaoImpl;
import com.celestra.dao.impl.InvitationDaoImpl;
import com.celestra.dao.impl.UserDaoImpl;
import com.celestra.db.TransactionUtil;
import com.celestra.email.EmailService;
import com.celestra.email.JavaMailEmailService;
import com.celestra.email.exception.EmailException;
import com.celestra.enums.AuditEventType;
import com.celestra.enums.InvitationStatus;
import com.celestra.enums.UserRole;
import com.celestra.enums.UserStatus;
import com.celestra.model.AuditLog;
import com.celestra.model.Invitation;
import com.celestra.model.User;

/**
 * Implementation of the RegistrationService interface.
 */
public class RegistrationServiceImpl implements RegistrationService {
    
    private static final Logger LOGGER = Logger.getLogger(RegistrationServiceImpl.class.getName());
    private static final SecureRandom RANDOM = new SecureRandom();
    
    private final UserDao userDao;
    private final InvitationDao invitationDao;
    private final AuthConfigProvider config;
    private final EmailService emailService;
    private final AuditLogDao auditLogDao;
    
    /**
     * Default constructor.
     * Initializes DAOs with default implementations.
     */
    public RegistrationServiceImpl() {
        this(new UserDaoImpl(), new InvitationDaoImpl(), AuthConfigurationManager.getInstance());
    }
    
    /**
     * Constructor with DAOs but default services.
     */
    public RegistrationServiceImpl(UserDao userDao, InvitationDao invitationDao, AuthConfigProvider config) {
        this(userDao, invitationDao, config, new JavaMailEmailService(), new AuditLogDaoImpl());
    }
    
    /**
     * Parameterized constructor for dependency injection.
     */
    public RegistrationServiceImpl(UserDao userDao, InvitationDao invitationDao, AuthConfigProvider config, 
                                  EmailService emailService, AuditLogDao auditLogDao) {
        this.userDao = userDao;
        this.invitationDao = invitationDao;
        this.config = config;
        this.emailService = emailService;
        this.auditLogDao = auditLogDao;
    }
    
    @Override
    public User registerUser(String email, String name, String password, UserRole role, 
                           Integer companyId, String ipAddress, Map<String, String> metadata) 
                           throws SQLException, IllegalArgumentException, SecurityException {
        
        // Validate input
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        
        if (role == null) {
            throw new IllegalArgumentException("Role is required");
        }
        
        // Normalize email
        email = EmailUtil.normalizeEmail(email);
        
        // Validate email format
        if (!validateEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        
        // Check if email is already in use
        if (isEmailInUse(email)) {
            throw new IllegalArgumentException("Email is already in use");
        }
        
        // Validate password complexity
        Map<String, Boolean> passwordValidation = validatePassword(password);
        if (passwordValidation.containsValue(false)) {
            StringBuilder errorMessage = new StringBuilder("Password does not meet complexity requirements: ");
            if (Boolean.FALSE.equals(passwordValidation.get("length"))) {
                errorMessage.append("length must be between ")
                           .append(config.getPasswordMinLength())
                           .append(" and ")
                           .append(config.getPasswordMaxLength())
                           .append(" characters; ");
            }
            if (Boolean.FALSE.equals(passwordValidation.get("uppercase"))) {
                errorMessage.append("must contain uppercase letters; ");
            }
            if (Boolean.FALSE.equals(passwordValidation.get("lowercase"))) {
                errorMessage.append("must contain lowercase letters; ");
            }
            if (Boolean.FALSE.equals(passwordValidation.get("digit"))) {
                errorMessage.append("must contain digits; ");
            }
            if (Boolean.FALSE.equals(passwordValidation.get("special"))) {
                errorMessage.append("must contain special characters; ");
            }
            throw new IllegalArgumentException(errorMessage.toString());
        }
        
        // Validate role and company ID combination
        if (role == UserRole.SUPER_ADMIN && companyId != null) {
            throw new IllegalArgumentException("Super admins cannot be associated with a company");
        }
        
        if (role != UserRole.SUPER_ADMIN && companyId == null) {
            throw new IllegalArgumentException("Non-super admin users must be associated with a company");
        }
        
        // Check if self-registration is allowed
        if (!config.isSelfRegistrationAllowed() && role != UserRole.SUPER_ADMIN) {
            throw new SecurityException("Self-registration is not allowed");
        }
        
        // Hash the password
        String passwordHash = PasswordUtil.createPasswordHash(password);
        
        // Create the user
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPasswordHash(passwordHash);
        user.setRole(role);
        user.setCompanyId(companyId);
        user.setStatus(config.isEmailVerificationRequired() ? UserStatus.SUSPENDED : UserStatus.ACTIVE);
        
        // Create the user
        User createdUser;
        try {
            final User finalUser = user;
            TransactionUtil.executeInTransaction(conn -> {
                // Create the user
                User newUser = userDao.create(finalUser);
                
                // Add password to history
                userDao.addPasswordToHistory(newUser.getId(), passwordHash);
                
                // Generate email verification token if required
                if (config.isEmailVerificationRequired()) {
                    generateEmailVerificationInvitation(newUser);
                }
                
                // Create audit log entry for account creation
                createAuditLog(newUser.getId(), AuditEventType.OTHER, "User account created", ipAddress, "users", newUser.getId().toString(), null);
            });
            createdUser = userDao.findByEmail(email).orElseThrow(() -> new SQLException("User creation failed"));
            
            // Send welcome email
            sendWelcomeEmail(createdUser, password);
        } catch (Exception e) {
            throw new SQLException("Error creating user: " + e.getMessage(), e);
        }
        return createdUser;
    }

    @Override
    public User registerUserViaInvitation(String invitationToken, String name, String password,
                                        String ipAddress, Map<String, String> metadata)
                                        throws SQLException, IllegalArgumentException, SecurityException {
        
        // Validate input
        if (invitationToken == null || invitationToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Invitation token is required");
        }
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        
        // Find the invitation
        Optional<Invitation> invitationOpt = invitationDao.findByToken(invitationToken);
        if (!invitationOpt.isPresent()) {
            throw new IllegalArgumentException("Invalid invitation token");
        }
        
        Invitation invitation = invitationOpt.get();
        
        // Check if the invitation is valid
        if (invitation.isExpired()) {
            throw new IllegalArgumentException("Invitation has expired");
        }
        
        if (invitation.isAccepted()) {
            throw new IllegalArgumentException("Invitation has already been accepted");
        }
        
        // Find the user associated with the invitation
        Optional<User> userOpt = userDao.findById(invitation.getUserId());
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("User not found for invitation");
        }
        
        User user = userOpt.get();
        
        // Validate password complexity
        Map<String, Boolean> passwordValidation = validatePassword(password);
        if (passwordValidation.containsValue(false)) {
            StringBuilder errorMessage = new StringBuilder("Password does not meet complexity requirements: ");
            if (Boolean.FALSE.equals(passwordValidation.get("length"))) {
                errorMessage.append("length must be between ")
                           .append(config.getPasswordMinLength())
                           .append(" and ")
                           .append(config.getPasswordMaxLength())
                           .append(" characters; ");
            }
            if (Boolean.FALSE.equals(passwordValidation.get("uppercase"))) {
                errorMessage.append("must contain uppercase letters; ");
            }
            if (Boolean.FALSE.equals(passwordValidation.get("lowercase"))) {
                errorMessage.append("must contain lowercase letters; ");
            }
            if (Boolean.FALSE.equals(passwordValidation.get("digit"))) {
                errorMessage.append("must contain digits; ");
            }
            if (Boolean.FALSE.equals(passwordValidation.get("special"))) {
                errorMessage.append("must contain special characters; ");
            }
            throw new IllegalArgumentException(errorMessage.toString());
        }
        
        // Hash the password
        String passwordHash = PasswordUtil.createPasswordHash(password);
        
        // Update the user
        User updatedUser;
        try {
            final User finalUser = user;
            final Invitation finalInvitation = invitation;
            TransactionUtil.executeInTransaction(conn -> {
                // Update the user
                finalUser.setName(name);
                finalUser.setPasswordHash(passwordHash);
                finalUser.setStatus(UserStatus.ACTIVE);
                userDao.update(finalUser);
                
                // Add password to history
                userDao.addPasswordToHistory(finalUser.getId(), passwordHash);
                
                // Mark the invitation as accepted
                finalInvitation.setStatus(InvitationStatus.ACCEPTED);
                invitationDao.update(finalInvitation);
                
                // Create audit log entry for account activation
                createAuditLog(finalUser.getId(), AuditEventType.OTHER, "User account activated via invitation", ipAddress, "users", finalUser.getId().toString(), null);
            });
            updatedUser = userDao.findById(user.getId()).orElseThrow(() -> new SQLException("User update failed"));
            
            // Send account activation email
            sendAccountActivationEmail(updatedUser);
        } catch (Exception e) {
            throw new SQLException("Error updating user: " + e.getMessage(), e);
        }
        return updatedUser;
    }

    @Override
    public boolean validateEmail(String email) {
        return EmailUtil.isValidEmail(email);
    }
    
    @Override
    public Map<String, Boolean> validatePassword(String password) {
        return PasswordUtil.validatePassword(password);
    }
    
    @Override
    public boolean isEmailInUse(String email) throws SQLException {
        return userDao.findByEmail(email).isPresent();
    }
    
    @Override
    public String generateEmailVerificationInvitation(User user) throws SQLException {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        // Generate a random token
        String token = generateRandomToken();
        
        // Calculate expiration time
        Timestamp expiresAt = Timestamp.from(
            Instant.now().plus(config.getEmailVerificationExpirationHours(), ChronoUnit.HOURS)
        );
        
        // Create the invitation
        Invitation invitation = new Invitation();
        invitation.setUserId(user.getId());
        invitation.setToken(token);
        invitation.setStatus(InvitationStatus.SENT); // Set as SENT for email verification
        invitation.setSentAt(new Timestamp(System.currentTimeMillis()));
        invitation.setExpiresAt(expiresAt);
        invitation.setResendCount(0);
        
        // Save the invitation
        invitationDao.create(invitation);
        
        return token;
    }
    
    @Override
    public boolean verifyEmail(String token) throws SQLException {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        // Find the invitation
        Optional<Invitation> invitationOpt = invitationDao.findByToken(token);
        if (!invitationOpt.isPresent()) {
            return false;
        }
        
        Invitation invitation = invitationOpt.get();
        
        // Check if the invitation is valid
        if (invitation.isExpired()) {
            return false;
        }
        
        if (invitation.isAccepted()) {
            return false;
        }
        
        // Find the user
        Optional<User> userOpt = userDao.findById(invitation.getUserId());
        if (!userOpt.isPresent()) {
            return false;
        }
        
        User user = userOpt.get();
        
        // Update the user and invitation
        boolean success;
        try {
            final User finalUser = user;
            final Invitation finalInvitation = invitation;
            TransactionUtil.executeInTransaction(conn -> {
                // Update the user status
                finalUser.setStatus(UserStatus.ACTIVE);
                userDao.update(finalUser);
                
                // Mark the invitation as accepted
                finalInvitation.setStatus(InvitationStatus.ACCEPTED);
                invitationDao.update(finalInvitation);
                
                // Create audit log entry for email verification
                createAuditLog(finalUser.getId(), AuditEventType.OTHER, "Email verified", null, "users", finalUser.getId().toString(), null);
            });
            success = true;
            
            // Send email verification confirmation
            sendEmailVerificationConfirmationEmail(user);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error verifying email: " + e.getMessage(), e);
            success = false;
        }
        return success;
    }
    
    /**
     * Send a welcome email to a newly registered user.
     * 
     * @param user The user to send the email to
     * @param password The user's plain text password (only included if self-registration)
     */
    private void sendWelcomeEmail(User user, String password) {
        try {
            String subject = "Welcome to Celestra";
            
            StringBuilder bodyBuilder = new StringBuilder();
            bodyBuilder.append("Dear ").append(user.getName()).append(",\n\n");
            bodyBuilder.append("Welcome to Celestra! Your account has been created successfully.\n\n");
            
            if (config.isEmailVerificationRequired()) {
                bodyBuilder.append("Please verify your email address by clicking on the verification link that has been sent to you in a separate email.\n\n");
            }
            
            if (password != null) {
                bodyBuilder.append("Your account details:\n");
                bodyBuilder.append("Email: ").append(user.getEmail()).append("\n");
                bodyBuilder.append("Password: ").append(password).append("\n\n");
                bodyBuilder.append("Please change your password after your first login for security reasons.\n\n");
            }
            
            bodyBuilder.append("If you have any questions, please contact our support team.\n\n");
            bodyBuilder.append("Best regards,\n");
            bodyBuilder.append("The Celestra Team");
            
            emailService.sendPlainTextEmail(user.getEmail(), subject, bodyBuilder.toString());
            
            LOGGER.info("Welcome email sent to " + user.getEmail());
        } catch (EmailException e) {
            LOGGER.log(Level.SEVERE, "Error sending welcome email to " + user.getEmail(), e);
        }
    }
    
    /**
     * Send an account activation email to a user who has activated their account via invitation.
     * 
     * @param user The user to send the email to
     */
    private void sendAccountActivationEmail(User user) {
        try {
            String subject = "Celestra Account Activated";
            
            StringBuilder bodyBuilder = new StringBuilder();
            bodyBuilder.append("Dear ").append(user.getName()).append(",\n\n");
            bodyBuilder.append("Your Celestra account has been successfully activated.\n\n");
            bodyBuilder.append("You can now log in to the system using your email address and the password you provided.\n\n");
            bodyBuilder.append("If you have any questions, please contact our support team.\n\n");
            bodyBuilder.append("Best regards,\n");
            bodyBuilder.append("The Celestra Team");
            
            emailService.sendPlainTextEmail(user.getEmail(), subject, bodyBuilder.toString());
            
            LOGGER.info("Account activation email sent to " + user.getEmail());
        } catch (EmailException e) {
            LOGGER.log(Level.SEVERE, "Error sending account activation email to " + user.getEmail(), e);
        }
    }
    
    /**
     * Send an email verification confirmation email to a user who has verified their email.
     * 
     * @param user The user to send the email to
     */
    private void sendEmailVerificationConfirmationEmail(User user) {
        try {
            String subject = "Celestra Email Verification Successful";
            
            StringBuilder bodyBuilder = new StringBuilder();
            bodyBuilder.append("Dear ").append(user.getName()).append(",\n\n");
            bodyBuilder.append("Your email address has been successfully verified.\n\n");
            bodyBuilder.append("You can now use all features of the Celestra platform.\n\n");
            bodyBuilder.append("If you have any questions, please contact our support team.\n\n");
            bodyBuilder.append("Best regards,\n");
            bodyBuilder.append("The Celestra Team");
            
            emailService.sendPlainTextEmail(user.getEmail(), subject, bodyBuilder.toString());
            
            LOGGER.info("Email verification confirmation sent to " + user.getEmail());
        } catch (EmailException e) {
            LOGGER.log(Level.SEVERE, "Error sending email verification confirmation to " + user.getEmail(), e);
        }
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
     * Generate a random token for email verification.
     * 
     * @return A random token
     */
    private String generateRandomToken() {
        byte[] randomBytes = new byte[32];
        RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}