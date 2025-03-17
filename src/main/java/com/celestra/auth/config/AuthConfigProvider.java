package com.celestra.auth.config;

/**
 * Interface for providing authentication configuration settings.
 * This allows for different implementations (e.g., from properties file, database, etc.)
 */
public interface AuthConfigProvider {
    
    // Password Complexity Requirements
    int getPasswordMinLength();
    boolean isPasswordUppercaseRequired();
    boolean isPasswordLowercaseRequired();
    boolean isPasswordDigitRequired();
    boolean isPasswordSpecialCharRequired();
    String getPasswordSpecialChars();
    int getPasswordMaxLength();
    int getPasswordHistoryCount();
    
    // Account Lockout Settings
    int getLockoutMaxAttempts();
    int getLockoutWindowMinutes();
    int getLockoutDurationMinutes();
    boolean isLockoutResetCounterAfterSuccess();
    int getLockoutPermanentAfterConsecutiveTempLockouts();
    
    // Session Management
    int getSessionExpirationMinutes();
    boolean isSessionExtendOnActivity();
    int getSessionMaxConcurrentSessions();
    boolean isSessionInvalidateOnPasswordChange();
    
    // Token Expiration Settings
    int getPasswordResetTokenExpirationMinutes();
    int getInvitationTokenExpirationDays();
    int getRememberMeTokenExpirationDays();
    
    // Security Timeouts
    int getIdleTimeoutMinutes();
    int getAbsoluteTimeoutMinutes();
    
    // Two-Factor Authentication
    boolean isTwoFactorAuthEnabled();
    boolean isTwoFactorAuthRequiredForAdmins();
    int getTwoFactorAuthCodeExpirationSeconds();
    int getTwoFactorAuthBackupCodesCount();
    
    // Login Settings
    boolean isRememberMeAllowed();
    boolean isTrackIpAddressEnabled();
    boolean isTrackUserAgentEnabled();
    int getRequireCaptchaAfterFailures();
    
    // Registration Settings
    boolean isEmailVerificationRequired();
    int getEmailVerificationExpirationHours();
    boolean isSelfRegistrationAllowed();
    String getDefaultRole();
    
    // Audit Settings
    boolean isAuditLogSuccessfulLoginsEnabled();
    boolean isAuditLogFailedLoginsEnabled();
    boolean isAuditLogLogoutsEnabled();
    boolean isAuditLogPasswordChangesEnabled();
    boolean isAuditLogProfileChangesEnabled();
    boolean isAuditLogRoleChangesEnabled();
    boolean isAuditLogStatusChangesEnabled();
}