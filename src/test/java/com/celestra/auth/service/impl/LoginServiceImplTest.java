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
import com.celestra.auth.service.UserLockoutService;
import com.celestra.dao.AuditLogDao;
import com.celestra.dao.CompanyDao;
import com.celestra.dao.FailedLoginDao;
import com.celestra.dao.UserDao;
import com.celestra.dao.UserLockoutDao;
import com.celestra.dao.UserSessionDao;
import com.celestra.enums.UserStatus;
import com.celestra.model.AuditLog;
import com.celestra.model.FailedLogin;
import com.celestra.enums.CompanyStatus;
import com.celestra.model.Company;
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
    private CompanyDao companyDao;
    
    @Mock
    private AuditLogDao auditLogDao;
    
    @Mock
    private AuthConfigProvider config;
    
    @Mock
    private UserLockoutService userLockoutService;
    
    private TestableLoginServiceImpl loginService;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        loginService = new TestableLoginServiceImpl(userDao, userSessionDao, failedLoginDao, userLockoutDao, companyDao, auditLogDao, config);
        
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
        user.setPasswordHash("salt:hash");
        user.setStatus(UserStatus.ACTIVE);
        
        Company company = new Company();
        company.setId(1);
        company.setStatus(CompanyStatus.ACTIVE);
        
        when(companyDao.findById(1)).thenReturn(Optional.of(company));
        
        when(userDao.findByEmail(email)).thenReturn(Optional.of(user));
        when(userLockoutDao.findActiveByUserId(1)).thenReturn(Optional.empty());
        
        // Set up our testable service to return success for password verification
        loginService.setPasswordVerificationResult(true);
        loginService.setAccountLockedResult(false);
        
        // Act
        Optional<User> result = loginService.authenticate(email, password, ipAddress, metadata);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        
        // Verify interactions
        verify(userDao).findByEmail(email);
        verify(auditLogDao).create(any(AuditLog.class));
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
        verify(userDao, times(2)).findByEmail(email);
        verify(failedLoginDao).create(any(FailedLogin.class));
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
        
        Company company = new Company();
        company.setId(1);
        company.setStatus(CompanyStatus.ACTIVE);
        
        when(companyDao.findById(1)).thenReturn(Optional.of(company));
        
        when(userDao.findByEmail(email)).thenReturn(Optional.of(user));
        when(userLockoutDao.findActiveByUserId(1)).thenReturn(Optional.empty());
        when(failedLoginDao.countRecentByEmail(email, config.getLockoutWindowMinutes())).thenReturn(1);
        
        // Set up our testable service to return failure for password verification
        loginService.setPasswordVerificationResult(false);
        loginService.setAccountLockedResult(false);
        
        // Act
        Optional<User> result = loginService.authenticate(email, password, ipAddress, metadata);
        
        // Assert
        assertFalse(result.isPresent());
        
        // Verify interactions
        verify(userDao, times(2)).findByEmail(email);
        verify(failedLoginDao).create(any(FailedLogin.class));
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
        
        Company company = new Company();
        company.setId(1);
        company.setStatus(CompanyStatus.ACTIVE);
        
        when(companyDao.findById(1)).thenReturn(Optional.of(company));
        
        UserLockout lockout = new UserLockout();
        lockout.setUserId(1);
        lockout.setLockoutStart(Timestamp.from(Instant.now().minusSeconds(60)));
        lockout.setLockoutEnd(Timestamp.from(Instant.now().plusSeconds(60)));
        
        when(userDao.findByEmail(email)).thenReturn(Optional.of(user));
        
        // Set up our testable service to return true for account locked
        loginService.setAccountLockedResult(true);
        
        // Act
        Optional<User> result = loginService.authenticate(email, password, ipAddress, metadata);
        
        // Assert
        assertFalse(result.isPresent());
        
        // Verify interactions
        verify(userDao, times(2)).findByEmail(email);
        verify(failedLoginDao).create(any(FailedLogin.class));
        verify(auditLogDao).create(any(AuditLog.class));
    }
    
    @Test
    public void testAuthenticate_InactiveCompany() throws SQLException {
        // Arrange
        String email = "user@example.com";
        String password = "Password123!";
        String ipAddress = "127.0.0.1";
        Map<String, String> metadata = new HashMap<>();
        
        User user = new User();
        user.setId(1);
        user.setEmail(email);
        user.setPasswordHash("salt:hash");
        user.setStatus(UserStatus.ACTIVE);
        user.setCompanyId(1);
        
        Company company = new Company();
        company.setId(1);
        company.setStatus(CompanyStatus.SUSPENDED);
        
        when(userDao.findByEmail(email)).thenReturn(Optional.of(user));
        when(userLockoutDao.findActiveByUserId(1)).thenReturn(Optional.empty());
        when(companyDao.findById(1)).thenReturn(Optional.of(company));
        
        // Set up our testable service
        loginService.setPasswordVerificationResult(true);
        loginService.setAccountLockedResult(false);
        loginService.setCompanyActiveResult(false);
        
        // Act
        Optional<User> result = loginService.authenticate(email, password, ipAddress, metadata);
        
        // Assert
        assertFalse(result.isPresent());
        
        // Verify interactions
        verify(userDao, times(2)).findByEmail(email);
        verify(userLockoutDao).findActiveByUserId(1);
        verify(companyDao).findById(1);
        verify(failedLoginDao).create(any(FailedLogin.class));
        verify(auditLogDao).create(any(AuditLog.class));
    }
    
    @Test
    public void testAuthenticate_SuperAdminWithInactiveCompany() throws SQLException {
        // TODO: Add test for super admin with inactive company
        // Super admins should be able to log in even if their company is inactive
    }
    
    @Test
    public void testValidateSession_InactiveCompany() throws SQLException {
        // TODO: Add test for session validation with inactive company
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
        
        Company company = new Company();
        company.setId(1);
        company.setStatus(CompanyStatus.ACTIVE);
        
        when(companyDao.findById(1)).thenReturn(Optional.of(company));
        
        when(userSessionDao.findBySessionToken(sessionToken)).thenReturn(Optional.of(session));
        when(userDao.findById(1)).thenReturn(Optional.of(user));
        
        // Set up our testable service
        loginService.setSessionExpiredResult(false);
        loginService.setAccountLockedResult(false);
        
        // Act
        Optional<UserSession> result = loginService.validateSession(sessionToken);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(session, result.get());
        
        // Verify interactions
        verify(userSessionDao).findBySessionToken(sessionToken);
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
        
        // Set up our testable service
        loginService.setSessionExpiredResult(true);
        
        // Act
        Optional<UserSession> result = loginService.validateSession(sessionToken);
        
        // Assert
        assertFalse(result.isPresent());
        
        // Verify interactions
        verify(userSessionDao).findBySessionToken(sessionToken);
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
        
        // Set up our testable service
        loginService.setAccountLockedResult(true);
        
        // Act
        boolean result = loginService.isAccountLocked(userId);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    public void testIsAccountLocked_ById_NotLocked() throws SQLException {
        // Arrange
        Integer userId = 1;
        
        // Set up our testable service
        loginService.setAccountLockedResult(false);
        
        // Act
        boolean result = loginService.isAccountLocked(userId);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    public void testIsAccountLocked_ByEmail_Locked() throws SQLException {
        // Arrange
        String email = "locked@example.com";
        Integer userId = 1;
        
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        
        when(userDao.findByEmail(email)).thenReturn(Optional.of(user));
        
        // Set up our testable service
        loginService.setAccountLockedResult(true);
        
        // Also set up the mock UserLockoutService
        when(loginService.getMockUserLockoutService().isAccountLocked(email)).thenReturn(true);
        
        // Act
        boolean result = loginService.isAccountLocked(email);
        
        // Assert
        assertTrue(result, "Account should be locked");
        
        // Verify interactions
        verify(loginService.getMockUserLockoutService()).isAccountLocked(email);
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