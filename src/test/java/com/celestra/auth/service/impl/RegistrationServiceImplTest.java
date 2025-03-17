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
import com.celestra.auth.service.RegistrationService;
import com.celestra.dao.AuditLogDao;
import com.celestra.dao.InvitationDao;
import com.celestra.dao.UserDao;
import com.celestra.email.EmailService;
import com.celestra.email.exception.EmailException;
import com.celestra.enums.InvitationStatus;
import com.celestra.enums.UserRole;
import com.celestra.enums.UserStatus;
import com.celestra.model.AuditLog;
import com.celestra.model.Invitation;
import com.celestra.model.User;

/**
 * Test class for RegistrationServiceImpl.
 */
public class RegistrationServiceImplTest {
    
    @Mock
    private UserDao userDao;
    
    @Mock
    private InvitationDao invitationDao;
    
    @Mock
    private AuthConfigProvider config;
    
    @Mock
    private EmailService emailService;
    
    @Mock
    private AuditLogDao auditLogDao;
    
    @Mock
    private AuditService auditService;
    
    private RegistrationService registrationService;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Configure default behavior for config
        when(config.getPasswordMinLength()).thenReturn(8);
        when(config.getPasswordMaxLength()).thenReturn(64);
        when(config.isPasswordUppercaseRequired()).thenReturn(true);
        when(config.isPasswordLowercaseRequired()).thenReturn(true);
        when(config.isPasswordDigitRequired()).thenReturn(true);
        when(config.isPasswordSpecialCharRequired()).thenReturn(true);
        when(config.getPasswordSpecialChars()).thenReturn("!@#$%^&*()_+-=[]{}|;:,.<>?");
        when(config.isSelfRegistrationAllowed()).thenReturn(true);
        when(config.isEmailVerificationRequired()).thenReturn(false);
        when(config.getEmailVerificationExpirationHours()).thenReturn(24);
        
