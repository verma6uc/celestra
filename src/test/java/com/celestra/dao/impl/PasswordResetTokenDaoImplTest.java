package com.celestra.dao.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.celestra.dao.PasswordResetTokenDao;
import com.celestra.dao.UserDao;
import com.celestra.enums.UserRole;
import com.celestra.enums.UserStatus;
import com.celestra.model.PasswordResetToken;
import com.celestra.model.User;

/**
 * Tests for the PasswordResetTokenDaoImpl class.
 */
public class PasswordResetTokenDaoImplTest {
    
    private PasswordResetTokenDao passwordResetTokenDao;
    private UserDao userDao;
    private User testUser;
    
    @BeforeEach
    public void setUp() throws SQLException {
        passwordResetTokenDao = new PasswordResetTokenDaoImpl();
        userDao = new UserDaoImpl();
        
        // Create a test user if it doesn't exist
        Optional<User> existingUser = userDao.findByEmail("test.reset@example.com");
        if (existingUser.isPresent()) {
            testUser = existingUser.get();
        } else {
            User newUser = new User();
            newUser.setEmail("test.reset@example.com");
            newUser.setName("Test Reset User");
            newUser.setPasswordHash("$2a$10$abcdefghijklmnopqrstuvwxyz0123456789");
            newUser.setRole(UserRole.REGULAR_USER);
            newUser.setStatus(UserStatus.ACTIVE);
            testUser = userDao.create(newUser);
        }
        
        // Clean up any existing tokens for this user
        List<PasswordResetToken> existingTokens = passwordResetTokenDao.findByUserId(testUser.getId());
        for (PasswordResetToken token : existingTokens) {
            passwordResetTokenDao.delete(token.getId());
        }
    }
    
