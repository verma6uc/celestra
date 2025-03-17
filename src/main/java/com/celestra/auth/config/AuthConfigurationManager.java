package com.celestra.auth.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration manager for authentication services.
 * Loads and provides access to authentication configuration properties.
 */
public class AuthConfigurationManager implements AuthConfigProvider {
    
    private static final Logger LOGGER = Logger.getLogger(AuthConfigurationManager.class.getName());
    private static final String CONFIG_FILE = "auth-config.properties";
    private static final AuthConfigurationManager INSTANCE = new AuthConfigurationManager();
    
    private Properties properties;
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private AuthConfigurationManager() {
        loadProperties();
    }
    
    /**
     * Get the singleton instance of the configuration manager.
     * 
     * @return The singleton instance
     */
    public static AuthConfigurationManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Load properties from the configuration file.
     */
    private void loadProperties() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                LOGGER.severe("Unable to find " + CONFIG_FILE);
                throw new RuntimeException("Unable to find " + CONFIG_FILE);
            }
            properties.load(input);
            LOGGER.info("Successfully loaded authentication configuration properties");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error loading authentication configuration properties", ex);
            throw new RuntimeException("Error loading authentication configuration properties", ex);
        }
    }
    
    /**
     * Reload properties from the configuration file.
     * This can be used to refresh the configuration at runtime.
     */
    public void reloadProperties() {
        loadProperties();
    }
    
    /**
     * Get a property value.
     * 
     * @param key The property key
     * @return The property value, or null if not found
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Get a property value with a default value.
     * 
     * @param key The property key
     * @param defaultValue The default value to return if the property is not found
     * @return The property value, or the default value if not found
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Get a property value as an integer.
     * 
     * @param key The property key
     * @param defaultValue The default value to return if the property is not found or not a valid integer
     * @return The property value as an integer, or the default value if not found or not a valid integer
     */
    public int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid integer property value for key: " + key + ", using default value: " + defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Get a property value as a boolean.
     * 
     * @param key The property key
     * @param defaultValue The default value to return if the property is not found
     * @return The property value as a boolean, or the default value if not found
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
    
    // Password Complexity Requirements
    
    @Override
    public int getPasswordMinLength() {
        return getIntProperty("auth.password.min_length", 8);
    }
    
    @Override
    public boolean isPasswordUppercaseRequired() {
        return getBooleanProperty("auth.password.require_uppercase", true);
    }
    
    @Override
    public boolean isPasswordLowercaseRequired() {
        return getBooleanProperty("auth.password.require_lowercase", true);
    }
    
    @Override
    public boolean isPasswordDigitRequired() {
        return getBooleanProperty("auth.password.require_digit", true);
    }
    
    @Override
    public boolean isPasswordSpecialCharRequired() {
        return getBooleanProperty("auth.password.require_special", true);
    }
    
    @Override
    public String getPasswordSpecialChars() {
        return getProperty("auth.password.special_chars", "!@#$%^&*()_-+={}[]|:;\"'<>,.?/~`");
    }
    
    @Override
    public int getPasswordMaxLength() {
        return getIntProperty("auth.password.max_length", 64);
    }
    
    @Override
    public int getPasswordHistoryCount() {
        return getIntProperty("auth.password.history_count", 5);
    }
    
    // Account Lockout Settings
    
    @Override
    public int getLockoutMaxAttempts() {
        return getIntProperty("auth.lockout.max_attempts", 5);
    }
    
    @Override
    public int getLockoutWindowMinutes() {
        return getIntProperty("auth.lockout.window_minutes", 30);
    }
    
    @Override
    public int getLockoutDurationMinutes() {
        return getIntProperty("auth.lockout.duration_minutes", 60);
    }
    
    @Override
    public boolean isLockoutResetCounterAfterSuccess() {
        return getBooleanProperty("auth.lockout.reset_counter_after_success", true);
    }
    
    @Override
    public int getLockoutPermanentAfterConsecutiveTempLockouts() {
        return getIntProperty("auth.lockout.permanent_after_consecutive_temp_lockouts", 3);
    }
    
    // Session Management
    
    @Override
    public int getSessionExpirationMinutes() {
        return getIntProperty("auth.session.expiration_minutes", 120);
    }
    
    @Override
    public boolean isSessionExtendOnActivity() {
        return getBooleanProperty("auth.session.extend_on_activity", true);
    }
    
    @Override
    public int getSessionMaxConcurrentSessions() {
        return getIntProperty("auth.session.max_concurrent_sessions", 5);
    }
    
    @Override
    public boolean isSessionInvalidateOnPasswordChange() {
        return getBooleanProperty("auth.session.invalidate_on_password_change", true);
    }
    
    // Token Expiration Settings
    
    @Override
    public int getPasswordResetTokenExpirationMinutes() {
        return getIntProperty("auth.token.password_reset_minutes", 30);
    }
    
    @Override
    public int getInvitationTokenExpirationDays() {
        return getIntProperty("auth.token.invitation_days", 7);
    }
    
    @Override
    public int getRememberMeTokenExpirationDays() {
        return getIntProperty("auth.token.remember_me_days", 30);
    }
    
    // Security Timeouts
    
    @Override
    public int getIdleTimeoutMinutes() {
        return getIntProperty("auth.timeout.idle_minutes", 15);
    }
    
    @Override
    public int getAbsoluteTimeoutMinutes() {
        return getIntProperty("auth.timeout.absolute_minutes", 480);
    }
    
    // Two-Factor Authentication
    
    @Override
    public boolean isTwoFactorAuthEnabled() {
        return getBooleanProperty("auth.2fa.enabled", false);
    }
    
    @Override
    public boolean isTwoFactorAuthRequiredForAdmins() {
        return getBooleanProperty("auth.2fa.required_for_admins", true);
    }
    
    @Override
    public int getTwoFactorAuthCodeExpirationSeconds() {
        return getIntProperty("auth.2fa.code_expiration_seconds", 300);
    }
    
    @Override
    public int getTwoFactorAuthBackupCodesCount() {
        return getIntProperty("auth.2fa.backup_codes_count", 10);
    }
    
    // Login Settings
    
    @Override
    public boolean isRememberMeAllowed() {
        return getBooleanProperty("auth.login.allow_remember_me", true);
    }
    
    @Override
    public boolean isTrackIpAddressEnabled() {
        return getBooleanProperty("auth.login.track_ip_address", true);
    }
    
    @Override
    public boolean isTrackUserAgentEnabled() {
        return getBooleanProperty("auth.login.track_user_agent", true);
    }
    
    @Override
    public int getRequireCaptchaAfterFailures() {
        return getIntProperty("auth.login.require_captcha_after_failures", 3);
    }
    
    // Registration Settings
    
    @Override
    public boolean isEmailVerificationRequired() {
        return getBooleanProperty("auth.registration.require_email_verification", true);
    }
    
    @Override
    public int getEmailVerificationExpirationHours() {
        return getIntProperty("auth.registration.email_verification_expiration_hours", 24);
    }
    
    @Override
    public boolean isSelfRegistrationAllowed() {
        return getBooleanProperty("auth.registration.allow_self_registration", false);
    }
    
    @Override
    public String getDefaultRole() {
        return getProperty("auth.registration.default_role", "REGULAR_USER");
    }
    
    // Audit Settings
    
    @Override
    public boolean isAuditLogSuccessfulLoginsEnabled() {
        return getBooleanProperty("auth.audit.log_successful_logins", true);
    }
    
    @Override
    public boolean isAuditLogFailedLoginsEnabled() {
        return getBooleanProperty("auth.audit.log_failed_logins", true);
    }
    
    @Override
    public boolean isAuditLogLogoutsEnabled() {
        return getBooleanProperty("auth.audit.log_logouts", true);
    }
    
    @Override
    public boolean isAuditLogPasswordChangesEnabled() {
        return getBooleanProperty("auth.audit.log_password_changes", true);
    }
    
    @Override
    public boolean isAuditLogProfileChangesEnabled() {
        return getBooleanProperty("auth.audit.log_profile_changes", true);
    }
    
    @Override
    public boolean isAuditLogRoleChangesEnabled() {
        return getBooleanProperty("auth.audit.log_role_changes", true);
    }
    
    @Override
    public boolean isAuditLogStatusChangesEnabled() {
        return getBooleanProperty("auth.audit.log_status_changes", true);
    }
}