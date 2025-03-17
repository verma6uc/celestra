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
import com.celestra.auth.service.RegistrationService;
import com.celestra.dao.AuditLogDao;
import com.celestra.dao.InvitationDao;
import com.celestra.dao.UserDao;
import com.celestra.email.EmailService;
import com.celestra.email.exception.EmailException;
import com.celestra.enums.AuditEventType;
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
    
    private RegistrationService registrationService;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        registrationService = new RegistrationServiceImpl(userDao, invitationDao, config, emailService, auditLogDao);
        
        // Configure default behavior for config
        when(config.getPasswordMinLength()).thenReturn(8);
        when(config.getPasswordMaxLength()).thenReturn(64);
        when(config.isPasswordUppercaseRequired()).thenReturn(true);
        when(config.isPasswordLowercaseRequired()).thenReturn(true);
        when(config.isPasswordDigitRequired()).thenReturn(true);
        when(config.isPasswordSpecialCharRequired()).thenReturn(true);
        when(config.getPasswordSpecialChars()).thenReturn("!@#$%^&*()_-+={}[]|:;\"'<>,.?/~`");
        when(config.isEmailVerificationRequired()).thenReturn(false);
        when(config.isSelfRegistrationAllowed()).thenReturn(true);
        when(config.getEmailVerificationExpirationHours()).thenReturn(24);
    }
    
    @Test
    public void testRegisterUser_Success() throws SQLException, EmailException {
        // Arrange
        String email = "nupur.bhaisare@leucinetech.com";
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
        verify(auditLogDao).create(any(AuditLog.class));
        verify(emailService).sendPlainTextEmail(eq(email), anyString(), anyString());
    }
    
    @Test
    public void testRegisterUser_WithEmailVerification() throws SQLException, EmailException {
        // Arrange
        String email = "nupur.bhaisare@leucinetech.com";
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
        user.setStatus(UserStatus.SUSPENDED);
        
        when(config.isEmailVerificationRequired()).thenReturn(true);
        when(userDao.findByEmail(email)).thenReturn(Optional.empty()).thenReturn(Optional.of(user));
        when(userDao.create(any(User.class))).thenReturn(user);
        
        // Act
        User result = registrationService.registerUser(email, name, password, role, companyId, ipAddress, metadata);
        
        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(name, result.getName());
        assertEquals(role, result.getRole());
        assertEquals(companyId, result.getCompanyId());
        assertEquals(UserStatus.SUSPENDED, result.getStatus());
        
        // Verify interactions
        verify(userDao, times(2)).findByEmail(email);
        verify(userDao).create(any(User.class));
        verify(userDao).addPasswordToHistory(eq(1), anyString());
        verify(invitationDao).create(any(Invitation.class));
        verify(auditLogDao).create(any(AuditLog.class));
        verify(emailService).sendPlainTextEmail(eq(email), anyString(), anyString());
    }
    
    @Test
    public void testRegisterUser_EmailAlreadyInUse() throws SQLException {
        // Arrange
        String email = "nupur.bhaisare@leucinetech.com";
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
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.registerUser(email, name, password, role, companyId, ipAddress, metadata);
        });
        
        assertEquals("Email is already in use", exception.getMessage());
        
        // Verify interactions
        verify(userDao).findByEmail(email);
        verify(userDao, never()).create(any(User.class));
        verify(auditLogDao, never()).create(any(AuditLog.class));
        verifyNoInteractions(emailService);
    }
    
    @Test
    public void testRegisterUser_InvalidPassword() throws SQLException {
        // Arrange
        String email = "nupur.bhaisare@leucinetech.com";
        String name = "Test User";
        String password = "weak";
        UserRole role = UserRole.REGULAR_USER;
        Integer companyId = 1;
        String ipAddress = "127.0.0.1";
        Map<String, String> metadata = new HashMap<>();
        
        when(userDao.findByEmail(email)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.registerUser(email, name, password, role, companyId, ipAddress, metadata);
        });
        
        assertTrue(exception.getMessage().contains("Password does not meet complexity requirements"));
        
        // Verify interactions
        verify(userDao).findByEmail(email);
        verify(userDao, never()).create(any(User.class));
        verify(auditLogDao, never()).create(any(AuditLog.class));
        verifyNoInteractions(emailService);
    }
    
    @Test
    public void testRegisterUserViaInvitation_Success() throws Exception {
        // Arrange
        String token = "valid-token";
        String name = "Test User";
        String password = "Password123!";
        String ipAddress = "127.0.0.1";
        Map<String, String> metadata = new HashMap<>();
        
        User user = new User();
        user.setId(1);
        user.setEmail("nupur.bhaisare@leucinetech.com");
        user.setStatus(UserStatus.SUSPENDED);
        
        Invitation invitation = new Invitation();
        invitation.setId(1);
        invitation.setUserId(1);
        invitation.setToken(token);
        invitation.setStatus(InvitationStatus.SENT);
        invitation.setExpiresAt(Timestamp.from(Instant.now().plusSeconds(3600)));
        
        when(invitationDao.findByToken(token)).thenReturn(Optional.of(invitation));
        when(userDao.findById(1)).thenReturn(Optional.of(user)).thenReturn(Optional.of(user));
        
        // Act
        User result = registrationService.registerUserViaInvitation(token, name, password, ipAddress, metadata);
        
        // Assert
        assertNotNull(result);
        assertEquals("nupur.bhaisare@leucinetech.com", result.getEmail());
        
        // Verify interactions
        verify(invitationDao).findByToken(token);
        verify(userDao, times(2)).findById(1);
        verify(userDao).update(any(User.class));
        verify(userDao).addPasswordToHistory(eq(1), anyString());
        verify(invitationDao).update(any(Invitation.class));
        verify(auditLogDao).create(any(AuditLog.class));
        verify(emailService).sendPlainTextEmail(eq("nupur.bhaisare@leucinetech.com"), anyString(), anyString());
        
        // Verify user was updated correctly
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDao).update(userCaptor.capture());
        User updatedUser = userCaptor.getValue();
        assertEquals(name, updatedUser.getName());
        assertEquals(UserStatus.ACTIVE, updatedUser.getStatus());
        
        // Verify invitation was updated correctly
        ArgumentCaptor<Invitation> invitationCaptor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationDao).update(invitationCaptor.capture());
        Invitation updatedInvitation = invitationCaptor.getValue();
        assertEquals(InvitationStatus.ACCEPTED, updatedInvitation.getStatus());
    }
    
    @Test
    public void testRegisterUserViaInvitation_ExpiredToken() throws SQLException {
        // Arrange
        String token = "expired-token";
        String name = "Test User";
        String password = "Password123!";
        String ipAddress = "127.0.0.1";
        Map<String, String> metadata = new HashMap<>();
        
        Invitation invitation = new Invitation();
        invitation.setId(1);
        invitation.setUserId(1);
        invitation.setToken(token);
        invitation.setStatus(InvitationStatus.SENT);
        invitation.setExpiresAt(Timestamp.from(Instant.now().minusSeconds(3600)));
        
        when(invitationDao.findByToken(token)).thenReturn(Optional.of(invitation));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.registerUserViaInvitation(token, name, password, ipAddress, metadata);
        });
        
        assertEquals("Invitation has expired", exception.getMessage());
        
        // Verify interactions
        verify(invitationDao).findByToken(token);
        verify(userDao, never()).findById(anyInt());
        verify(userDao, never()).update(any(User.class));
        verify(auditLogDao, never()).create(any(AuditLog.class));
        verifyNoInteractions(emailService);
    }
    
    @Test
    public void testVerifyEmail_Success() throws SQLException, EmailException {
        // Arrange
        String token = "valid-token";
        
        User user = new User();
        user.setId(1);
        user.setEmail("nupur.bhaisare@leucinetech.com");
        user.setStatus(UserStatus.SUSPENDED);
        
        Invitation invitation = new Invitation();
        invitation.setId(1);
        invitation.setUserId(1);
        invitation.setToken(token);
        invitation.setStatus(InvitationStatus.SENT);
        invitation.setExpiresAt(Timestamp.from(Instant.now().plusSeconds(3600)));
        
        when(invitationDao.findByToken(token)).thenReturn(Optional.of(invitation));
        when(userDao.findById(1)).thenReturn(Optional.of(user));
        
        // Act
        boolean result = registrationService.verifyEmail(token);
        
        // Assert
        assertTrue(result);
        
        // Verify interactions
        verify(invitationDao).findByToken(token);
        verify(userDao).findById(1);
        verify(userDao).update(any(User.class));
        verify(invitationDao).update(any(Invitation.class));
        verify(auditLogDao).create(any(AuditLog.class));
        verify(emailService).sendPlainTextEmail(eq("nupur.bhaisare@leucinetech.com"), anyString(), anyString());
        
        // Verify user was updated correctly
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDao).update(userCaptor.capture());
        User updatedUser = userCaptor.getValue();
        assertEquals(UserStatus.ACTIVE, updatedUser.getStatus());
        
        // Verify invitation was updated correctly
        ArgumentCaptor<Invitation> invitationCaptor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationDao).update(invitationCaptor.capture());
        Invitation updatedInvitation = invitationCaptor.getValue();
        assertEquals(InvitationStatus.ACCEPTED, updatedInvitation.getStatus());
    }
    
    @Test
    public void testVerifyEmail_InvalidToken() throws SQLException {
        // Arrange
        String token = "invalid-token";
        
        when(invitationDao.findByToken(token)).thenReturn(Optional.empty());
        
        // Act
        boolean result = registrationService.verifyEmail(token);
        
        // Assert
        assertFalse(result);
        
        // Verify interactions
        verify(invitationDao).findByToken(token);
        verify(userDao, never()).findById(anyInt());
        verify(userDao, never()).update(any(User.class));
        verify(invitationDao, never()).update(any(Invitation.class));
        verify(auditLogDao, never()).create(any(AuditLog.class));
        verifyNoInteractions(emailService);
    }
    
    @Test
    public void testValidateEmail() {
        // Valid emails
        assertTrue(registrationService.validateEmail("nupur.bhaisare@leucinetech.com"));
        assertTrue(registrationService.validateEmail("user.name+tag@example.co.uk"));
        assertTrue(registrationService.validateEmail("user-name@example.org"));
        
        // Invalid emails
        assertFalse(registrationService.validateEmail(""));
        assertFalse(registrationService.validateEmail(null));
        assertFalse(registrationService.validateEmail("invalid"));
        assertFalse(registrationService.validateEmail("invalid@"));
        assertFalse(registrationService.validateEmail("@example.com"));
    }
    
    @Test
    public void testValidatePassword() {
        // Valid password
        Map<String, Boolean> result = registrationService.validatePassword("Password123!");
        assertFalse(result.containsValue(false));
        
        // Invalid passwords
        result = registrationService.validatePassword("short");
        assertTrue(result.containsValue(false));
        
        result = registrationService.validatePassword("nouppercase123!");
        assertTrue(result.containsValue(false));
        
        result = registrationService.validatePassword("NOLOWERCASE123!");
        assertTrue(result.containsValue(false));
        
        result = registrationService.validatePassword("NoDigits!");
        assertTrue(result.containsValue(false));
        
        result = registrationService.validatePassword("NoSpecialChars123");
        assertTrue(result.containsValue(false));
    }
}