    @Test
    public void testCreateAndFindById() throws SQLException {
        // Create a new token
        PasswordResetToken token = new PasswordResetToken();
        token.setUserId(testUser.getId());
        token.setToken(UUID.randomUUID().toString());
        token.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        token.setExpiresAt(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        
        // Save to database
        PasswordResetToken savedToken = passwordResetTokenDao.create(token);
        
        // Verify ID was generated
        assertNotNull(savedToken.getId());
        
        // Find by ID
        Optional<PasswordResetToken> foundToken = passwordResetTokenDao.findById(savedToken.getId());
        
        // Verify token was found
        assertTrue(foundToken.isPresent());
        assertEquals(savedToken.getId(), foundToken.get().getId());
        assertEquals(testUser.getId(), foundToken.get().getUserId());
        assertEquals(token.getToken(), foundToken.get().getToken());
    }
    
    @Test
    public void testFindByToken() throws SQLException {
        // Create a new token with a known value
        String tokenValue = UUID.randomUUID().toString();
        PasswordResetToken token = new PasswordResetToken();
        token.setUserId(testUser.getId());
        token.setToken(tokenValue);
        token.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        token.setExpiresAt(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        
        // Save to database
        passwordResetTokenDao.create(token);
        
        // Find by token value
        Optional<PasswordResetToken> foundToken = passwordResetTokenDao.findByToken(tokenValue);
        
        // Verify token was found
        assertTrue(foundToken.isPresent());
        assertEquals(tokenValue, foundToken.get().getToken());
        assertEquals(testUser.getId(), foundToken.get().getUserId());
    }
    
    @Test
    public void testFindByUserId() throws SQLException {
        // Create multiple tokens for the same user
        PasswordResetToken token1 = new PasswordResetToken();
        token1.setUserId(testUser.getId());
        token1.setToken(UUID.randomUUID().toString());
        token1.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        token1.setExpiresAt(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        passwordResetTokenDao.create(token1);
        
        PasswordResetToken token2 = new PasswordResetToken();
        token2.setUserId(testUser.getId());
        token2.setToken(UUID.randomUUID().toString());
        token2.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        token2.setExpiresAt(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        passwordResetTokenDao.create(token2);
        
        // Find by user ID
        List<PasswordResetToken> tokens = passwordResetTokenDao.findByUserId(testUser.getId());
        
        // Verify tokens were found
        assertFalse(tokens.isEmpty());
        assertTrue(tokens.size() >= 2);
    }
    
    @Test
    public void testFindActiveByUserId() throws SQLException {
        // Create an active token
        PasswordResetToken activeToken = new PasswordResetToken();
        activeToken.setUserId(testUser.getId());
        activeToken.setToken(UUID.randomUUID().toString());
        activeToken.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        activeToken.setExpiresAt(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        passwordResetTokenDao.create(activeToken);
        
        // Create an expired token
        PasswordResetToken expiredToken = new PasswordResetToken();
        expiredToken.setUserId(testUser.getId());
        expiredToken.setToken(UUID.randomUUID().toString());
        expiredToken.setCreatedAt(Timestamp.from(Instant.now().minus(2, ChronoUnit.HOURS)));
        expiredToken.setExpiresAt(Timestamp.from(Instant.now().minus(1, ChronoUnit.HOURS)));
        passwordResetTokenDao.create(expiredToken);
        
        // Create a used token
        PasswordResetToken usedToken = new PasswordResetToken();
        usedToken.setUserId(testUser.getId());
        usedToken.setToken(UUID.randomUUID().toString());
        usedToken.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        usedToken.setExpiresAt(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        usedToken.setUsedAt(new Timestamp(System.currentTimeMillis()));
        passwordResetTokenDao.create(usedToken);
        
        // Find active tokens
        List<PasswordResetToken> activeTokens = passwordResetTokenDao.findActiveByUserId(testUser.getId());
        
        // Verify only active tokens were found
        assertFalse(activeTokens.isEmpty());
        for (PasswordResetToken token : activeTokens) {
            assertTrue(token.isValid());
        }
    }
    
    @Test
    public void testMarkAsUsed() throws SQLException {
        // Create a new token
        PasswordResetToken token = new PasswordResetToken();
        token.setUserId(testUser.getId());
        token.setToken(UUID.randomUUID().toString());
        token.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        token.setExpiresAt(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        PasswordResetToken savedToken = passwordResetTokenDao.create(token);
        
        // Mark as used
        Timestamp usedAt = new Timestamp(System.currentTimeMillis());
        boolean marked = passwordResetTokenDao.markAsUsed(savedToken.getId(), usedAt);
        
        // Verify token was marked as used
        assertTrue(marked);
        
        // Verify token is now used
        Optional<PasswordResetToken> updatedToken = passwordResetTokenDao.findById(savedToken.getId());
        assertTrue(updatedToken.isPresent());
        assertNotNull(updatedToken.get().getUsedAt());
    }
    
    @Test
    public void testMarkAsUsedByToken() throws SQLException {
        // Create a new token with a known value
        String tokenValue = UUID.randomUUID().toString();
        PasswordResetToken token = new PasswordResetToken();
        token.setUserId(testUser.getId());
        token.setToken(tokenValue);
        token.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        token.setExpiresAt(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        passwordResetTokenDao.create(token);
        
        // Mark as used by token value
        Timestamp usedAt = new Timestamp(System.currentTimeMillis());
        boolean marked = passwordResetTokenDao.markAsUsed(tokenValue, usedAt);
        
        // Verify token was marked as used
        assertTrue(marked);
        
        // Verify token is now used
        Optional<PasswordResetToken> updatedToken = passwordResetTokenDao.findByToken(tokenValue);
        assertTrue(updatedToken.isPresent());
        assertNotNull(updatedToken.get().getUsedAt());
    }
    
    @Test
    public void testInvalidateAllForUser() throws SQLException {
        // Create multiple tokens for the same user
        PasswordResetToken token1 = new PasswordResetToken();
        token1.setUserId(testUser.getId());
        token1.setToken(UUID.randomUUID().toString());
        token1.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        token1.setExpiresAt(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        passwordResetTokenDao.create(token1);
        
        PasswordResetToken token2 = new PasswordResetToken();
        token2.setUserId(testUser.getId());
        token2.setToken(UUID.randomUUID().toString());
        token2.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        token2.setExpiresAt(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        passwordResetTokenDao.create(token2);
        
        // Invalidate all tokens for the user
        int invalidated = passwordResetTokenDao.invalidateAllForUser(testUser.getId());
        
        // Verify tokens were invalidated
        assertTrue(invalidated >= 2);
        
        // Verify no active tokens remain
        List<PasswordResetToken> activeTokens = passwordResetTokenDao.findActiveByUserId(testUser.getId());
        assertTrue(activeTokens.isEmpty());
    }
    
    @Test
    public void testDeleteExpiredTokens() throws SQLException {
        // Create an expired token
        PasswordResetToken expiredToken = new PasswordResetToken();
        expiredToken.setUserId(testUser.getId());
        expiredToken.setToken(UUID.randomUUID().toString());
        expiredToken.setCreatedAt(Timestamp.from(Instant.now().minus(30, ChronoUnit.DAYS)));
        expiredToken.setExpiresAt(Timestamp.from(Instant.now().minus(29, ChronoUnit.DAYS)));
        passwordResetTokenDao.create(expiredToken);
        
        // Delete expired tokens older than 7 days
        Timestamp olderThan = Timestamp.from(Instant.now().minus(7, ChronoUnit.DAYS));
        int deleted = passwordResetTokenDao.deleteExpiredTokens(olderThan);
        
        // Verify expired token was deleted
        assertTrue(deleted >= 1);
        
        // Verify token no longer exists
        Optional<PasswordResetToken> foundToken = passwordResetTokenDao.findById(expiredToken.getId());
        assertFalse(foundToken.isPresent());
    }
}