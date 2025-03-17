package com.celestra.auth.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.celestra.auth.config.TestAuthConfigProvider;
import com.celestra.auth.service.ForgotPasswordService;
import com.celestra.dao.AuditLogDao;
import com.celestra.dao.PasswordHistoryDao;
import com.celestra.dao.PasswordResetTokenDao;
import com.celestra.dao.UserDao;
import com.celestra.dao.UserSessionDao;
import com.celestra.email.EmailService;
import com.celestra.email.exception.EmailException;
import com.celestra.enums.UserRole;
import com.celestra.enums.UserStatus;
import com.celestra.model.PasswordResetToken;
import com.celestra.model.User;

/**
 * Tests for the ForgotPasswordServiceImpl class.
 */
public class ForgotPasswordServiceImplTest {
    
    @Mock
    private UserDao userDao;
    
    @Mock
    private PasswordResetTokenDao passwordResetTokenDao;
    
    @Mock
    private PasswordHistoryDao passwordHistoryDao;
    
    @Mock
    private UserSessionDao userSessionDao;
    
    @Mock
    private AuditLogDao auditLogDao;
    
    @Mock
    private EmailService emailService;
    
    private TestAuthConfigProvider authConfig;
    
    private ForgotPasswordService forgotPasswordService;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Create and configure test auth config
        authConfig = new TestAuthConfigProvider();
        authConfig.setPasswordResetTokenExpirationMinutes(30);
        authConfig.setPasswordHistoryCount(5);
        
