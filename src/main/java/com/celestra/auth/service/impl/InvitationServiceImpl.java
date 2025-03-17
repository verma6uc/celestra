package com.celestra.auth.service.impl;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.celestra.auth.config.AuthConfigProvider;
import com.celestra.auth.service.AuditService;
import com.celestra.auth.service.InvitationService;
import com.celestra.enums.AuditEventType;
import com.celestra.dao.InvitationDao;
import com.celestra.dao.UserDao;
import com.celestra.email.EmailService;
import com.celestra.email.exception.EmailException;
import com.celestra.enums.InvitationStatus;
import com.celestra.model.Invitation;
import com.celestra.model.User;

/**
 * Implementation of the InvitationService interface.
 */
public class InvitationServiceImpl implements InvitationService {
    
    private static final Logger LOGGER = Logger.getLogger(InvitationServiceImpl.class.getName());
    private static final int TOKEN_LENGTH_BYTES = 32; // 256 bits
    
    private final InvitationDao invitationDao;
    private final UserDao userDao;
    private final EmailService emailService;
    private final AuditService auditService;
    private final AuthConfigProvider config;
    
    /**
     * Constructor with all required dependencies.
     * 
     * @param invitationDao The invitation data access object
     * @param userDao The user data access object
     * @param emailService The email service
     * @param auditService The audit service
     * @param config The authentication configuration provider
     */
    public InvitationServiceImpl(InvitationDao invitationDao, UserDao userDao, 
            EmailService emailService, AuditService auditService, AuthConfigProvider config) {
        this.invitationDao = invitationDao;
        this.userDao = userDao;
        this.emailService = emailService;
        this.auditService = auditService;
        this.config = config;
    }
    
    @Override
    public Invitation createInvitation(User user, Integer inviterUserId, String ipAddress) 
            throws SQLException, EmailException {
        // Validate user
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User cannot be null and must have an ID");
        }
        
        // Check if user exists
        Optional<User> existingUser = userDao.findById(user.getId());
        if (!existingUser.isPresent()) {
            throw new IllegalArgumentException("User with ID " + user.getId() + " does not exist");
        }
        
        // Generate token
        String token = generateInvitationToken();
        
        // Create invitation
        Invitation invitation = new Invitation(user.getId(), token);
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setResendCount(0);
        
        // Set expiration date
        Instant expirationTime = Instant.now().plus(config.getInvitationTokenExpirationDays(), ChronoUnit.DAYS);
        invitation.setExpiresAt(Timestamp.from(expirationTime));
        
        // Save invitation
        Invitation createdInvitation = invitationDao.create(invitation);
        
        // Record audit log
        User inviter = userDao.findById(inviterUserId).orElse(null);
        auditService.recordSecurityEvent(
                AuditEventType.OTHER, 
                existingUser.get(), 
                ipAddress, 
                "Invitation created", 
                "invitations", 
                String.valueOf(createdInvitation.getId()),
                null);
        
        LOGGER.log(Level.INFO, "Created invitation for user ID: {0}", user.getId());
        