        registrationService = new RegistrationServiceImpl(
                userDao, invitationDao, config, emailService, auditLogDao, auditService);
    }
    
    @Test
    public void testRegisterUser_Success() throws SQLException, EmailException {
        // Arrange
        String email = "test@example.com";
        String name = "Test User";
        String password = "Password123!";
        UserRole role = UserRole.REGULAR_USER;
        Integer companyId = 1;
        String ipAddress = "127.0.0.1";
        Map<String, String> metadata = new HashMap<>();
        
        User user = new User();
        user.setId(1);
        user.setEmail(email);
        user.setName(name);
        user.setRole(role);
        user.setCompanyId(companyId);
        user.setStatus(UserStatus.ACTIVE);
        
        when(userDao.findByEmail(email)).thenReturn(Optional.empty()).thenReturn(Optional.of(user));
        when(userDao.create(any(User.class))).thenReturn(user);
        when(auditService.recordUserCreation(any(User.class), anyString(), any())).thenReturn(new AuditLog());
        
        // Act
        User result = registrationService.registerUser(email, name, password, role, companyId, ipAddress, metadata);
        
        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(name, result.getName());
        assertEquals(role, result.getRole());
        assertEquals(companyId, result.getCompanyId());
        assertEquals(UserStatus.ACTIVE, result.getStatus());
        
        // Verify interactions
        verify(userDao, times(2)).findByEmail(email);
        verify(userDao).create(any(User.class));
        verify(userDao).addPasswordToHistory(eq(1), anyString());
        verify(auditService).recordUserCreation(any(User.class), eq(ipAddress), any());
        verify(emailService).sendPlainTextEmail(eq(email), anyString(), anyString());
    }
    
    @Test
    public void testRegisterUser_EmailAlreadyInUse() throws SQLException {
        // Arrange
        String email = "test@example.com";
        String name = "Test User";
        String password = "Password123!";
        UserRole role = UserRole.REGULAR_USER;
        Integer companyId = 1;
        String ipAddress = "127.0.0.1";
        Map<String, String> metadata = new HashMap<>();
        
        User existingUser = new User();
        existingUser.setId(1);
        existingUser.setEmail(email);
        
        when(userDao.findByEmail(email)).thenReturn(Optional.of(existingUser));
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.registerUser(email, name, password, role, companyId, ipAddress, metadata);
        });
        
        assertEquals("Email is already in use", exception.getMessage());
        
        // Verify interactions
        verify(userDao).findByEmail(email);
        verify(userDao, never()).create(any(User.class));
        verify(userDao, never()).addPasswordToHistory(anyInt(), anyString());
        try {
            verify(auditService, never()).recordUserCreation(any(), anyString(), any());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        try {
            verify(emailService, never()).sendPlainTextEmail(anyString(), anyString(), anyString());
        } catch (EmailException e) {
            fail("Unexpected EmailException: " + e.getMessage());
        }
    }
    
    @Test
    public void testRegisterUser_InvalidPassword() throws SQLException {
        // Arrange
        String email = "test@example.com";
        String name = "Test User";
        String password = "weak";
        UserRole role = UserRole.REGULAR_USER;
        Integer companyId = 1;
        String ipAddress = "127.0.0.1";
        Map<String, String> metadata = new HashMap<>();
        
        when(userDao.findByEmail(email)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.registerUser(email, name, password, role, companyId, ipAddress, metadata);
        });
        
        assertTrue(exception.getMessage().contains("Password does not meet complexity requirements"));
        
        // Verify interactions
        verify(userDao).findByEmail(email);
        verify(userDao, never()).create(any(User.class));
        verify(userDao, never()).addPasswordToHistory(anyInt(), anyString());
        try {
            verify(auditService, never()).recordUserCreation(any(), anyString(), any());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        try {
            verify(emailService, never()).sendPlainTextEmail(anyString(), anyString(), anyString());
        } catch (EmailException e) {
            fail("Unexpected EmailException: " + e.getMessage());
        }
    }
    
    @Test
    public void testRegisterUser_SuperAdminWithCompanyId() throws SQLException {
        // Arrange
        String email = "admin@example.com";
        String name = "Admin User";
        String password = "Password123!";
        UserRole role = UserRole.SUPER_ADMIN;
        Integer companyId = 1; // This should cause an error for super admin
        String ipAddress = "127.0.0.1";
        Map<String, String> metadata = new HashMap<>();
        
        when(userDao.findByEmail(email)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.registerUser(email, name, password, role, companyId, ipAddress, metadata);
        });
        
        assertEquals("Super admins cannot be associated with a company", exception.getMessage());
        
        // Verify interactions
        verify(userDao).findByEmail(email);
        verify(userDao, never()).create(any(User.class));
        verify(userDao, never()).addPasswordToHistory(anyInt(), anyString());
        try {
            verify(auditService, never()).recordUserCreation(any(), anyString(), any());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        try {
            verify(emailService, never()).sendPlainTextEmail(anyString(), anyString(), anyString());
        } catch (EmailException e) {
            fail("Unexpected EmailException: " + e.getMessage());
        }
    }
    
    @Test
    public void testRegisterUser_NonSuperAdminWithoutCompanyId() throws SQLException {
        // Arrange
        String email = "user@example.com";
        String name = "Regular User";
        String password = "Password123!";
        UserRole role = UserRole.REGULAR_USER;
        Integer companyId = null; // This should cause an error for non-super admin
        String ipAddress = "127.0.0.1";
        Map<String, String> metadata = new HashMap<>();
        
        when(userDao.findByEmail(email)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.registerUser(email, name, password, role, companyId, ipAddress, metadata);
        });
        
        assertEquals("Non-super admin users must be associated with a company", exception.getMessage());
        
        // Verify interactions
        verify(userDao).findByEmail(email);
        verify(userDao, never()).create(any(User.class));
        verify(userDao, never()).addPasswordToHistory(anyInt(), anyString());
        try {
            verify(auditService, never()).recordUserCreation(any(), anyString(), any());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        try {
            verify(emailService, never()).sendPlainTextEmail(anyString(), anyString(), anyString());
        } catch (EmailException e) {
            fail("Unexpected EmailException: " + e.getMessage());
        }
    }
    
    @Test
    public void testRegisterUserViaInvitation_Success() throws SQLException, EmailException {
        // Arrange
        String invitationToken = "valid-token";
        String name = "Test User";
        String password = "Password123!";
        String ipAddress = "127.0.0.1";
        Map<String, String> metadata = new HashMap<>();
        
        Invitation invitation = new Invitation();
        invitation.setId(1);
        invitation.setUserId(1);
        invitation.setToken(invitationToken);
        invitation.setStatus(InvitationStatus.SENT);
        invitation.setExpiresAt(Timestamp.from(Instant.now().plusSeconds(3600)));
        
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        user.setStatus(UserStatus.SUSPENDED);
        
        User updatedUser = new User();
        updatedUser.setId(1);
        updatedUser.setEmail("test@example.com");
        updatedUser.setName(name);
        updatedUser.setStatus(UserStatus.ACTIVE);
        
        when(invitationDao.findByToken(invitationToken)).thenReturn(Optional.of(invitation));
        when(userDao.findById(1)).thenReturn(Optional.of(user)).thenReturn(Optional.of(updatedUser));
        when(userDao.update(any(User.class))).thenReturn(updatedUser);
        when(invitationDao.update(any(Invitation.class))).thenReturn(invitation);
        when(auditService.recordSecurityEvent(any(), any(), anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(new AuditLog());
        
        // Act
        User result = registrationService.registerUserViaInvitation(invitationToken, name, password, ipAddress, metadata);
        
        // Assert
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(UserStatus.ACTIVE, result.getStatus());
        
        // Verify interactions
        verify(invitationDao).findByToken(invitationToken);
        verify(userDao, times(2)).findById(1);
        verify(userDao).update(any(User.class));
        verify(userDao).addPasswordToHistory(eq(1), anyString());
        verify(invitationDao).update(any(Invitation.class));
        verify(auditService).recordSecurityEvent(any(), any(), eq(ipAddress), anyString(), anyString(), anyString(), any());
        verify(emailService).sendPlainTextEmail(eq("test@example.com"), anyString(), anyString());
    }
    
    @Test
    public void testRegisterUserViaInvitation_InvalidToken() throws SQLException {
        // Arrange
        String invitationToken = "invalid-token";
        String name = "Test User";
        String password = "Password123!";
        String ipAddress = "127.0.0.1";
        Map<String, String> metadata = new HashMap<>();
        
        when(invitationDao.findByToken(invitationToken)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.registerUserViaInvitation(invitationToken, name, password, ipAddress, metadata);
        });
        
        assertEquals("Invalid invitation token", exception.getMessage());
        
        // Verify interactions
        verify(invitationDao).findByToken(invitationToken);
        verify(userDao, never()).findById(anyInt());
        verify(userDao, never()).update(any(User.class));
        verify(userDao, never()).addPasswordToHistory(anyInt(), anyString());
        verify(invitationDao, never()).update(any(Invitation.class));
        try {
            verify(auditService, never()).recordSecurityEvent(any(), any(), any(), anyString(), anyString(), anyString(), any());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        try {
            verify(emailService, never()).sendPlainTextEmail(anyString(), anyString(), anyString());
        } catch (EmailException e) {
            fail("Unexpected EmailException: " + e.getMessage());
        }
    }
    
    @Test
    public void testRegisterUserViaInvitation_ExpiredInvitation() throws SQLException {
        // Arrange
        String invitationToken = "expired-token";
        String name = "Test User";
        String password = "Password123!";
        String ipAddress = "127.0.0.1";
        Map<String, String> metadata = new HashMap<>();
        
        Invitation invitation = new Invitation();
        invitation.setId(1);
        invitation.setUserId(1);
        invitation.setToken(invitationToken);
        invitation.setStatus(InvitationStatus.SENT);
        invitation.setExpiresAt(Timestamp.from(Instant.now().minusSeconds(3600))); // Expired
        
        when(invitationDao.findByToken(invitationToken)).thenReturn(Optional.of(invitation));
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.registerUserViaInvitation(invitationToken, name, password, ipAddress, metadata);
        });
        
        assertEquals("Invitation has expired", exception.getMessage());
        
        // Verify interactions
        verify(invitationDao).findByToken(invitationToken);
        verify(userDao, never()).findById(anyInt());
        verify(userDao, never()).update(any(User.class));
        verify(userDao, never()).addPasswordToHistory(anyInt(), anyString());
        verify(invitationDao, never()).update(any(Invitation.class));
        try {
            verify(auditService, never()).recordSecurityEvent(any(), any(), any(), anyString(), anyString(), anyString(), any());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
        
        try {
            verify(emailService, never()).sendPlainTextEmail(anyString(), anyString(), anyString());
        } catch (EmailException e) {
            fail("Unexpected EmailException: " + e.getMessage());
        }
    }
    
    @Test
    public void testValidateEmail_Valid() {
        // Arrange
        String email = "test@example.com";
        
        // Act
        boolean result = registrationService.validateEmail(email);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    public void testValidateEmail_Invalid() {
        // Arrange
        String email = "invalid-email";
        
        // Act
        boolean result = registrationService.validateEmail(email);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    public void testValidatePassword_Valid() {
        // Arrange
        String password = "Password123!";
        
        // Act
        Map<String, Boolean> result = registrationService.validatePassword(password);
        
        // Assert
        assertFalse(result.containsValue(false));
    }
    
    @Test
    public void testValidatePassword_TooShort() {
        // Arrange
        String password = "Pass1!";
        
        // Act
        Map<String, Boolean> result = registrationService.validatePassword(password);
        
        // Assert
        assertTrue(result.containsValue(false));
        assertFalse(result.get("length"));
    }
    
    @Test
    public void testIsEmailInUse_True() throws SQLException {
        // Arrange
        String email = "existing@example.com";
        User existingUser = new User();
        existingUser.setId(1);
        existingUser.setEmail(email);
        
        when(userDao.findByEmail(email)).thenReturn(Optional.of(existingUser));
        
        // Act
        boolean result = registrationService.isEmailInUse(email);
        
        // Assert
        assertTrue(result);
        
        // Verify interactions
        verify(userDao).findByEmail(email);
    }
    
    @Test
    public void testIsEmailInUse_False() throws SQLException {
        // Arrange
        String email = "new@example.com";
        
        when(userDao.findByEmail(email)).thenReturn(Optional.empty());
        
        // Act
        boolean result = registrationService.isEmailInUse(email);
        
        // Assert
        assertFalse(result);
        
        // Verify interactions
        verify(userDao).findByEmail(email);
    }
    
    @Test
    public void testVerifyEmail_Success() throws SQLException, EmailException {
        // Arrange
        String token = "valid-token";
        
        Invitation invitation = new Invitation();
        invitation.setId(1);
        invitation.setUserId(1);
        invitation.setToken(token);
        invitation.setStatus(InvitationStatus.SENT);
        invitation.setExpiresAt(Timestamp.from(Instant.now().plusSeconds(3600)));
        
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        user.setStatus(UserStatus.SUSPENDED);
        
        when(invitationDao.findByToken(token)).thenReturn(Optional.of(invitation));
        when(userDao.findById(1)).thenReturn(Optional.of(user));
        when(userDao.update(any(User.class))).thenReturn(user);
        when(invitationDao.update(any(Invitation.class))).thenReturn(invitation);
        when(auditService.recordSecurityEvent(any(), any(), anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(new AuditLog());
        
        // Act
        boolean result = registrationService.verifyEmail(token);
        
        // Assert
        assertTrue(result);
        
        // Verify interactions
        verify(invitationDao).findByToken(token);
        verify(userDao).findById(1);
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDao).update(userCaptor.capture());
        assertEquals(UserStatus.ACTIVE, userCaptor.getValue().getStatus());
        
        ArgumentCaptor<Invitation> invitationCaptor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationDao).update(invitationCaptor.capture());
        assertEquals(InvitationStatus.ACCEPTED, invitationCaptor.getValue().getStatus());
        
        verify(auditService).recordSecurityEvent(any(), any(), isNull(), anyString(), anyString(), anyString(), any());
        verify(emailService).sendPlainTextEmail(eq("test@example.com"), anyString(), anyString());
    }
}