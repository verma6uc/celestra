package com.celestra.auth.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.celestra.auth.config.AuthConfigProvider;
import com.celestra.auth.service.AuditService;
import com.celestra.auth.service.InvitationService;
import com.celestra.dao.InvitationDao;
import com.celestra.dao.UserDao;
import com.celestra.email.EmailService;
import com.celestra.email.exception.EmailException;
import com.celestra.enums.AuditEventType;
import com.celestra.enums.InvitationStatus;
import com.celestra.model.AuditLog;
import com.celestra.model.Invitation;
import com.celestra.model.User;

/**
 * Test class for InvitationServiceImpl.
 */
public class InvitationServiceImplTest {
    
    @Mock
    private InvitationDao invitationDao;
    
    @Mock
    private UserDao userDao;
    
    @Mock
    private EmailService emailService;
    
    @Mock
    private AuditService auditService;
    
    @Mock
    private AuthConfigProvider config;
    
    private InvitationService invitationService;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        invitationService = new InvitationServiceImpl(invitationDao, userDao, emailService, auditService, config);
        
        // Configure default behavior
        when(config.getInvitationTokenExpirationDays()).thenReturn(7);
    }
    
    @Test
    public void testCreateInvitation() throws SQLException, EmailException {
        // Arrange
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        
        Integer inviterUserId = 2;
        String ipAddress = "127.0.0.1";
        
        when(userDao.findById(1)).thenReturn(Optional.of(user));
        
        Invitation createdInvitation = new Invitation();
        createdInvitation.setId(1);
        createdInvitation.setUserId(1);
        createdInvitation.setToken("test-token");
        createdInvitation.setStatus(InvitationStatus.PENDING);
        
        when(invitationDao.create(any(Invitation.class))).thenReturn(createdInvitation);
        when(auditService.recordSecurityEvent(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new AuditLog());
        
        // Act
        Invitation result = invitationService.createInvitation(user, inviterUserId, ipAddress);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(1, result.getUserId());
        assertEquals(InvitationStatus.PENDING, result.getStatus());
        
        // Verify interactions
        verify(userDao).findById(1);
        verify(invitationDao).create(any(Invitation.class));
        verify(auditService).recordSecurityEvent(
                eq(AuditEventType.OTHER), 
                eq(user), 
                eq(ipAddress), 
                eq("Invitation created"), 
                eq("invitations"), 
                eq("1"), 
                isNull());
    }
    
    @Test
    public void testSendInvitation() throws SQLException, EmailException {
        // Arrange
        Integer invitationId = 1;
        Integer inviterUserId = 2;
        String ipAddress = "127.0.0.1";
        
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        
        Invitation invitation = new Invitation();
        invitation.setId(invitationId);
        invitation.setUserId(1);
        invitation.setToken("test-token");
        invitation.setStatus(InvitationStatus.PENDING);
        
        when(invitationDao.findById(invitationId)).thenReturn(Optional.of(invitation));
        when(userDao.findById(1)).thenReturn(Optional.of(user));
        
        Invitation updatedInvitation = new Invitation();
        updatedInvitation.setId(invitationId);
        updatedInvitation.setUserId(1);
        updatedInvitation.setToken("test-token");
        updatedInvitation.setStatus(InvitationStatus.SENT);
        updatedInvitation.setSentAt(new Timestamp(System.currentTimeMillis()));
        
        when(invitationDao.update(any(Invitation.class))).thenReturn(updatedInvitation);
        when(auditService.recordSecurityEvent(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new AuditLog());
        
        // Act
        Invitation result = invitationService.sendInvitation(invitationId, inviterUserId, ipAddress);
        
        // Assert
        assertNotNull(result);
        assertEquals(invitationId, result.getId());
        assertEquals(InvitationStatus.SENT, result.getStatus());
        assertNotNull(result.getSentAt());
        
        // Verify interactions
        verify(invitationDao).findById(invitationId);
        verify(userDao).findById(1);
        verify(emailService).sendHtmlEmail(eq("test@example.com"), anyString(), anyString());
        verify(invitationDao).update(any(Invitation.class));
        verify(auditService).recordSecurityEvent(
                eq(AuditEventType.OTHER), 
                eq(user), 
                eq(ipAddress), 
                eq("Invitation sent"), 
                eq("invitations"), 
                eq("1"), 
                isNull());
    }
    
    @Test
    public void testResendInvitation() throws SQLException, EmailException {
        // Arrange
        Integer invitationId = 1;
        Integer inviterUserId = 2;
        String ipAddress = "127.0.0.1";
        
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        
        Invitation invitation = new Invitation();
        invitation.setId(invitationId);
        invitation.setUserId(1);
        invitation.setToken("test-token");
        invitation.setStatus(InvitationStatus.SENT);
        invitation.setResendCount(0);
        
        when(invitationDao.findById(invitationId)).thenReturn(Optional.of(invitation));
        when(userDao.findById(1)).thenReturn(Optional.of(user));
        when(invitationDao.incrementResendCount(invitationId)).thenReturn(true);
        
        Invitation updatedInvitation = new Invitation();
        updatedInvitation.setId(invitationId);
        updatedInvitation.setUserId(1);
        updatedInvitation.setToken("test-token");
        updatedInvitation.setStatus(InvitationStatus.SENT);
        updatedInvitation.setResendCount(1);
        updatedInvitation.setSentAt(new Timestamp(System.currentTimeMillis()));
        
        when(invitationDao.update(any(Invitation.class))).thenReturn(updatedInvitation);
        when(auditService.recordSecurityEvent(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new AuditLog());
        
        // Act
        Invitation result = invitationService.resendInvitation(invitationId, inviterUserId, ipAddress);
        
        // Assert
        assertNotNull(result);
        assertEquals(invitationId, result.getId());
        assertEquals(InvitationStatus.SENT, result.getStatus());
        assertEquals(1, result.getResendCount());
        assertNotNull(result.getSentAt());
        
        // Verify interactions
        verify(invitationDao).findById(invitationId);
        verify(userDao).findById(1);
        verify(invitationDao).incrementResendCount(invitationId);
        verify(emailService).sendHtmlEmail(eq("test@example.com"), anyString(), anyString());
        verify(invitationDao).update(any(Invitation.class));
        verify(auditService).recordSecurityEvent(
                eq(AuditEventType.OTHER), 
                eq(user), 
                eq(ipAddress), 
                eq("Invitation resent"), 
                eq("invitations"), 
                eq("1"), 
                isNull());
    }
    
    @Test
    public void testCancelInvitation() throws SQLException {
        // Arrange
        Integer invitationId = 1;
        Integer cancellerUserId = 2;
        String ipAddress = "127.0.0.1";
        String reason = "No longer needed";
        
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        
        Invitation invitation = new Invitation();
        invitation.setId(invitationId);
        invitation.setUserId(1);
        invitation.setToken("test-token");
        invitation.setStatus(InvitationStatus.SENT);
        
        when(invitationDao.findById(invitationId)).thenReturn(Optional.of(invitation));
        when(userDao.findById(1)).thenReturn(Optional.of(user));
        when(invitationDao.updateStatus(invitationId, InvitationStatus.CANCELLED)).thenReturn(true);
        when(auditService.recordSecurityEvent(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new AuditLog());
        
        // Act
        boolean result = invitationService.cancelInvitation(invitationId, cancellerUserId, ipAddress, reason);
        
        // Assert
        assertTrue(result);
        
        // Verify interactions
        verify(invitationDao).findById(invitationId);
        verify(userDao).findById(1);
        verify(invitationDao).updateStatus(invitationId, InvitationStatus.CANCELLED);
        verify(auditService).recordSecurityEvent(
                eq(AuditEventType.OTHER), 
                eq(user), 
                eq(ipAddress), 
                eq("Invitation cancelled: No longer needed"), 
                eq("invitations"), 
                eq("1"), 
                isNull());
    }
    
    @Test
    public void testValidateInvitationToken_Valid() throws SQLException {
        // Arrange
        String token = "valid-token";
        
        User user = new User();
        user.setId(1);
        
        Invitation invitation = new Invitation();
        invitation.setId(1);
        invitation.setUserId(1);
        invitation.setToken(token);
        invitation.setStatus(InvitationStatus.SENT);
        invitation.setExpiresAt(Timestamp.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        
        when(invitationDao.findByToken(token)).thenReturn(Optional.of(invitation));
        
        // Act
        Optional<Invitation> result = invitationService.validateInvitationToken(token);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(invitation, result.get());
        
        // Verify interactions
        verify(invitationDao).findByToken(token);
    }
    
    @Test
    public void testValidateInvitationToken_Invalid() throws SQLException {
        // Arrange
        String token = "invalid-token";
        
        when(invitationDao.findByToken(token)).thenReturn(Optional.empty());
        
        // Act
        Optional<Invitation> result = invitationService.validateInvitationToken(token);
        
        // Assert
        assertFalse(result.isPresent());
        
        // Verify interactions
        verify(invitationDao).findByToken(token);
    }
    
    @Test
    public void testValidateInvitationToken_Expired() throws SQLException {
        // Arrange
        String token = "expired-token";
        
        Invitation invitation = new Invitation();
        invitation.setId(1);
        invitation.setUserId(1);
        invitation.setToken(token);
        invitation.setStatus(InvitationStatus.SENT);
        invitation.setExpiresAt(Timestamp.from(Instant.now().minus(1, ChronoUnit.DAYS)));
        
        when(invitationDao.findByToken(token)).thenReturn(Optional.of(invitation));
        
        // Act
        Optional<Invitation> result = invitationService.validateInvitationToken(token);
        
        // Assert
        assertFalse(result.isPresent());
        
        // Verify interactions
        verify(invitationDao).findByToken(token);
        verify(invitationDao).updateStatus(1, InvitationStatus.EXPIRED);
    }
    
    @Test
    public void testAcceptInvitation() throws SQLException {
        // Arrange
        String token = "valid-token";
        String ipAddress = "127.0.0.1";
        
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        
        Invitation invitation = new Invitation();
        invitation.setId(1);
        invitation.setUserId(1);
        invitation.setToken(token);
        invitation.setStatus(InvitationStatus.SENT);
        invitation.setExpiresAt(Timestamp.from(Instant.now().plus(1, ChronoUnit.DAYS)));
        
        when(invitationDao.findByToken(token)).thenReturn(Optional.of(invitation));
        when(userDao.findById(1)).thenReturn(Optional.of(user));
        when(invitationDao.updateStatus(1, InvitationStatus.ACCEPTED)).thenReturn(true);
        when(auditService.recordSecurityEvent(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new AuditLog());
        
        // Act
        boolean result = invitationService.acceptInvitation(token, ipAddress);
        
        // Assert
        assertTrue(result);
        
        // Verify interactions
        verify(invitationDao).findByToken(token);
        verify(userDao).findById(1);
        verify(invitationDao).updateStatus(1, InvitationStatus.ACCEPTED);
        verify(auditService).recordSecurityEvent(
                eq(AuditEventType.OTHER), 
                eq(user), 
                eq(ipAddress), 
                eq("Invitation accepted"), 
                eq("invitations"), 
                eq("1"), 
                isNull());
    }
    
    @Test
    public void testGetInvitationsByUserId() throws SQLException {
        // Arrange
        Integer userId = 1;
        
        List<Invitation> invitations = new ArrayList<>();
        invitations.add(new Invitation(1, userId, "token1", InvitationStatus.SENT, null, null, 0, null, null));
        invitations.add(new Invitation(2, userId, "token2", InvitationStatus.EXPIRED, null, null, 0, null, null));
        
        when(invitationDao.findByUserId(userId)).thenReturn(invitations);
        
        // Act
        List<Invitation> result = invitationService.getInvitationsByUserId(userId);
        
        // Assert
        assertEquals(2, result.size());
        
        // Verify interactions
        verify(invitationDao).findByUserId(userId);
    }
    
    @Test
    public void testGetInvitationsByStatus() throws SQLException {
        // Arrange
        InvitationStatus status = InvitationStatus.SENT;
        
        List<Invitation> invitations = new ArrayList<>();
        invitations.add(new Invitation(1, 1, "token1", status, null, null, 0, null, null));
        invitations.add(new Invitation(2, 2, "token2", status, null, null, 0, null, null));
        
        when(invitationDao.findByStatus(status)).thenReturn(invitations);
        
        // Act
        List<Invitation> result = invitationService.getInvitationsByStatus(status);
        
        // Assert
        assertEquals(2, result.size());
        
        // Verify interactions
        verify(invitationDao).findByStatus(status);
    }
    
    @Test
    public void testCleanupExpiredInvitations() throws SQLException {
        // Arrange
        List<Invitation> expiredInvitations = new ArrayList<>();
        expiredInvitations.add(new Invitation(1, 1, "token1", InvitationStatus.SENT, null, null, 0, null, null));
        expiredInvitations.add(new Invitation(2, 2, "token2", InvitationStatus.SENT, null, null, 0, null, null));
        
        when(invitationDao.findExpired()).thenReturn(expiredInvitations);
        when(invitationDao.updateStatus(1, InvitationStatus.EXPIRED)).thenReturn(true);
        when(invitationDao.updateStatus(2, InvitationStatus.EXPIRED)).thenReturn(true);
        
        // Act
        int result = invitationService.cleanupExpiredInvitations();
        
        // Assert
        assertEquals(2, result);
        
        // Verify interactions
        verify(invitationDao).findExpired();
        verify(invitationDao).updateStatus(1, InvitationStatus.EXPIRED);
        verify(invitationDao).updateStatus(2, InvitationStatus.EXPIRED);
    }
    
    @Test
    public void testGenerateInvitationToken() {
        // Act
        String token1 = invitationService.generateInvitationToken();
        String token2 = invitationService.generateInvitationToken();
        
        // Assert
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2); // Tokens should be unique
        assertTrue(token1.length() >= 32); // Token should be at least 32 characters
    }
}