        return createdInvitation;
    }
    
    @Override
    public Invitation sendInvitation(Integer invitationId, Integer inviterUserId, String ipAddress) 
            throws SQLException, EmailException {
        // Get invitation
        Optional<Invitation> optInvitation = invitationDao.findById(invitationId);
        if (!optInvitation.isPresent()) {
            throw new IllegalArgumentException("Invitation with ID " + invitationId + " does not exist");
        }
        
        Invitation invitation = optInvitation.get();
        
        // Check if invitation is in a valid state to be sent
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Invitation is not in PENDING state");
        }
        
        // Get user
        Optional<User> optUser = userDao.findById(invitation.getUserId());
        if (!optUser.isPresent()) {
            throw new IllegalStateException("User associated with invitation does not exist");
        }
        
        User user = optUser.get();
        
        // Send email
        sendInvitationEmail(user, invitation);
        
        // Update invitation status
        invitation.setStatus(InvitationStatus.SENT);
        invitation.setSentAt(new Timestamp(System.currentTimeMillis()));
        Invitation updatedInvitation = invitationDao.update(invitation);
        
        // Record audit log
        User inviter = userDao.findById(inviterUserId).orElse(null);
        auditService.recordSecurityEvent(
                AuditEventType.OTHER, 
                user, 
                ipAddress, 
                "Invitation sent", 
                "invitations", 
                String.valueOf(invitation.getId()),
                null);
        
        LOGGER.log(Level.INFO, "Sent invitation ID: {0} to user ID: {1}", 
                new Object[]{invitation.getId(), user.getId()});
        
        return updatedInvitation;
    }
    
    @Override
    public Invitation resendInvitation(Integer invitationId, Integer inviterUserId, String ipAddress) 
            throws SQLException, EmailException {
        // Get invitation
        Optional<Invitation> optInvitation = invitationDao.findById(invitationId);
        if (!optInvitation.isPresent()) {
            throw new IllegalArgumentException("Invitation with ID " + invitationId + " does not exist");
        }
        
        Invitation invitation = optInvitation.get();
        
        // Check if invitation is in a valid state to be resent
        if (invitation.getStatus() != InvitationStatus.SENT) {
            throw new IllegalStateException("Invitation is not in SENT state");
        }
        
        // Get user
        Optional<User> optUser = userDao.findById(invitation.getUserId());
        if (!optUser.isPresent()) {
            throw new IllegalStateException("User associated with invitation does not exist");
        }
        
        User user = optUser.get();
        
        // Increment resend count
        invitationDao.incrementResendCount(invitationId);
        invitation.setResendCount(invitation.getResendCount() + 1);
        
        // Update sent timestamp
        invitation.setSentAt(new Timestamp(System.currentTimeMillis()));
        
        // Extend expiration if needed
        Instant expirationTime = Instant.now().plus(config.getInvitationTokenExpirationDays(), ChronoUnit.DAYS);
        invitation.setExpiresAt(Timestamp.from(expirationTime));
        
        // Update invitation
        Invitation updatedInvitation = invitationDao.update(invitation);
        
        // Send email
        sendInvitationEmail(user, invitation);
        
        // Record audit log
        User inviter = userDao.findById(inviterUserId).orElse(null);
        auditService.recordSecurityEvent(
                AuditEventType.OTHER, 
                user, 
                ipAddress, 
                "Invitation resent", 
                "invitations", 
                String.valueOf(invitation.getId()),
                null);
        
        LOGGER.log(Level.INFO, "Resent invitation ID: {0} to user ID: {1}", 
                new Object[]{invitation.getId(), user.getId()});
        
        return updatedInvitation;
    }
    
    @Override
    public boolean cancelInvitation(Integer invitationId, Integer cancellerUserId, String ipAddress, String reason) 
            throws SQLException {
        // Get invitation
        Optional<Invitation> optInvitation = invitationDao.findById(invitationId);
        if (!optInvitation.isPresent()) {
            throw new IllegalArgumentException("Invitation with ID " + invitationId + " does not exist");
        }
        
        Invitation invitation = optInvitation.get();
        
        // Check if invitation is in a valid state to be cancelled
        if (invitation.getStatus() == InvitationStatus.ACCEPTED || 
            invitation.getStatus() == InvitationStatus.CANCELLED || 
            invitation.getStatus() == InvitationStatus.EXPIRED) {
            throw new IllegalStateException("Invitation cannot be cancelled in its current state: " + invitation.getStatus());
        }
        
        // Get user
        Optional<User> optUser = userDao.findById(invitation.getUserId());
        if (!optUser.isPresent()) {
            throw new IllegalStateException("User associated with invitation does not exist");
        }
        
        User user = optUser.get();
        
        // Update invitation status
        boolean updated = invitationDao.updateStatus(invitationId, InvitationStatus.CANCELLED);
        
        if (updated) {
            // Record audit log
            User canceller = userDao.findById(cancellerUserId).orElse(null);
            auditService.recordSecurityEvent(
                    AuditEventType.OTHER, 
                    user, 
                    ipAddress, 
                    "Invitation cancelled: " + (reason != null ? reason : "No reason provided"), 
                    "invitations", 
                    String.valueOf(invitation.getId()),
                    null);
            
            LOGGER.log(Level.INFO, "Cancelled invitation ID: {0} for user ID: {1}", 
                    new Object[]{invitation.getId(), user.getId()});
        }
        
        return updated;
    }
    
    @Override
    public Optional<Invitation> validateInvitationToken(String token) throws SQLException {
        if (token == null || token.trim().isEmpty()) {
            return Optional.empty();
        }
        
        Optional<Invitation> optInvitation = invitationDao.findByToken(token);
        
        if (!optInvitation.isPresent()) {
            LOGGER.log(Level.INFO, "Invalid invitation token: {0}", token);
            return Optional.empty();
        }
        
        Invitation invitation = optInvitation.get();
        
        // Check if invitation is expired
        if (invitation.isExpired()) {
            LOGGER.log(Level.INFO, "Expired invitation token: {0}", token);
            
            // Update status if not already marked as expired
            if (invitation.getStatus() != InvitationStatus.EXPIRED) {
                invitationDao.updateStatus(invitation.getId(), InvitationStatus.EXPIRED);
            }
            
            return Optional.empty();
        }
        
        // Check if invitation is in a valid state
        if (invitation.getStatus() != InvitationStatus.SENT) {
            LOGGER.log(Level.INFO, "Invitation token in invalid state: {0}, state: {1}", 
                    new Object[]{token, invitation.getStatus()});
            return Optional.empty();
        }
        
        return optInvitation;
    }
    
    @Override
    public boolean acceptInvitation(String token, String ipAddress) throws SQLException {
        Optional<Invitation> optInvitation = validateInvitationToken(token);
        
        if (!optInvitation.isPresent()) {
            return false;
        }
        
        Invitation invitation = optInvitation.get();
        
        // Get user
        Optional<User> optUser = userDao.findById(invitation.getUserId());
        if (!optUser.isPresent()) {
            throw new IllegalStateException("User associated with invitation does not exist");
        }
        
        User user = optUser.get();
        
        // Update invitation status
        boolean updated = invitationDao.updateStatus(invitation.getId(), InvitationStatus.ACCEPTED);
        
        if (updated) {
            // Record audit log
            auditService.recordSecurityEvent(
                    AuditEventType.OTHER, 
                    user, 
                    ipAddress, 
                    "Invitation accepted", 
                    "invitations", 
                    String.valueOf(invitation.getId()),
                    null);
            
            LOGGER.log(Level.INFO, "Accepted invitation ID: {0} for user ID: {1}", 
                    new Object[]{invitation.getId(), user.getId()});
        }
        
        return updated;
    }
    
    @Override
    public List<Invitation> getInvitationsByUserId(Integer userId) throws SQLException {
        return invitationDao.findByUserId(userId);
    }
    
    @Override
    public List<Invitation> getInvitationsByStatus(InvitationStatus status) throws SQLException {
        return invitationDao.findByStatus(status);
    }
    
    @Override
    public Optional<Invitation> getInvitationById(Integer invitationId) throws SQLException {
        return invitationDao.findById(invitationId);
    }
    
    @Override
    public Optional<Invitation> getInvitationByToken(String token) throws SQLException {
        return invitationDao.findByToken(token);
    }
    
    @Override
    public int cleanupExpiredInvitations() throws SQLException {
        List<Invitation> expiredInvitations = invitationDao.findExpired();
        
        int count = 0;
        for (Invitation invitation : expiredInvitations) {
            if (invitationDao.updateStatus(invitation.getId(), InvitationStatus.EXPIRED)) {
                count++;
                LOGGER.log(Level.INFO, "Marked invitation ID: {0} as expired", invitation.getId());
            }
        }
        
        return count;
    }
    
    @Override
    public String generateInvitationToken() {
        byte[] randomBytes = new byte[TOKEN_LENGTH_BYTES];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
    
    /**
     * Send an invitation email to a user.
     * 
     * @param user The user to send the invitation to
     * @param invitation The invitation to send
     * @throws EmailException if an error occurs sending the email
     */
    private void sendInvitationEmail(User user, Invitation invitation) throws EmailException {
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("User email is required to send invitation");
        }
        
        String subject = "Invitation to join Celestra";
        
        // Build the invitation URL
        String invitationUrl = buildInvitationUrl(invitation.getToken());
        
        // Build the email body
        String htmlBody = buildInvitationEmailBody(user, invitation, invitationUrl);
        
        // Send the email
        emailService.sendHtmlEmail(user.getEmail(), subject, htmlBody);
    }
    
    /**
     * Build the invitation URL.
     * 
     * @param token The invitation token
     * @return The invitation URL
     */
    private String buildInvitationUrl(String token) {
        // In a real application, this would be configured based on the deployment environment
        return "https://app.celestra.com/accept-invitation?token=" + token;
    }
    
    /**
     * Build the invitation email body.
     * 
     * @param user The user being invited
     * @param invitation The invitation
     * @param invitationUrl The invitation URL
     * @return The HTML email body
     */
    private String buildInvitationEmailBody(User user, Invitation invitation, String invitationUrl) {
        StringBuilder builder = new StringBuilder();
        
        builder.append("<html><body>");
        builder.append("<h1>Welcome to Celestra</h1>");
        builder.append("<p>Hello ");
        builder.append(user.getName() != null ? user.getName() : user.getEmail());
        builder.append(",</p>");
        builder.append("<p>You have been invited to join Celestra. Please click the button below to accept the invitation and set up your account:</p>");
        builder.append("<p><a href=\"");
        builder.append(invitationUrl);
        builder.append("\" style=\"display: inline-block; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 4px;\">Accept Invitation</a></p>");
        builder.append("<p>This invitation will expire in ");
        builder.append(config.getInvitationTokenExpirationDays());
        builder.append(" days.</p>");
        builder.append("<p>If you did not expect this invitation, please ignore this email.</p>");
        builder.append("<p>Thank you,<br>The Celestra Team</p>");
        builder.append("</body></html>");
        
        return builder.toString();
    }
}