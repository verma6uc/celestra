package com.celestra.auth.config;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Test class for AuthConfigurationManager.
 */
public class AuthConfigurationManagerTest {
    
    /**
     * Test that the configuration manager loads successfully.
     */
    @Test
    public void testConfigurationLoading() {
        // Get the singleton instance
        AuthConfigProvider config = AuthConfigurationManager.getInstance();
        
        // Verify that the configuration was loaded
        assertNotNull(config, "Configuration manager should not be null");
        
        // Test a few key properties to ensure they were loaded correctly
        assertTrue(config.getPasswordMinLength() >= 8, "Minimum password length should be at least 8");
        assertTrue(config.getLockoutMaxAttempts() > 0, "Max lockout attempts should be positive");
        assertTrue(config.getSessionExpirationMinutes() > 0, "Session expiration should be positive");
    }
    
    /**
     * Test password complexity settings.
     */
    @Test
    public void testPasswordComplexitySettings() {
        AuthConfigProvider config = AuthConfigurationManager.getInstance();
        
        // Verify password complexity settings
        assertTrue(config.getPasswordMinLength() <= config.getPasswordMaxLength(), 
                "Min length should be less than or equal to max length");
        
        // Check that special characters are defined if required
        if (config.isPasswordSpecialCharRequired()) {
            assertFalse(config.getPasswordSpecialChars().isEmpty(), 
                    "Special characters should be defined if required");
        }
    }
    
    /**
     * Test lockout settings.
     */
    @Test
    public void testLockoutSettings() {
        AuthConfigProvider config = AuthConfigurationManager.getInstance();
        
        // Verify lockout settings
        assertTrue(config.getLockoutMaxAttempts() > 0, 
                "Max lockout attempts should be positive");
        assertTrue(config.getLockoutWindowMinutes() > 0, 
                "Lockout window should be positive");
        assertTrue(config.getLockoutDurationMinutes() > 0, 
                "Lockout duration should be positive");
    }
    
    /**
     * Test session settings.
     */
    @Test
    public void testSessionSettings() {
        AuthConfigProvider config = AuthConfigurationManager.getInstance();
        
        // Verify session settings
        assertTrue(config.getSessionExpirationMinutes() > 0, 
                "Session expiration should be positive");
        assertTrue(config.getSessionMaxConcurrentSessions() > 0, 
                "Max concurrent sessions should be positive");
    }
    
    /**
     * Test token expiration settings.
     */
    @Test
    public void testTokenExpirationSettings() {
        AuthConfigProvider config = AuthConfigurationManager.getInstance();
        
        // Verify token expiration settings
        assertTrue(config.getPasswordResetTokenExpirationMinutes() > 0, 
                "Password reset token expiration should be positive");
        assertTrue(config.getInvitationTokenExpirationDays() > 0, 
                "Invitation token expiration should be positive");
        assertTrue(config.getRememberMeTokenExpirationDays() > 0, 
                "Remember me token expiration should be positive");
    }
    
    /**
     * Test two-factor authentication settings.
     */
    @Test
    public void testTwoFactorAuthSettings() {
        AuthConfigProvider config = AuthConfigurationManager.getInstance();
        
        // If 2FA is enabled, verify related settings
        if (config.isTwoFactorAuthEnabled()) {
            assertTrue(config.getTwoFactorAuthCodeExpirationSeconds() > 0, 
                    "2FA code expiration should be positive");
            assertTrue(config.getTwoFactorAuthBackupCodesCount() > 0, 
                    "2FA backup codes count should be positive");
        }
    }
}