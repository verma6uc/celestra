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
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.celestra.auth.service.AuditService;
import com.celestra.dao.AuditChangeLogDao;
import com.celestra.dao.AuditLogDao;
import com.celestra.enums.AuditEventType;
import com.celestra.enums.UserStatus;
import com.celestra.model.AuditChangeLog;
import com.celestra.model.AuditLog;
import com.celestra.model.User;

public class AuditServiceImplTest {
    
    @Mock
    private AuditLogDao auditLogDao;
    
    @Mock
    private AuditChangeLogDao auditChangeLogDao;
    
    private AuditService auditService;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        auditService = new AuditServiceImpl(auditLogDao, auditChangeLogDao);
    }

    @Test
    public void testRecordSuccessfulLogin() throws SQLException {
        // Arrange
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        
        String ipAddress = "192.168.1.1";
        
        when(auditLogDao.create(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog log = invocation.getArgument(0);
            log.setId(1);
            return log;
        });
        
        // Act
        AuditLog result = auditService.recordSuccessfulLogin(user, ipAddress);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(user.getId(), result.getUserId());
        assertEquals(AuditEventType.SUCCESSFUL_LOGIN, result.getEventType());
        assertEquals(ipAddress, result.getIpAddress());
        assertTrue(result.getEventDescription().contains("successfully logged in"));
        assertNotNull(result.getDigitalSignature());
        
        // Verify
        verify(auditLogDao).create(any(AuditLog.class));
    }
    
    @Test
    public void testRecordFailedLogin() throws SQLException {
        // Arrange
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        
        String ipAddress = "192.168.1.1";
        String reason = "Invalid password";
        
        when(auditLogDao.create(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog log = invocation.getArgument(0);
            log.setId(1);
            return log;
        });
        
        // Act
        AuditLog result = auditService.recordFailedLogin(user, user.getEmail(), ipAddress, reason);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(user.getId(), result.getUserId());
        assertEquals(AuditEventType.FAILED_LOGIN, result.getEventType());
        assertEquals(ipAddress, result.getIpAddress());
        assertTrue(result.getEventDescription().contains("Failed login attempt"));
        assertTrue(result.getEventDescription().contains(user.getEmail()));
        assertNotNull(result.getDigitalSignature());
        
        // Verify
        verify(auditLogDao).create(any(AuditLog.class));
    }
    
    @Test
    public void testRecordFailedLoginWithNullUser() throws SQLException {
        // Arrange
        String email = "nonexistent@example.com";
        String ipAddress = "192.168.1.1";
        String reason = "User not found";
        
        when(auditLogDao.create(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog log = invocation.getArgument(0);
            log.setId(1);
            return log;
        });
        
        // Act
        AuditLog result = auditService.recordFailedLogin(null, email, ipAddress, reason);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertNull(result.getUserId());
        assertEquals(AuditEventType.FAILED_LOGIN, result.getEventType());
        assertEquals(ipAddress, result.getIpAddress());
        assertTrue(result.getEventDescription().contains("Failed login attempt"));
        assertTrue(result.getEventDescription().contains(email));
        assertTrue(result.getEventDescription().contains(reason));
        assertNotNull(result.getDigitalSignature());
        
        // Verify
        verify(auditLogDao).create(any(AuditLog.class));
    }
    
    @Test
    public void testRecordLogout() throws SQLException {
        // Arrange
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        
        String ipAddress = "192.168.1.1";
        String sessionId = "session-123";
        
        when(auditLogDao.create(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog log = invocation.getArgument(0);
            log.setId(1);
            return log;
        });
        
        // Act
        AuditLog result = auditService.recordLogout(user, ipAddress, sessionId);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(user.getId(), result.getUserId());
        assertEquals(AuditEventType.SESSION_ENDED, result.getEventType());
        assertEquals(ipAddress, result.getIpAddress());
        assertTrue(result.getEventDescription().contains("User logged out"));
        assertTrue(result.getEventDescription().contains(sessionId));
        assertEquals("user_sessions", result.getTableName());
        assertEquals(sessionId, result.getRecordId());
        assertNotNull(result.getDigitalSignature());
        
        // Verify
        verify(auditLogDao).create(any(AuditLog.class));
    }
    
    @Test
    public void testRecordPasswordChange() throws SQLException {
        // Arrange
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        
        String ipAddress = "192.168.1.1";
        
        when(auditLogDao.create(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog log = invocation.getArgument(0);
            log.setId(1);
            return log;
        });
        
        // Act
        AuditLog result = auditService.recordPasswordChange(user, ipAddress, user);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(user.getId(), result.getUserId());
        assertEquals(AuditEventType.OTHER, result.getEventType());
        assertEquals(ipAddress, result.getIpAddress());
        assertTrue(result.getEventDescription().contains("Password changed by user"));
        assertEquals("users", result.getTableName());
        assertEquals(user.getId().toString(), result.getRecordId());
        assertNotNull(result.getDigitalSignature());
        
        // Verify
        verify(auditLogDao).create(any(AuditLog.class));
    }
    
    @Test
    public void testRecordPasswordChangeByAdmin() throws SQLException {
        // Arrange
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        
        User admin = new User();
        admin.setId(2);
        admin.setEmail("admin@example.com");
        
        String ipAddress = "192.168.1.1";
        
        when(auditLogDao.create(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog log = invocation.getArgument(0);
            log.setId(1);
            return log;
        });
        
        // Act
        AuditLog result = auditService.recordPasswordChange(user, ipAddress, admin);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(user.getId(), result.getUserId());
        assertEquals(AuditEventType.OTHER, result.getEventType());
        assertEquals(ipAddress, result.getIpAddress());
        assertTrue(result.getEventDescription().contains("Password changed by administrator"));
        assertEquals("users", result.getTableName());
        assertEquals(user.getId().toString(), result.getRecordId());
        assertEquals(admin.getId(), result.getSignedBy());
        assertNotNull(result.getDigitalSignature());
        
        // Verify
        verify(auditLogDao).create(any(AuditLog.class));
    }
    
    @Test
    public void testRecordChange() throws SQLException {
        // Arrange
        Integer auditLogId = 1;
        String columnName = "status";
        String oldValue = "ACTIVE";
        String newValue = "SUSPENDED";
        
        when(auditChangeLogDao.create(any(AuditChangeLog.class))).thenAnswer(invocation -> {
            AuditChangeLog log = invocation.getArgument(0);
            log.setId(1);
            return log;
        });
        
        // Act
        AuditChangeLog result = auditService.recordChange(auditLogId, columnName, oldValue, newValue);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(auditLogId, result.getAuditLogId());
        assertEquals(columnName, result.getColumnName());
        assertEquals(oldValue, result.getOldValue());
        assertEquals(newValue, result.getNewValue());
        
        // Verify
        verify(auditChangeLogDao).create(any(AuditChangeLog.class));
    }
    
    @Test
    public void testRecordUserStatusChange() throws SQLException {
        // Arrange
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        
        User admin = new User();
        admin.setId(2);
        admin.setEmail("admin@example.com");
        
        String ipAddress = "192.168.1.1";
        String oldStatus = UserStatus.SUSPENDED.name();
        String newStatus = UserStatus.ACTIVE.name(); 
        String reason = "Email verified";
        
        when(auditLogDao.create(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog log = invocation.getArgument(0);
            log.setId(1);
            return log;
        });
        
        // Act
        AuditLog result = auditService.recordUserStatusChange(user, ipAddress, admin, oldStatus, newStatus, reason);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(user.getId(), result.getUserId());
        assertEquals(AuditEventType.OTHER, result.getEventType());
        assertEquals(ipAddress, result.getIpAddress());
        assertTrue(result.getEventDescription().contains("User status changed from"));
        assertTrue(result.getEventDescription().contains(oldStatus));
        assertTrue(result.getEventDescription().contains(newStatus));
        assertTrue(result.getEventDescription().contains("by administrator"));
        assertEquals("users", result.getTableName());
        assertEquals(user.getId().toString(), result.getRecordId());
        assertEquals(admin.getId(), result.getSignedBy());
        assertEquals(reason, result.getReason());
        assertNotNull(result.getDigitalSignature());
        
        // Verify
        verify(auditLogDao).create(any(AuditLog.class));
    }
    
    @Test
    public void testRecordUserUpdate() throws SQLException {
        // Arrange
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        
        String ipAddress = "192.168.1.1";
        
        Map<String, Object> beforeValues = new HashMap<>();
        beforeValues.put("firstName", "John");
        beforeValues.put("lastName", "Doe");
        
        Map<String, Object> afterValues = new HashMap<>();
        afterValues.put("firstName", "John");
        afterValues.put("lastName", "Smith");
        
        when(auditLogDao.create(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog log = invocation.getArgument(0);
            log.setId(1);
            return log;
        });
        
        // Act
        AuditLog result = auditService.recordUserUpdate(user, ipAddress, user, beforeValues, afterValues);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(user.getId(), result.getUserId());
        assertEquals(AuditEventType.OTHER, result.getEventType());
        assertEquals(ipAddress, result.getIpAddress());
        assertTrue(result.getEventDescription().contains("User account updated by user"));
        assertEquals("users", result.getTableName());
        assertEquals(user.getId().toString(), result.getRecordId());
        assertNotNull(result.getDigitalSignature());
        
        // Verify
        verify(auditLogDao).create(any(AuditLog.class));
    }
    
    @Test
    public void testSignAndVerifyAuditLog() throws SQLException {
        // Arrange
        AuditLog auditLog = new AuditLog(AuditEventType.OTHER);
        auditLog.setUserId(1);
        auditLog.setEventDescription("Test event");
        auditLog.setIpAddress("192.168.1.1");
        auditLog.setTableName("users");
        auditLog.setRecordId("1");
        auditLog.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        
        // Act
        AuditLog signedLog = auditService.signAuditLog(auditLog, null);
        boolean isValid = auditService.verifyAuditLogSignature(signedLog);
        
        // Assert
        assertNotNull(signedLog.getDigitalSignature());
        assertTrue(isValid);
        
        // Tamper with the log and verify it fails
        signedLog.setEventDescription("Tampered event");
        boolean isStillValid = auditService.verifyAuditLogSignature(signedLog);
        
        // Assert
        assertFalse(isStillValid);
    }
    
    @Test
    public void testGetAuditLogsForUser() throws SQLException {
        // Arrange
        Integer userId = 1;
        
        List<AuditLog> expectedLogs = new ArrayList<>();
        AuditLog log1 = new AuditLog(AuditEventType.SUCCESSFUL_LOGIN);
        log1.setId(1);
        log1.setUserId(userId);
        expectedLogs.add(log1);
        
        AuditLog log2 = new AuditLog(AuditEventType.SESSION_ENDED);
        log2.setId(2);
        log2.setUserId(userId);
        expectedLogs.add(log2);
        
        when(auditLogDao.findByUserId(userId)).thenReturn(expectedLogs);
        
        // Act
        List<AuditLog> result = auditService.getAuditLogsForUser(userId);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedLogs, result);
        
        // Verify
        verify(auditLogDao).findByUserId(userId);
    }
    
    @Test
    public void testGetAuditLogsByEventType() throws SQLException {
        // Arrange
        AuditEventType eventType = AuditEventType.SUCCESSFUL_LOGIN;
        
        List<AuditLog> expectedLogs = new ArrayList<>();
        AuditLog log1 = new AuditLog(eventType);
        log1.setId(1);
        log1.setUserId(1);
        expectedLogs.add(log1);
        
        AuditLog log2 = new AuditLog(eventType);
        log2.setId(2);
        log2.setUserId(2);
        expectedLogs.add(log2);
        
        when(auditLogDao.findByEventType(eventType)).thenReturn(expectedLogs);
        
        // Act
        List<AuditLog> result = auditService.getAuditLogsByEventType(eventType);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedLogs, result);
        
        // Verify
        verify(auditLogDao).findByEventType(eventType);
    }
    
    @Test
    public void testGetAuditLogsByDateRange() throws SQLException {
        // Arrange
        String startDate = "2025-01-01";
        String endDate = "2025-01-31";
        
        List<AuditLog> expectedLogs = new ArrayList<>();
        AuditLog log1 = new AuditLog(AuditEventType.SUCCESSFUL_LOGIN);
        log1.setId(1);
        expectedLogs.add(log1);
        
        AuditLog log2 = new AuditLog(AuditEventType.FAILED_LOGIN);
        log2.setId(2);
        expectedLogs.add(log2);
        
        when(auditLogDao.findByDateRange(startDate, endDate)).thenReturn(expectedLogs);
        
        // Act
        List<AuditLog> result = auditService.getAuditLogsByDateRange(startDate, endDate);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedLogs, result);
        
        // Verify
        verify(auditLogDao).findByDateRange(startDate, endDate);
    }
    
    @Test
    public void testGetAuditLogsForRecord() throws SQLException {
        // Arrange
        String tableName = "users";
        String recordId = "1";
        
        List<AuditLog> expectedLogs = new ArrayList<>();
        AuditLog log1 = new AuditLog(AuditEventType.OTHER);
        log1.setId(1);
        log1.setTableName(tableName);
        log1.setRecordId(recordId);
        expectedLogs.add(log1);
        
        AuditLog log2 = new AuditLog(AuditEventType.OTHER);
        log2.setId(2);
        log2.setTableName(tableName);
        log2.setRecordId(recordId);
        expectedLogs.add(log2);
        
        when(auditLogDao.findByTableNameAndRecordId(tableName, recordId)).thenReturn(expectedLogs);
        
        // Act
        List<AuditLog> result = auditService.getAuditLogsForRecord(tableName, recordId);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedLogs, result);
        
        // Verify
        verify(auditLogDao).findByTableNameAndRecordId(tableName, recordId);
    }
    
    @Test
    public void testGetAuditLogsByGroupId() throws SQLException {
        // Arrange
        UUID groupId = UUID.randomUUID();
        
        List<AuditLog> expectedLogs = new ArrayList<>();
        AuditLog log1 = new AuditLog(AuditEventType.OTHER);
        log1.setId(1);
        log1.setGroupId(groupId);
        expectedLogs.add(log1);
        
        AuditLog log2 = new AuditLog(AuditEventType.OTHER);
        log2.setId(2);
        log2.setGroupId(groupId);
        expectedLogs.add(log2);
        
        when(auditLogDao.findByGroupId(groupId)).thenReturn(expectedLogs);
        
        // Act
        List<AuditLog> result = auditService.getAuditLogsByGroupId(groupId);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedLogs, result);
        
        // Verify
        verify(auditLogDao).findByGroupId(groupId);
    }
    
    @Test
    public void testGetAuditLogById() throws SQLException {
        // Arrange
        Integer id = 1;
        
        AuditLog expectedLog = new AuditLog(AuditEventType.SUCCESSFUL_LOGIN);
        expectedLog.setId(id);
        
        when(auditLogDao.findById(id)).thenReturn(Optional.of(expectedLog));
        
        // Act
        Optional<AuditLog> result = auditService.getAuditLogById(id);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedLog, result.get());
        
        // Verify
        verify(auditLogDao).findById(id);
    }
}