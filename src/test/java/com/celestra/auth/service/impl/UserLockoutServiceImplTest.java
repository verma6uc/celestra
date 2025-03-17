package com.celestra.auth.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.celestra.auth.config.TestAuthConfigProvider;
import com.celestra.auth.service.UserLockoutService;
import com.celestra.auth.service.AuditService;
import com.celestra.dao.AuditLogDao;
import com.celestra.dao.UserDao;
import com.celestra.dao.UserLockoutDao;
import com.celestra.dao.UserSessionDao;
import com.celestra.enums.AuditEventType;
import com.celestra.model.AuditLog;
import com.celestra.model.User;
import com.celestra.model.UserLockout;
import com.celestra.model.UserSession;

public class UserLockoutServiceImplTest {
    
    @Mock
    private UserLockoutDao userLockoutDao;
    
    @Mock
    private UserDao userDao;
    
    @Mock
    private UserSessionDao userSessionDao;
    
    @Mock
    private AuditLogDao auditLogDao;
    
    @Mock
    private AuditService auditService;
    
    private TestAuthConfigProvider authConfig;
    
    private UserLockoutService userLockoutService;
    
    @BeforeEach
    public void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        
        // Create and configure test auth config
        authConfig = new TestAuthConfigProvider();
        authConfig.setLockoutMaxAttempts(5);
        authConfig.setLockoutWindowMinutes(30);
        authConfig.setLockoutDurationMinutes(60);
        authConfig.setLockoutPermanentAfterConsecutiveTempLockouts(3);
        
        // Set up mock user for tests
        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setEmail("test@example.com");
        when(userDao.findById(1)).thenReturn(Optional.of(mockUser));
        