        forgotPasswordService = new ForgotPasswordServiceImpl(
            userDao, passwordResetTokenDao, passwordHistoryDao, userSessionDao, 
            auditLogDao, emailService, authConfig
        );
    }
    
    @Test
    public void testInitiatePasswordResetForExistingUser() throws SQLException, EmailException {
        // Setup test data
        String email = "test@example.com";
        String ipAddress = "127.0.0.1";
        Map<String, String> metadata = new HashMap<>();
        metadata.put("user_agent", "Test Browser");
        
        User user = new User();
        user.setId(1);
        user.setEmail(email);
        user.setName("Test User");
        user.setRole(UserRole.REGULAR_USER);
        user.setStatus(UserStatus.ACTIVE);
        
        // Configure mocks
        when(userDao.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordResetTokenDao.create(any(PasswordResetToken.class))).thenAnswer(invocation -> {
            PasswordResetToken token = invocation.getArgument(0);
            token.setId(1);
            return token;
        });
        doNothing().when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());
        
        // Call the method
        boolean result = forgotPasswordService.initiatePasswordReset(email, ipAddress, metadata);
        
        // Verify the result
        assertTrue(result);
        
        // Verify interactions
        verify(userDao).findByEmail(email);
        verify(passwordResetTokenDao).create(any(PasswordResetToken.class));
        verify(auditLogDao).create(any());
        verify(emailService).sendHtmlEmail(eq(email), anyString(), anyString());
    }
    
    @Test
    public void testInitiatePasswordResetForNonExistentUser() throws SQLException {
        // Setup test data
        String email = "nonexistent@example.com";
        String ipAddress = "127.0.0.1";
        Map<String, String> metadata = new HashMap<>();
        
        // Configure mocks
        when(userDao.findByEmail(email)).thenReturn(Optional.empty());
        
        // Call the method
        boolean result = forgotPasswordService.initiatePasswordReset(email, ipAddress, metadata);
        
        // Verify the result - should still return true to prevent email enumeration
        assertTrue(result);
        
        // Verify interactions
        verify(userDao).findByEmail(email);
        verify(passwordResetTokenDao, never()).create(any());
        verify(auditLogDao, never()).create(any());
        verifyNoInteractions(emailService);
    }
    
    @Test
    public void testInitiatePasswordResetWithEmailException() throws SQLException, EmailException {
        // Setup test data
        String email = "test@example.com";
        String ipAddress = "127.0.0.1";
        Map<String, String> metadata = new HashMap<>();
        
        User user = new User();
        user.setId(1);
        user.setEmail(email);
        
        // Configure mocks
        when(userDao.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordResetTokenDao.create(any(PasswordResetToken.class))).thenReturn(new PasswordResetToken());
        doThrow(new EmailException("Failed to send email")).when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());
        
        // Call the method
        boolean result = forgotPasswordService.initiatePasswordReset(email, ipAddress, metadata);
        
        // Verify the result
        assertFalse(result);
        
        // Verify interactions
        verify(userDao).findByEmail(email);
        verify(passwordResetTokenDao).create(any(PasswordResetToken.class));
        verify(auditLogDao).create(any());
        verify(emailService).sendHtmlEmail(eq(email), anyString(), anyString());
    }
    
    @Test
    public void testValidateResetTokenValid() throws SQLException {
        // Setup test data
        String token = UUID.randomUUID().toString();
        
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setId(1);
        resetToken.setUserId(1);
        resetToken.setToken(token);
        resetToken.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        resetToken.setExpiresAt(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        
        // Configure mocks
        when(passwordResetTokenDao.findByToken(token)).thenReturn(Optional.of(resetToken));
        
        // Call the method
        boolean result = forgotPasswordService.validateResetToken(token);
        
        // Verify the result
        assertTrue(result);
        
        // Verify interactions
        verify(passwordResetTokenDao).findByToken(token);
    }
    
    @Test
    public void testValidateResetTokenExpired() throws SQLException {
        // Setup test data
        String token = UUID.randomUUID().toString();
        
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setId(1);
        resetToken.setUserId(1);
        resetToken.setToken(token);
        resetToken.setCreatedAt(Timestamp.from(Instant.now().minus(2, ChronoUnit.HOURS)));
        resetToken.setExpiresAt(Timestamp.from(Instant.now().minus(1, ChronoUnit.HOURS)));
        
        // Configure mocks
        when(passwordResetTokenDao.findByToken(token)).thenReturn(Optional.of(resetToken));
        
        // Call the method
        boolean result = forgotPasswordService.validateResetToken(token);
        
        // Verify the result
        assertFalse(result);
        
        // Verify interactions
        verify(passwordResetTokenDao).findByToken(token);
    }
    
    @Test
    public void testValidateResetTokenUsed() throws SQLException {
        // Setup test data
        String token = UUID.randomUUID().toString();
        
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setId(1);
        resetToken.setUserId(1);
        resetToken.setToken(token);
        resetToken.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        resetToken.setExpiresAt(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        resetToken.setUsedAt(new Timestamp(System.currentTimeMillis()));
        
        // Configure mocks
        when(passwordResetTokenDao.findByToken(token)).thenReturn(Optional.of(resetToken));
        
        // Call the method
        boolean result = forgotPasswordService.validateResetToken(token);
        
        // Verify the result
        assertFalse(result);
        
        // Verify interactions
        verify(passwordResetTokenDao).findByToken(token);
    }
    
    @Test
    public void testValidateResetTokenNonExistent() throws SQLException {
        // Setup test data
        String token = UUID.randomUUID().toString();
        
        // Configure mocks
        when(passwordResetTokenDao.findByToken(token)).thenReturn(Optional.empty());
        
        // Call the method
        boolean result = forgotPasswordService.validateResetToken(token);
        
        // Verify the result
        assertFalse(result);
        
        // Verify interactions
        verify(passwordResetTokenDao).findByToken(token);
    }
    
    @Test
    public void testResetPasswordSuccess() throws SQLException, EmailException {
        // Setup test data
        String token = UUID.randomUUID().toString();
        String newPassword = "NewPassword123!";
        String ipAddress = "127.0.0.1";
        Map<String, String> metadata = new HashMap<>();
        
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setId(1);
        resetToken.setUserId(1);
        resetToken.setToken(token);
        resetToken.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        resetToken.setExpiresAt(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        user.setPasswordHash("oldPasswordHash");
        
        // Configure mocks
        when(passwordResetTokenDao.findByToken(token)).thenReturn(Optional.of(resetToken));
        when(userDao.findById(1)).thenReturn(Optional.of(user));
        when(passwordHistoryDao.existsByUserIdAndPasswordHash(eq(1), anyString())).thenReturn(false);
        when(userDao.update(any(User.class))).thenReturn(user);
        when(passwordHistoryDao.create(any())).thenReturn(null);
        when(passwordResetTokenDao.markAsUsed(eq(token), any(Timestamp.class))).thenReturn(true);
        when(userSessionDao.deleteByUserId(1)).thenReturn(1);
        doNothing().when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());
        
        // Call the method
        boolean result = forgotPasswordService.resetPassword(token, newPassword, ipAddress, metadata);
        
        // Verify the result
        assertTrue(result);
        
        // Verify interactions
        verify(passwordResetTokenDao).findByToken(token);
        verify(userDao).findById(1);
        verify(passwordHistoryDao).existsByUserIdAndPasswordHash(eq(1), anyString());
        verify(userDao).update(any(User.class));
        verify(passwordHistoryDao).create(any());
        verify(passwordResetTokenDao).markAsUsed(eq(token), any(Timestamp.class));
        verify(userSessionDao).deleteByUserId(1);
        verify(auditLogDao).create(any());
        verify(emailService).sendHtmlEmail(eq("test@example.com"), anyString(), anyString());
    }
    
    @Test
    public void testResetPasswordWithReusedPassword() throws SQLException {
        // Setup test data
        String token = UUID.randomUUID().toString();
        String newPassword = "ReusedPassword123!";
        String ipAddress = "127.0.0.1";
        Map<String, String> metadata = new HashMap<>();
        
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setId(1);
        resetToken.setUserId(1);
        resetToken.setToken(token);
        resetToken.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        resetToken.setExpiresAt(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        
        User user = new User();
        user.setId(1);
        
        // Configure mocks
        when(passwordResetTokenDao.findByToken(token)).thenReturn(Optional.of(resetToken));
        when(userDao.findById(1)).thenReturn(Optional.of(user));
        when(passwordHistoryDao.existsByUserIdAndPasswordHash(eq(1), anyString())).thenReturn(true);
        
        // Call the method
        boolean result = forgotPasswordService.resetPassword(token, newPassword, ipAddress, metadata);
        
        // Verify the result
        assertFalse(result);
        
        // Verify interactions
        verify(passwordResetTokenDao).findByToken(token);
        verify(userDao).findById(1);
        verify(passwordHistoryDao).existsByUserIdAndPasswordHash(eq(1), anyString());
        verify(userDao, never()).update(any(User.class));
        verify(passwordHistoryDao, never()).create(any());
        verify(passwordResetTokenDao, never()).markAsUsed(anyString(), any(Timestamp.class));
        verify(userSessionDao, never()).deleteByUserId(anyInt());
        verify(auditLogDao).create(any()); // We now create an audit log for password reuse
        verifyNoInteractions(emailService);
    }
    
    @Test
    public void testGetEmailFromToken() throws SQLException {
        // Setup test data
        String token = UUID.randomUUID().toString();
        String email = "test@example.com";
        
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setId(1);
        resetToken.setUserId(1);
        resetToken.setToken(token);
        resetToken.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        resetToken.setExpiresAt(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        
        User user = new User();
        user.setId(1);
        user.setEmail(email);
        
        // Configure mocks
        when(passwordResetTokenDao.findByToken(token)).thenReturn(Optional.of(resetToken));
        when(userDao.findById(1)).thenReturn(Optional.of(user));
        
        // Call the method
        String result = forgotPasswordService.getEmailFromToken(token);
        
        // Verify the result
        assertEquals(email, result);
        
        // Verify interactions
        verify(passwordResetTokenDao).findByToken(token);
        verify(userDao).findById(1);
    }
    
    @Test
    public void testGetEmailFromInvalidToken() throws SQLException {
        // Setup test data
        String token = UUID.randomUUID().toString();
        
        // Configure mocks
        when(passwordResetTokenDao.findByToken(token)).thenReturn(Optional.empty());
        
        // Call the method
        String result = forgotPasswordService.getEmailFromToken(token);
        
        // Verify the result
        assertNull(result);
        
        // Verify interactions
        verify(passwordResetTokenDao).findByToken(token);
        verify(userDao, never()).findById(anyInt());
    }
}