package com.celestra.auth.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.celestra.auth.config.AuthConfigProvider;
import com.celestra.auth.service.AuditService;
import com.celestra.auth.service.InvitationAcceptanceService;
import com.celestra.auth.service.InvitationService;
import com.celestra.dao.UserDao;
import com.celestra.email.EmailService;
import com.celestra.email.exception.EmailException;
import com.celestra.enums.InvitationStatus;
import com.celestra.enums.UserStatus;
import com.celestra.model.AuditLog;
import com.celestra.model.Invitation;
import com.celestra.model.User;

/**
 * Test class for InvitationAcceptanceServiceImpl.
 */
public class InvitationAcceptanceServiceImplTest {
    
    @Mock
    private InvitationService invitationService;
    
    @Mock
    private UserDao userDao;
    
    @Mock
    private AuditService auditService;
    
    @Mock
    private EmailService emailService;
    
    @Mock
    private AuthConfigProvider config;
    
    private InvitationAcceptanceService invitationAcceptanceService;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        invitationAcceptanceService = new InvitationAcceptanceServiceImpl(
                invitationService, userDao, auditService, emailService, config);
        
        // Configure default behavior
        when(config.getPasswordMinLength()).thenReturn(8);
        when(config.getPasswordMaxLength()).thenReturn(64);
        when(config.isPasswordUppercaseRequired()).thenReturn(true);
        when(config.isPasswordLowercaseRequired()).thenReturn(true);
        when(config.isPasswordDigitRequired()).thenReturn(true);
        when(config.isPasswordSpecialCharRequired()).thenReturn(true);
        when(config.getPasswordSpecialChars()).thenReturn("!@#$%^&*()_+-=[]{}|;:,.<>?");
    }
    
    @Test
    public void testValidateInvitationToken() throws SQLException {
        // Arrange
        String token = "valid-token";
        
        Invitation invitation = new Invitation();
        invitation.setId(1);
        invitation.setUserId(1);
        invitation.setToken(token);
        invitation.setStatus(InvitationStatus.SENT);
        
        when(invitationService.validateInvitationToken(token)).thenReturn(Optional.of(invitation));
        
        // Act
        Optional<Invitation> result = invitationAcceptanceService.validateInvitationToken(token);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(invitation, result.get());
        
        // Verify interactions
        verify(invitationService).validateInvitationToken(token);
    }
    
    @Test
    public void testGetUserFromInvitation() throws SQLException {
        // Arrange
        Invitation invitation = new Invitation();
        invitation.setId(1);
        invitation.setUserId(1);
        
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        
        when(userDao.findById(1)).thenReturn(Optional.of(user));
        
        // Act
        Optional<User> result = invitationAcceptanceService.getUserFromInvitation(invitation);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        
        // Verify interactions
        verify(userDao).findById(1);
    }
    
    @Test
    public void testCompleteAccountSetup_Success() throws SQLException {
        // Arrange
        String token = "valid-token";
        String password = "Password123!";
        String confirmPassword = "Password123!";
        Map<String, String> userDetails = new HashMap<>();
        userDetails.put("name", "John Doe");
        String ipAddress = "127.0.0.1";
        
        Invitation invitation = new Invitation();
        invitation.setId(1);
        invitation.setUserId(1);
        invitation.setToken(token);
        invitation.setStatus(InvitationStatus.SENT);
        
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        user.setStatus(UserStatus.SUSPENDED);
        
        User updatedUser = new User();
        updatedUser.setId(1);
        updatedUser.setEmail("test@example.com");
        updatedUser.setName("John Doe");
        updatedUser.setStatus(UserStatus.ACTIVE);
        updatedUser.setPasswordHash("salt:hash");
        
        when(invitationService.validateInvitationToken(token)).thenReturn(Optional.of(invitation));
        when(userDao.findById(1)).thenReturn(Optional.of(user));
        when(userDao.update(any(User.class))).thenReturn(updatedUser);
        when(invitationService.acceptInvitation(token, ipAddress)).thenReturn(true);
        when(auditService.recordUserUpdate(any(), any(), any(), any(), any())).thenReturn(new AuditLog());
        
        // Act
        Optional<User> result = invitationAcceptanceService.completeAccountSetup(
                token, password, confirmPassword, userDetails, ipAddress);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(updatedUser, result.get());
        
        // Verify interactions
        verify(invitationService).validateInvitationToken(token);
        verify(userDao).findById(1);
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDao).update(userCaptor.capture());
        
        User capturedUser = userCaptor.getValue();
        assertEquals("John Doe", capturedUser.getName());
        assertEquals(UserStatus.ACTIVE, capturedUser.getStatus());
        assertNotNull(capturedUser.getPasswordHash());
        
        verify(invitationService).acceptInvitation(token, ipAddress);
        verify(auditService).recordUserUpdate(any(), eq(ipAddress), any(), any(), any());
    }
    
    @Test
    public void testCompleteAccountSetup_InvalidToken() throws SQLException {
        // Arrange
        String token = "invalid-token";
        String password = "Password123!";
        String confirmPassword = "Password123!";
        Map<String, String> userDetails = new HashMap<>();
        String ipAddress = "127.0.0.1";
        
        when(invitationService.validateInvitationToken(token)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            invitationAcceptanceService.completeAccountSetup(
                    token, password, confirmPassword, userDetails, ipAddress);
        });
        
        assertEquals("Invalid or expired invitation token", exception.getMessage());
        
        // Verify interactions
        verify(invitationService).validateInvitationToken(token);
        verify(userDao, never()).findById(anyInt());
        verify(userDao, never()).update(any(User.class));
        verify(invitationService, never()).acceptInvitation(anyString(), anyString());
    }
    
    @Test
    public void testCompleteAccountSetup_PasswordMismatch() throws SQLException {
        // Arrange
        String token = "valid-token";
        String password = "Password123!";
        String confirmPassword = "DifferentPassword123!";
        Map<String, String> userDetails = new HashMap<>();
        String ipAddress = "127.0.0.1";
        
        Invitation invitation = new Invitation();
        invitation.setId(1);
        invitation.setUserId(1);
        invitation.setToken(token);
        invitation.setStatus(InvitationStatus.SENT);
        
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        
        when(invitationService.validateInvitationToken(token)).thenReturn(Optional.of(invitation));
        when(userDao.findById(1)).thenReturn(Optional.of(user));
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            invitationAcceptanceService.completeAccountSetup(
                    token, password, confirmPassword, userDetails, ipAddress);
        });
        
        assertEquals("Passwords do not match", exception.getMessage());
        
        // Verify interactions
        verify(invitationService).validateInvitationToken(token);
        verify(userDao).findById(1);
        verify(userDao, never()).update(any(User.class));
        verify(invitationService, never()).acceptInvitation(anyString(), anyString());
    }
    
    @Test
    public void testCompleteAccountSetup_InvalidPassword() throws SQLException {
        // Arrange
        String token = "valid-token";
        String password = "weak";
        String confirmPassword = "weak";
        Map<String, String> userDetails = new HashMap<>();
        String ipAddress = "127.0.0.1";
        
        Invitation invitation = new Invitation();
        invitation.setId(1);
        invitation.setUserId(1);
        invitation.setToken(token);
        invitation.setStatus(InvitationStatus.SENT);
        
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        
        when(invitationService.validateInvitationToken(token)).thenReturn(Optional.of(invitation));
        when(userDao.findById(1)).thenReturn(Optional.of(user));
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            invitationAcceptanceService.completeAccountSetup(
                    token, password, confirmPassword, userDetails, ipAddress);
        });
        
        assertEquals("Password does not meet requirements", exception.getMessage());
        
        // Verify interactions
        verify(invitationService).validateInvitationToken(token);
        verify(userDao).findById(1);
        verify(userDao, never()).update(any(User.class));
        verify(invitationService, never()).acceptInvitation(anyString(), anyString());
    }
    
    @Test
    public void testActivateUserAccount() throws SQLException {
        // Arrange
        Integer userId = 1;
        String ipAddress = "127.0.0.1";
        
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setStatus(UserStatus.SUSPENDED);
        
        when(userDao.findById(userId)).thenReturn(Optional.of(user));
        when(userDao.update(any(User.class))).thenReturn(user);
        when(auditService.recordUserStatusChange(any(), any(), any(), any(), any(), any()))
                .thenReturn(new AuditLog());
        
        // Act
        boolean result = invitationAcceptanceService.activateUserAccount(userId, ipAddress);
        
        // Assert
        assertTrue(result);
        
        // Verify interactions
        verify(userDao).findById(userId);
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDao).update(userCaptor.capture());
        
        User capturedUser = userCaptor.getValue();
        assertEquals(UserStatus.ACTIVE, capturedUser.getStatus());
        
        verify(auditService).recordUserStatusChange(
                eq(user), 
                eq(ipAddress), 
                eq(user), 
                eq("SUSPENDED"), 
                eq("ACTIVE"), 
                eq("Account activated after invitation acceptance"));
    }
    
    @Test
    public void testSendWelcomeEmail() throws EmailException {
        // Arrange
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        user.setName("John Doe");
        
        // Act
        invitationAcceptanceService.sendWelcomeEmail(user);
        
        // Verify interactions
        verify(emailService).sendHtmlEmail(eq("test@example.com"), eq("Welcome to Celestra"), anyString());
    }
    
    @Test
    public void testValidatePassword_Valid() {
        // Arrange
        String password = "Password123!";
        
        // Act
        boolean result = invitationAcceptanceService.validatePassword(password);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    public void testValidatePassword_TooShort() {
        // Arrange
        String password = "Pass1!";
        
        // Act
        boolean result = invitationAcceptanceService.validatePassword(password);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    public void testValidatePassword_NoUppercase() {
        // Arrange
        String password = "password123!";
        
        // Act
        boolean result = invitationAcceptanceService.validatePassword(password);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    public void testValidatePassword_NoLowercase() {
        // Arrange
        String password = "PASSWORD123!";
        
        // Act
        boolean result = invitationAcceptanceService.validatePassword(password);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    public void testValidatePassword_NoDigit() {
        // Arrange
        String password = "Password!";
        
        // Act
        boolean result = invitationAcceptanceService.validatePassword(password);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    public void testValidatePassword_NoSpecialChar() {
        // Arrange
        String password = "Password123";
        
        // Act
        boolean result = invitationAcceptanceService.validatePassword(password);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    public void testGetPasswordRequirements() {
        // Arrange
        String password = "Password123!";
        
        // Act
        Map<String, Boolean> result = invitationAcceptanceService.getPasswordRequirements(password);
        
        // Assert
        assertEquals(5, result.size());
        assertTrue(result.get("Length (min 8, max 64)"));
        assertTrue(result.get("At least one uppercase letter"));
        assertTrue(result.get("At least one lowercase letter"));
        assertTrue(result.get("At least one digit"));
        assertTrue(result.get("At least one special character"));
    }
}