        userLockoutService = new UserLockoutServiceImpl(
            userLockoutDao, userDao, userSessionDao, auditLogDao, auditService, authConfig
        );
    }
    
    @Test
    public void testLockAccount() throws SQLException {
        // Setup test data
        Integer userId = 1;
        int failedAttempts = 5;
        String reason = "Too many failed login attempts";
        String ipAddress = "192.168.1.1";
        
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        
        // Mock DAO behavior
        when(userDao.findById(userId)).thenReturn(Optional.of(user));
        when(userLockoutDao.findByUserId(userId)).thenReturn(List.of());
        when(userLockoutDao.create(any(UserLockout.class))).thenAnswer(invocation -> {
            UserLockout lockout = invocation.getArgument(0);
            lockout.setId(1);
            return lockout;
        });
        
        // Execute the method
        UserLockout result = userLockoutService.lockAccount(userId, failedAttempts, reason, ipAddress);
        
        // Verify the result
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(failedAttempts, result.getFailedAttempts());
        assertEquals(reason, result.getReason());
        assertNotNull(result.getLockoutStart());
        assertNotNull(result.getLockoutEnd()); // Should be temporary lockout
        
        // Verify DAO interactions
        verify(userDao).findById(userId);
        verify(userLockoutDao).create(any(UserLockout.class));
        verify(auditService).recordAccountLockout(any(User.class), eq(ipAddress), eq(reason));
        verify(userSessionDao).findActiveByUserId(userId);
    }
    
    @Test
    public void testLockAccountByEmail() throws SQLException {
        // Setup test data
        String email = "test@example.com";
        int failedAttempts = 5;
        String reason = "Too many failed login attempts";
        String ipAddress = "192.168.1.1";
        
        User user = new User();
        user.setId(1);
        user.setEmail(email);
        
        // Mock DAO behavior
        when(userDao.findByEmail(email)).thenReturn(Optional.of(user));
        when(userDao.findById(user.getId())).thenReturn(Optional.of(user));
        when(userLockoutDao.findByUserId(user.getId())).thenReturn(List.of());
        when(userLockoutDao.create(any(UserLockout.class))).thenAnswer(invocation -> {
            UserLockout lockout = invocation.getArgument(0);
            lockout.setId(1);
            return lockout;
        });
        
        // Execute the method
        UserLockout result = userLockoutService.lockAccountByEmail(email, failedAttempts, reason, ipAddress);
        
        // Verify the result
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(user.getId(), result.getUserId());
        assertEquals(failedAttempts, result.getFailedAttempts());
        assertEquals(reason, result.getReason());
        
        // Verify DAO interactions
        verify(userDao).findByEmail(email);
        verify(userLockoutDao).create(any(UserLockout.class));
    }
    
    @Test
    public void testLockAccountByEmailUserNotFound() throws SQLException {
        // Setup test data
        String email = "nonexistent@example.com";
        int failedAttempts = 5;
        String reason = "Too many failed login attempts";
        String ipAddress = "192.168.1.1";
        
        // Mock DAO behavior
        when(userDao.findByEmail(email)).thenReturn(Optional.empty());
        
        // Execute the method
        UserLockout result = userLockoutService.lockAccountByEmail(email, failedAttempts, reason, ipAddress);
        
        // Verify the result
        assertNull(result);
        
        // Verify DAO interactions
        verify(userDao).findByEmail(email);
        verifyNoInteractions(userLockoutDao);
    }
    
    @Test
    public void testIsAccountLocked() throws SQLException {
        // Setup test data
        Integer userId = 1;
        
        UserLockout activeLockout = new UserLockout();
        activeLockout.setId(1);
        activeLockout.setUserId(userId);
        activeLockout.setLockoutStart(Timestamp.from(Instant.now().minus(1, ChronoUnit.HOURS)));
        activeLockout.setLockoutEnd(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        
        // Mock DAO behavior
        when(userLockoutDao.findActiveByUserId(userId)).thenReturn(Optional.of(activeLockout));
        
        // Execute the method
        boolean result = userLockoutService.isAccountLocked(userId);
        
        // Verify the result
        assertTrue(result);
        
        // Verify DAO interactions
        verify(userLockoutDao).findActiveByUserId(userId);
    }
    
    @Test
    public void testIsAccountLockedNoLockout() throws SQLException {
        // Setup test data
        Integer userId = 1;
        
        // Mock DAO behavior
        when(userLockoutDao.findActiveByUserId(userId)).thenReturn(Optional.empty());
        
        // Execute the method
        boolean result = userLockoutService.isAccountLocked(userId);
        
        // Verify the result
        assertFalse(result);
        
        // Verify DAO interactions
        verify(userLockoutDao).findActiveByUserId(userId);
    }
    
    @Test
    public void testIsAccountLockedByEmail() throws SQLException {
        // Setup test data
        String email = "test@example.com";
        Integer userId = 1;
        
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        
        UserLockout activeLockout = new UserLockout();
        activeLockout.setId(1);
        activeLockout.setUserId(userId);
        activeLockout.setLockoutStart(Timestamp.from(Instant.now().minus(1, ChronoUnit.HOURS)));
        activeLockout.setLockoutEnd(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        
        // Mock DAO behavior
        when(userDao.findByEmail(email)).thenReturn(Optional.of(user));
        when(userLockoutDao.findActiveByUserId(userId)).thenReturn(Optional.of(activeLockout));
        
        // Execute the method
        boolean result = userLockoutService.isAccountLocked(email);
        
        // Verify the result
        assertTrue(result);
        
        // Verify DAO interactions
        verify(userDao).findByEmail(email);
        verify(userLockoutDao).findActiveByUserId(userId);
    }
    
    @Test
    public void testGetActiveLockout() throws SQLException {
        // Setup test data
        Integer userId = 1;
        
        UserLockout activeLockout = new UserLockout();
        activeLockout.setId(1);
        activeLockout.setUserId(userId);
        activeLockout.setLockoutStart(Timestamp.from(Instant.now().minus(1, ChronoUnit.HOURS)));
        activeLockout.setLockoutEnd(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        
        // Mock DAO behavior
        when(userLockoutDao.findActiveByUserId(userId)).thenReturn(Optional.of(activeLockout));
        
        // Execute the method
        Optional<UserLockout> result = userLockoutService.getActiveLockout(userId);
        
        // Verify the result
        assertTrue(result.isPresent());
        assertEquals(activeLockout.getId(), result.get().getId());
        
        // Verify DAO interactions
        verify(userLockoutDao).findActiveByUserId(userId);
    }
    
    @Test
    public void testGetLockoutHistory() throws SQLException {
        // Setup test data
        Integer userId = 1;
        
        List<UserLockout> lockoutHistory = new ArrayList<>();
        UserLockout lockout1 = new UserLockout();
        lockout1.setId(1);
        lockout1.setUserId(userId);
        lockoutHistory.add(lockout1);
        
        UserLockout lockout2 = new UserLockout();
        lockout2.setId(2);
        lockout2.setUserId(userId);
        lockoutHistory.add(lockout2);
        
        // Mock DAO behavior
        when(userLockoutDao.findByUserId(userId)).thenReturn(lockoutHistory);
        
        // Execute the method
        List<UserLockout> result = userLockoutService.getLockoutHistory(userId);
        
        // Verify the result
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify DAO interactions
        verify(userLockoutDao).findByUserId(userId);
    }
    
    @Test
    public void testUnlockAccount() throws SQLException {
        // Setup test data
        Integer userId = 1;
        Integer adminUserId = 2;
        String reason = "Manual unlock by admin";
        String ipAddress = "192.168.1.1";
        
        UserLockout activeLockout = new UserLockout();
        activeLockout.setId(1);
        activeLockout.setUserId(userId);
        activeLockout.setLockoutStart(Timestamp.from(Instant.now().minus(1, ChronoUnit.HOURS)));
        activeLockout.setLockoutEnd(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        
        User admin = new User();
        admin.setId(adminUserId);
        admin.setEmail("admin@example.com");
        
        // Mock DAO behavior
        when(userLockoutDao.findActiveByUserId(userId)).thenReturn(Optional.of(activeLockout));
        when(userLockoutDao.updateLockoutEnd(eq(activeLockout.getId()), any(Timestamp.class))).thenReturn(true);
        when(userDao.findById(userId)).thenReturn(Optional.of(user));
        when(userDao.findById(adminUserId)).thenReturn(Optional.of(admin));
        
        // Execute the method
        boolean result = userLockoutService.unlockAccount(userId, reason, adminUserId, ipAddress);
        
        // Verify the result
        assertTrue(result);
        
        // Verify DAO interactions
        verify(userLockoutDao).findActiveByUserId(userId);
        verify(userLockoutDao).updateLockoutEnd(eq(activeLockout.getId()), any(Timestamp.class));
        verify(userDao, times(2)).findById(anyInt());
        verify(auditService).recordAccountUnlock(any(User.class), eq(ipAddress), any(User.class), eq(reason));
    }
    
    @Test
    public void testUnlockAccountNotLocked() throws SQLException {
        // Setup test data
        Integer userId = 1;
        Integer adminUserId = 2;
        String reason = "Manual unlock by admin";
        String ipAddress = "192.168.1.1";
        
        // Mock DAO behavior
        when(userLockoutDao.findActiveByUserId(userId)).thenReturn(Optional.empty());
        
        // Execute the method
        boolean result = userLockoutService.unlockAccount(userId, reason, adminUserId, ipAddress);
        
        // Verify the result
        assertFalse(result);
        
        // Verify DAO interactions
        verify(userLockoutDao).findActiveByUserId(userId);
        verify(userLockoutDao, never()).updateLockoutEnd(anyInt(), any(Timestamp.class));
        verifyNoInteractions(auditService);
    }
    
    @Test
    public void testMakeLockoutPermanent() throws SQLException {
        // Setup test data
        Integer userId = 1;
        Integer adminUserId = 2;
        String reason = "Repeated violations";
        String ipAddress = "192.168.1.1";
        
        UserLockout temporaryLockout = new UserLockout();
        temporaryLockout.setId(1);
        temporaryLockout.setUserId(userId);
        temporaryLockout.setLockoutStart(Timestamp.from(Instant.now().minus(1, ChronoUnit.HOURS)));
        temporaryLockout.setLockoutEnd(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        
        User admin = new User();
        admin.setId(adminUserId);
        admin.setEmail("admin@example.com");
        
        // Mock DAO behavior
        when(userLockoutDao.findActiveByUserId(userId)).thenReturn(Optional.of(temporaryLockout));
        when(userLockoutDao.updateLockoutEnd(eq(temporaryLockout.getId()), isNull())).thenReturn(true);
        when(userDao.findById(userId)).thenReturn(Optional.of(user));
        when(userDao.findById(adminUserId)).thenReturn(Optional.of(admin));
        
        // Execute the method
        boolean result = userLockoutService.makeLockoutPermanent(userId, reason, adminUserId, ipAddress);
        
        // Verify the result
        assertTrue(result);
        
        // Verify DAO interactions
        verify(userLockoutDao).findActiveByUserId(userId);
        verify(userLockoutDao).updateLockoutEnd(eq(temporaryLockout.getId()), isNull());
        verify(userDao, times(2)).findById(anyInt());
        verify(auditService).recordSecurityEvent(any(), any(User.class), eq(ipAddress), contains("Lockout made permanent"), eq("user_lockouts"), eq(temporaryLockout.getId().toString()), eq(reason));
    }
    
    @Test
    public void testGetAllActiveLockouts() throws SQLException {
        // Setup test data
        List<UserLockout> activeLockouts = new ArrayList<>();
        UserLockout lockout1 = new UserLockout();
        lockout1.setId(1);
        activeLockouts.add(lockout1);
        
        UserLockout lockout2 = new UserLockout();
        lockout2.setId(2);
        activeLockouts.add(lockout2);
        
        // Mock DAO behavior
        when(userLockoutDao.findAllActive()).thenReturn(activeLockouts);
        
        // Execute the method
        List<UserLockout> result = userLockoutService.getAllActiveLockouts();
        
        // Verify the result
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify DAO interactions
        verify(userLockoutDao).findAllActive();
    }
    
    @Test
    public void testCleanupExpiredLockouts() throws SQLException {
        // Setup test data
        int expectedDeletedCount = 5;
        
        // Mock DAO behavior
        when(userLockoutDao.deleteExpired()).thenReturn(expectedDeletedCount);
        
        // Execute the method
        int result = userLockoutService.cleanupExpiredLockouts();
        
        // Verify the result
        assertEquals(expectedDeletedCount, result);
        
        // Verify DAO interactions
        verify(userLockoutDao).deleteExpired();
    }
    
    @Test
    public void testShouldLockoutBePermanent() throws SQLException {
        // Setup test data
        Integer userId = 1;
        
        List<UserLockout> lockoutHistory = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            UserLockout lockout = new UserLockout();
            lockout.setId(i + 1);
            lockout.setUserId(userId);
            lockout.setLockoutEnd(new Timestamp(System.currentTimeMillis())); // Not permanent
            lockoutHistory.add(lockout);
        }
        
        // Mock DAO behavior
        when(userLockoutDao.findByUserId(userId)).thenReturn(lockoutHistory);
        
        // Execute the method
        boolean result = userLockoutService.shouldLockoutBePermanent(userId);
        
        // Verify the result
        assertTrue(result);
        
        // Verify DAO interactions
        verify(userLockoutDao).findByUserId(userId);
    }
    
    @Test
    public void testShouldLockoutBePermanentNotEnoughHistory() throws SQLException {
        // Setup test data
        Integer userId = 1;
        
        List<UserLockout> lockoutHistory = new ArrayList<>();
        for (int i = 0; i < 2; i++) { // Only 2 lockouts, threshold is 3
            UserLockout lockout = new UserLockout();
            lockout.setId(i + 1);
            lockout.setUserId(userId);
            lockout.setLockoutEnd(new Timestamp(System.currentTimeMillis())); // Not permanent
            lockoutHistory.add(lockout);
        }
        
        // Mock DAO behavior
        when(userLockoutDao.findByUserId(userId)).thenReturn(lockoutHistory);
        
        // Execute the method
        boolean result = userLockoutService.shouldLockoutBePermanent(userId);
        
        // Verify the result
        assertFalse(result);
        
        // Verify DAO interactions
        verify(userLockoutDao).findByUserId(userId);
    }
}