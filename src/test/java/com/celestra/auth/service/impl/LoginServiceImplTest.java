package com.celestra.auth.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.celestra.auth.config.AuthConfigProvider;
import com.celestra.auth.service.LoginService;
import com.celestra.dao.AuditLogDao;
import com.celestra.dao.FailedLoginDao;
import com.celestra.dao.UserDao;
import com.celestra.dao.UserLockoutDao;
import com.celestra.dao.UserSessionDao;
import com.celestra.enums.UserStatus;
import com.celestra.model.AuditLog;
import com.celestra.model.FailedLogin;
import com.celestra.model.User;
import com.celestra.model.UserLockout;
import com.celestra.model.UserSession;

/**
 * Test class for LoginServiceImpl.
 */
public class LoginServiceImplTest {
    
    @Mock
    private UserDao userDao;
    
    @Mock
    private UserSessionDao userSessionDao;
    
    @Mock
    private FailedLoginDao failedLoginDao;
    
    @Mock
    private UserLockoutDao userLockoutDao;
    
    @Mock
    private AuditLogDao auditLogDao;
    
    @Mock
    private AuthConfigProvider config;
    
    private LoginService loginService;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        loginService = new LoginServiceImpl(userDao, userSessionDao, failedLoginDao, userLockoutDao, auditLogDao, config);
        
