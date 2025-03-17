package com.celestra.auth.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.celestra.auth.config.TestAuthConfigProvider;
import com.celestra.auth.service.FailedLoginTrackingService;
import com.celestra.dao.AuditLogDao;
import com.celestra.dao.FailedLoginDao;
import com.celestra.dao.UserDao;
import com.celestra.model.AuditLog;
import com.celestra.model.FailedLogin;
import com.celestra.model.User;

public class FailedLoginTrackingServiceImplTest {
    
    @Mock
    private FailedLoginDao failedLoginDao;
    
    @Mock
    private UserDao userDao;
    
    @Mock
    private AuditLogDao auditLogDao;
    
    private TestAuthConfigProvider authConfig;
    
    private FailedLoginTrackingService failedLoginTrackingService;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Create and configure test auth config
        authConfig = new TestAuthConfigProvider();
        authConfig.setLockoutMaxAttempts(5);
        authConfig.setLockoutWindowMinutes(30);
        
        failedLoginTrackingService = new FailedLoginTrackingServiceImpl(
            failedLoginDao, userDao, auditLogDao, authConfig
        );
    }
    
    @Test
    public void testRecordFailedLoginWithEmail() throws SQLException {
        // Setup test data
        String email = "test@example.com";
        String ipAddress = "192.168.1.1";
        String failureReason = "Invalid password";
        Map<String, String> metadata = new HashMap<>();
        metadata.put("browser", "Chrome");
        
        // Mock DAO behavior
        when(failedLoginDao.create(any(FailedLogin.class))).thenAnswer(invocation -> {
            FailedLogin failedLogin = invocation.getArgument(0);
            failedLogin.setId(1);
            return failedLogin;
        });
        
        // Execute the method
        FailedLogin result = failedLoginTrackingService.recordFailedLogin(
            email, ipAddress, failureReason, metadata
        );
        
        // Verify the result
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(email, result.getEmail());
        assertEquals(ipAddress, result.getIpAddress());
        assertEquals(failureReason, result.getFailureReason());
        assertNotNull(result.getAttemptedAt());
        
        // Verify DAO interactions
        verify(failedLoginDao).create(any(FailedLogin.class));
        verify(userDao).findByEmail(email);
        verify(auditLogDao).create(any(AuditLog.class));
    }
    
    @Test
    public void testRecordFailedLoginWithUser() throws SQLException {
        // Setup test data
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        
        String ipAddress = "192.168.1.1";
        String failureReason = "Account locked";
        Map<String, String> metadata = new HashMap<>();
        metadata.put("browser", "Firefox");
        
        // Mock DAO behavior
        when(failedLoginDao.create(any(FailedLogin.class))).thenAnswer(invocation -> {
            FailedLogin failedLogin = invocation.getArgument(0);
            failedLogin.setId(1);
            return failedLogin;
        });
        
        // Execute the method
        FailedLogin result = failedLoginTrackingService.recordFailedLogin(
            user, ipAddress, failureReason, metadata
        );
        
        // Verify the result
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(user.getId(), result.getUserId());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(ipAddress, result.getIpAddress());
        assertEquals(failureReason, result.getFailureReason());
        assertNotNull(result.getAttemptedAt());
        
        // Verify DAO interactions
        verify(failedLoginDao).create(any(FailedLogin.class));
        verify(auditLogDao).create(any(AuditLog.class));
        verifyNoInteractions(userDao); // User is already provided, no need to look it up
    }
    
    @Test
    public void testGetRecentFailedLoginCount() throws SQLException {
        // Setup test data
        String email = "test@example.com";
        int windowMinutes = 30;
        int expectedCount = 3;
        
        // Mock DAO behavior
        when(failedLoginDao.countRecentByEmail(email, windowMinutes)).thenReturn(expectedCount);
        
        // Execute the method
        int result = failedLoginTrackingService.getRecentFailedLoginCount(email, windowMinutes);
        
        // Verify the result
        assertEquals(expectedCount, result);
        
        // Verify DAO interactions
        verify(failedLoginDao).countRecentByEmail(email, windowMinutes);
    }
    
    @Test
    public void testGetRecentFailedLoginCountByIp() throws SQLException {
        // Setup test data
        String ipAddress = "192.168.1.1";
        int windowMinutes = 30;
        int expectedCount = 10;
        
        // Mock DAO behavior
        when(failedLoginDao.countRecentByIpAddress(ipAddress, windowMinutes)).thenReturn(expectedCount);
        
        // Execute the method
        int result = failedLoginTrackingService.getRecentFailedLoginCountByIp(ipAddress, windowMinutes);
        
        // Verify the result
        assertEquals(expectedCount, result);
        
        // Verify DAO interactions
        verify(failedLoginDao).countRecentByIpAddress(ipAddress, windowMinutes);
    }
    
    @Test
    public void testGetRecentFailedLogins() throws SQLException {
        // Setup test data
        String email = "test@example.com";
        int windowMinutes = 30;
        
        List<FailedLogin> expectedLogins = new ArrayList<>();
        FailedLogin login1 = new FailedLogin();
        login1.setId(1);
        login1.setEmail(email);
        login1.setAttemptedAt(new Timestamp(System.currentTimeMillis()));
        expectedLogins.add(login1);
        
        // Mock DAO behavior
        when(failedLoginDao.findRecentByEmail(email, windowMinutes)).thenReturn(expectedLogins);
        
        // Execute the method
        List<FailedLogin> result = failedLoginTrackingService.getRecentFailedLogins(email, windowMinutes);
        
        // Verify the result
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(login1.getId(), result.get(0).getId());
        
        // Verify DAO interactions
        verify(failedLoginDao).findRecentByEmail(email, windowMinutes);
    }
    
    @Test
    public void testIsFailedLoginThresholdExceeded() throws SQLException {
        // Setup test data
        String email = "test@example.com";
        int lockoutMaxAttempts = 5;
        int lockoutWindowMinutes = 30;
        
        // Configure auth config
        authConfig.setLockoutMaxAttempts(lockoutMaxAttempts);
        authConfig.setLockoutWindowMinutes(lockoutWindowMinutes);
        
        // Test case 1: Threshold not exceeded
        when(failedLoginDao.countRecentByEmail(email, lockoutWindowMinutes)).thenReturn(lockoutMaxAttempts - 1);
        
        // Execute the method
        boolean result1 = failedLoginTrackingService.isFailedLoginThresholdExceeded(email);
        
        // Verify the result
        assertFalse(result1);
        
        // Test case 2: Threshold exactly met
        when(failedLoginDao.countRecentByEmail(email, lockoutWindowMinutes)).thenReturn(lockoutMaxAttempts);
        
        // Execute the method
        boolean result2 = failedLoginTrackingService.isFailedLoginThresholdExceeded(email);
        
        // Verify the result
        assertTrue(result2);
        
        // Test case 3: Threshold exceeded
        when(failedLoginDao.countRecentByEmail(email, lockoutWindowMinutes)).thenReturn(lockoutMaxAttempts + 1);
        
        // Execute the method
        boolean result3 = failedLoginTrackingService.isFailedLoginThresholdExceeded(email);
        
        // Verify the result
        assertTrue(result3);
        
        // Verify DAO interactions
        verify(failedLoginDao, times(3)).countRecentByEmail(email, lockoutWindowMinutes);
    }
    
    @Test
    public void testCleanupOldRecords() throws SQLException {
        // Setup test data
        int olderThanDays = 30;
        int expectedDeletedCount = 100;
        
        // Mock DAO behavior
        when(failedLoginDao.deleteOlderThan(olderThanDays)).thenReturn(expectedDeletedCount);
        
        // Execute the method
        int result = failedLoginTrackingService.cleanupOldRecords(olderThanDays);
        
        // Verify the result
        assertEquals(expectedDeletedCount, result);
        
        // Verify DAO interactions
        verify(failedLoginDao).deleteOlderThan(olderThanDays);
    }
    
    @Test
    public void testRecordFailedLoginWithExistingUser() throws SQLException {
        // Setup test data
        String email = "existing@example.com";
        String ipAddress = "192.168.1.1";
        String failureReason = "Invalid password";
        Map<String, String> metadata = new HashMap<>();
        
        User existingUser = new User();
        existingUser.setId(42);
        existingUser.setEmail(email);
        
        // Mock DAO behavior
        when(userDao.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(failedLoginDao.create(any(FailedLogin.class))).thenAnswer(invocation -> {
            FailedLogin failedLogin = invocation.getArgument(0);
            failedLogin.setId(1);
            return failedLogin;
        });
        
        // Execute the method
        FailedLogin result = failedLoginTrackingService.recordFailedLogin(
            email, ipAddress, failureReason, metadata
        );
        
        // Verify the result
        assertNotNull(result);
        assertEquals(existingUser.getId(), result.getUserId());
        
        // Verify DAO interactions
        verify(userDao).findByEmail(email);
        verify(failedLoginDao).create(any(FailedLogin.class));
        verify(auditLogDao).create(any(AuditLog.class));
    }
    
    @Test
    public void testRecordFailedLoginWithDatabaseError() throws SQLException {
        // Setup test data
        String email = "test@example.com";
        String ipAddress = "192.168.1.1";
        String failureReason = "Invalid password";
        Map<String, String> metadata = new HashMap<>();
        
        // Mock DAO behavior to simulate database error
        when(userDao.findByEmail(email)).thenThrow(new SQLException("Database connection error"));
        when(failedLoginDao.create(any(FailedLogin.class))).thenAnswer(invocation -> {
            FailedLogin failedLogin = invocation.getArgument(0);
            failedLogin.setId(1);
            return failedLogin;
        });
        
        // Execute the method - should not throw exception
        FailedLogin result = failedLoginTrackingService.recordFailedLogin(
            email, ipAddress, failureReason, metadata
        );
        
        // Verify the result
        assertNotNull(result);
        assertNull(result.getUserId()); // User ID should be null due to database error
        
        // Verify DAO interactions
        verify(userDao).findByEmail(email);
        verify(failedLoginDao).create(any(FailedLogin.class));
        verify(auditLogDao).create(any(AuditLog.class));
    }
}