        // Configure default behavior for config
        when(config.getLockoutMaxAttempts()).thenReturn(5);
        when(config.getLockoutWindowMinutes()).thenReturn(30);
        when(config.getLockoutDurationMinutes()).thenReturn(15);
        when(config.getLockoutPermanentAfterConsecutiveTempLockouts()).thenReturn(3);
        when(config.getSessionExpirationMinutes()).thenReturn(60);
    }
    
    @Test
    public void testAuthenticate_Success() throws SQLException {
        // Arrange
        String email = "test@example.com";
        String password = "Password123!";
        String ipAddress = "127.0.0.1";
        Map<String, String> metadata = new HashMap<>();
        
        User user = new User();
        user.setId(1);
        user.setEmail(email);
        user.setPasswordHash("salt:hash"); // This format is expected by PasswordUtil
        user.setStatus(UserStatus.ACTIVE);
        
        when(userDao.findByEmail(email)).thenReturn(Optional.of(user));
        when(userLockoutDao.findActiveByUserId(1)).thenReturn(Optional.empty());
        
        // Mock the password verification
        mockStatic(com.celestra.auth.util.PasswordUtil.class);
        when(com.celestra.auth.util.PasswordUtil.verifyPassword(password, user.getPasswordHash())).thenReturn(true);
        
        // Act
        Optional<User> result = loginService.authenticate(email, password, ipAddress, metadata);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        
        // Verify interactions
        verify(userDao).findByEmail(email);
        verify(userLockoutDao).findActiveByUserId(1);
        verify(auditLogDao).create(any(AuditLog.class));
        verifyNoInteractions(failedLoginDao);
    }
    
    @Test
    public void testAuthenticate_UserNotFound() throws SQLException {
        // Arrange
        String email = "nonexistent@example.com";
        String password = "Password123!";
        String ipAddress = "127.0.0.1";
        Map<String, String> metadata = new HashMap<>();
        
        when(userDao.findByEmail(email)).thenReturn(Optional.empty());
        
        // Act
        Optional<User> result = loginService.authenticate(email, password, ipAddress, metadata);
        
        // Assert
        assertFalse(result.isPresent());
        
        // Verify interactions
        verify(userDao).findByEmail(email);
        verify(failedLoginDao).create(any(FailedLogin.class));
        verifyNoInteractions(userLockoutDao);
    }
    
    @Test
    public void testAuthenticate_InvalidPassword() throws SQLException {
        // Arrange
        String email = "test@example.com";
        String password = "WrongPassword";
        String ipAddress = "127.0.0.1";
        Map<String, String> metadata = new HashMap<>();
        
        User user = new User();
        user.setId(1);
        user.setEmail(email);
        user.setPasswordHash("salt:hash");
        user.setStatus(UserStatus.ACTIVE);
        
        when(userDao.findByEmail(email)).thenReturn(Optional.of(user));
        when(userLockoutDao.findActiveByUserId(1)).thenReturn(Optional.empty());
        when(failedLoginDao.countRecentByEmail(email, config.getLockoutWindowMinutes())).thenReturn(1);
        
        // Mock the password verification
        mockStatic(com.celestra.auth.util.PasswordUtil.class);
        when(com.celestra.auth.util.PasswordUtil.verifyPassword(password, user.getPasswordHash())).thenReturn(false);
        
        // Act
        Optional<User> result = loginService.authenticate(email, password, ipAddress, metadata);
        
        // Assert
        assertFalse(result.isPresent());
        
        // Verify interactions
        verify(userDao).findByEmail(email);
        verify(userLockoutDao).findActiveByUserId(1);
        verify(failedLoginDao).create(any(FailedLogin.class));
        verify(failedLoginDao).countRecentByEmail(email, config.getLockoutWindowMinutes());
        verify(auditLogDao).create(any(AuditLog.class));
    }
    
    @Test
    public void testAuthenticate_AccountLocked() throws SQLException {
        // Arrange
        String email = "locked@example.com";
        String password = "Password123!";
        String ipAddress = "127.0.0.1";
        Map<String, String> metadata = new HashMap<>();
        
        User user = new User();
        user.setId(1);
        user.setEmail(email);
        user.setPasswordHash("salt:hash");
        user.setStatus(UserStatus.ACTIVE);
        
        UserLockout lockout = new UserLockout();
        lockout.setUserId(1);
        lockout.setLockoutStart(Timestamp.from(Instant.now().minusSeconds(60)));
        lockout.setLockoutEnd(Timestamp.from(Instant.now().plusSeconds(60)));
        
        when(userDao.findByEmail(email)).thenReturn(Optional.of(user));
        when(userLockoutDao.findActiveByUserId(1)).thenReturn(Optional.of(lockout));
        
        // Mock the isActive method of UserLockout
        mockStatic(java.sql.Timestamp.class);
        when(lockout.isActive()).thenReturn(true);
        
        // Act
        Optional<User> result = loginService.authenticate(email, password, ipAddress, metadata);
        
        // Assert
        assertFalse(result.isPresent());
        
        // Verify interactions
        verify(userDao).findByEmail(email);
        verify(userLockoutDao).findActiveByUserId(1);
        verify(failedLoginDao).create(any(FailedLogin.class));
        verify(auditLogDao).create(any(AuditLog.class));
    }
    
    @Test
    public void testCreateSession() throws SQLException {
        // Arrange
        Integer userId = 1;
        String ipAddress = "127.0.0.1";
        String userAgent = "Mozilla/5.0";
        Map<String, String> metadata = new HashMap<>();
        
        UserSession session = new UserSession();
        session.setId(1);
        session.setUserId(userId);
        session.setSessionToken("generated-token");
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        session.setExpiresAt(Timestamp.from(Instant.now().plusSeconds(3600)));
        
        when(userSessionDao.create(any(UserSession.class))).thenReturn(session);
        
        // Act
        UserSession result = loginService.createSession(userId, ipAddress, userAgent, metadata);
        
        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(ipAddress, result.getIpAddress());
        assertEquals(userAgent, result.getUserAgent());
        
        // Verify interactions
        verify(userSessionDao).create(any(UserSession.class));
        verify(auditLogDao).create(any(AuditLog.class));
    }
    
    @Test
    public void testValidateSession_Valid() throws SQLException {
        // Arrange
        String sessionToken = "valid-token";
        
        UserSession session = new UserSession();
        session.setId(1);
        session.setUserId(1);
        session.setSessionToken(sessionToken);
        session.setExpiresAt(Timestamp.from(Instant.now().plusSeconds(3600)));
        
        User user = new User();
        user.setId(1);
        user.setStatus(UserStatus.ACTIVE);
        
        when(userSessionDao.findBySessionToken(sessionToken)).thenReturn(Optional.of(session));
        when(userLockoutDao.findActiveByUserId(1)).thenReturn(Optional.empty());
        when(userDao.findById(1)).thenReturn(Optional.of(user));
        
        // Mock the isExpired method of UserSession
        when(session.isExpired()).thenReturn(false);
        
        // Act
        Optional<UserSession> result = loginService.validateSession(sessionToken);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(session, result.get());
        
        // Verify interactions
        verify(userSessionDao).findBySessionToken(sessionToken);
        verify(userLockoutDao).findActiveByUserId(1);
        verify(userDao).findById(1);
    }
    
    @Test
    public void testValidateSession_Expired() throws SQLException {
        // Arrange
        String sessionToken = "expired-token";
        
        UserSession session = new UserSession();
        session.setId(1);
        session.setUserId(1);
        session.setSessionToken(sessionToken);
        session.setExpiresAt(Timestamp.from(Instant.now().minusSeconds(3600)));
        
        when(userSessionDao.findBySessionToken(sessionToken)).thenReturn(Optional.of(session));
        
        // Mock the isExpired method of UserSession
        when(session.isExpired()).thenReturn(true);
        
        // Act
        Optional<UserSession> result = loginService.validateSession(sessionToken);
        
        // Assert
        assertFalse(result.isPresent());
        
        // Verify interactions
        verify(userSessionDao).findBySessionToken(sessionToken);
        verifyNoInteractions(userLockoutDao);
        verifyNoInteractions(userDao);
    }
    
    @Test
    public void testEndSession() throws SQLException {
        // Arrange
        String sessionToken = "valid-token";
        String reason = "User logout";
        
        UserSession session = new UserSession();
        session.setId(1);
        session.setUserId(1);
        session.setSessionToken(sessionToken);
        session.setIpAddress("127.0.0.1");
        
        when(userSessionDao.findBySessionToken(sessionToken)).thenReturn(Optional.of(session));
        when(userSessionDao.updateExpiresAt(eq(1), any(Timestamp.class))).thenReturn(true);
        
        // Act
        boolean result = loginService.endSession(sessionToken, reason);
        
        // Assert
        assertTrue(result);
        
        // Verify interactions
        verify(userSessionDao).findBySessionToken(sessionToken);
        verify(userSessionDao).updateExpiresAt(eq(1), any(Timestamp.class));
        verify(auditLogDao).create(any(AuditLog.class));
    }
    
    @Test
    public void testEndAllSessions() throws SQLException {
        // Arrange
        Integer userId = 1;
        String reason = "Password changed";
        
        UserSession session1 = new UserSession();
        session1.setId(1);
        session1.setUserId(userId);
        session1.setSessionToken("token1");
        session1.setIpAddress("127.0.0.1");
        
        UserSession session2 = new UserSession();
        session2.setId(2);
        session2.setUserId(userId);
        session2.setSessionToken("token2");
        session2.setIpAddress("127.0.0.1");
        
        List<UserSession> sessions = new ArrayList<>();
        sessions.add(session1);
        sessions.add(session2);
        
        when(userSessionDao.findActiveByUserId(userId)).thenReturn(sessions);
        when(userSessionDao.findBySessionToken("token1")).thenReturn(Optional.of(session1));
        when(userSessionDao.findBySessionToken("token2")).thenReturn(Optional.of(session2));
        when(userSessionDao.updateExpiresAt(anyInt(), any(Timestamp.class))).thenReturn(true);
        
        // Act
        int result = loginService.endAllSessions(userId, reason);
        
        // Assert
        assertEquals(2, result);
        
        // Verify interactions
        verify(userSessionDao).findActiveByUserId(userId);
        verify(userSessionDao).findBySessionToken("token1");
        verify(userSessionDao).findBySessionToken("token2");
        verify(userSessionDao, times(2)).updateExpiresAt(anyInt(), any(Timestamp.class));
        verify(auditLogDao, times(2)).create(any(AuditLog.class));
    }
    
    @Test
    public void testIsAccountLocked_ById_Locked() throws SQLException {
        // Arrange
        Integer userId = 1;
        
        UserLockout lockout = new UserLockout();
        lockout.setUserId(userId);
        lockout.setLockoutStart(Timestamp.from(Instant.now().minusSeconds(60)));
        lockout.setLockoutEnd(Timestamp.from(Instant.now().plusSeconds(60)));
        
        when(userLockoutDao.findActiveByUserId(userId)).thenReturn(Optional.of(lockout));
        
        // Mock the isActive method of UserLockout
        when(lockout.isActive()).thenReturn(true);
        
        // Act
        boolean result = loginService.isAccountLocked(userId);
        
        // Assert
        assertTrue(result);
        
        // Verify interactions
        verify(userLockoutDao).findActiveByUserId(userId);
    }
    
    @Test
    public void testIsAccountLocked_ById_NotLocked() throws SQLException {
        // Arrange
        Integer userId = 1;
        
        when(userLockoutDao.findActiveByUserId(userId)).thenReturn(Optional.empty());
        
        // Act
        boolean result = loginService.isAccountLocked(userId);
        
        // Assert
        assertFalse(result);
        
        // Verify interactions
        verify(userLockoutDao).findActiveByUserId(userId);
    }
    
    @Test
    public void testIsAccountLocked_ByEmail_Locked() throws SQLException {
        // Arrange
        String email = "locked@example.com";
        Integer userId = 1;
        
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        
        UserLockout lockout = new UserLockout();
        lockout.setUserId(userId);
        lockout.setLockoutStart(Timestamp.from(Instant.now().minusSeconds(60)));
        lockout.setLockoutEnd(Timestamp.from(Instant.now().plusSeconds(60)));
        
        when(userDao.findByEmail(email)).thenReturn(Optional.of(user));
        when(userLockoutDao.findActiveByUserId(userId)).thenReturn(Optional.of(lockout));
        
        // Mock the isActive method of UserLockout
        when(lockout.isActive()).thenReturn(true);
        
        // Act
        boolean result = loginService.isAccountLocked(email);
        
        // Assert
        assertTrue(result);
        
        // Verify interactions
        verify(userDao).findByEmail(email);
        verify(userLockoutDao).findActiveByUserId(userId);
    }
    
    @Test
    public void testRecordFailedLogin() throws SQLException {
        // Arrange
        String email = "test@example.com";
        String ipAddress = "127.0.0.1";
        String reason = "Invalid password";
        Map<String, String> metadata = new HashMap<>();
        
        User user = new User();
        user.setId(1);
        user.setEmail(email);
        
        when(userDao.findByEmail(email)).thenReturn(Optional.of(user));
        
        // Act
        loginService.recordFailedLogin(email, ipAddress, reason, metadata);
        
        // Verify interactions
        verify(userDao).findByEmail(email);
        
        ArgumentCaptor<FailedLogin> failedLoginCaptor = ArgumentCaptor.forClass(FailedLogin.class);
        verify(failedLoginDao).create(failedLoginCaptor.capture());
        
        FailedLogin capturedFailedLogin = failedLoginCaptor.getValue();
        assertEquals(email, capturedFailedLogin.getEmail());
        assertEquals(ipAddress, capturedFailedLogin.getIpAddress());
        assertEquals(reason, capturedFailedLogin.getFailureReason());
        assertEquals(user.getId(), capturedFailedLogin.getUserId());
    }
    
    @Test
    public void testGetRecentFailedLoginCount_ById() throws SQLException {
        // Arrange
        Integer userId = 1;
        int minutes = 30;
        String email = "test@example.com";
        
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        
        when(userDao.findById(userId)).thenReturn(Optional.of(user));
        when(failedLoginDao.countRecentByEmail(email, minutes)).thenReturn(3);
        
        // Act
        int result = loginService.getRecentFailedLoginCount(userId, minutes);
        
        // Assert
        assertEquals(3, result);
        
        // Verify interactions
        verify(userDao).findById(userId);
        verify(failedLoginDao).countRecentByEmail(email, minutes);
    }
    
    @Test
    public void testGetRecentFailedLoginCount_ByIpAddress() throws SQLException {
        // Arrange
        String ipAddress = "127.0.0.1";
        int minutes = 30;
        
        when(failedLoginDao.countRecentByIpAddress(ipAddress, minutes)).thenReturn(5);
        
        // Act
        int result = loginService.getRecentFailedLoginCount(ipAddress, minutes);
        
        // Assert
        assertEquals(5, result);
        
        // Verify interactions
        verify(failedLoginDao).countRecentByIpAddress(ipAddress, minutes);
    }
}