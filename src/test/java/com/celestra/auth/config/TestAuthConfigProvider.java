package com.celestra.auth.config;

/**
 * Test implementation of the AuthConfigProvider interface.
 * This class is used for testing purposes to provide controlled configuration values.
 */
public class TestAuthConfigProvider implements AuthConfigProvider {
    
    private int passwordMinLength = 8;
    private boolean passwordUppercaseRequired = true;
    private boolean passwordLowercaseRequired = true;
    private boolean passwordDigitRequired = true;
    private boolean passwordSpecialCharRequired = true;
    private String passwordSpecialChars = "!@#$%^&*()_-+={}[]|:;\"'<>,.?/~`";
    private int passwordMaxLength = 64;
    private int passwordHistoryCount = 5;
    
    private int lockoutMaxAttempts = 5;
    private int lockoutWindowMinutes = 30;
    private int lockoutDurationMinutes = 60;
    private boolean lockoutResetCounterAfterSuccess = true;
    private int lockoutPermanentAfterConsecutiveTempLockouts = 3;
    
    private int sessionExpirationMinutes = 120;
    private boolean sessionExtendOnActivity = true;
    private int sessionMaxConcurrentSessions = 5;
    private boolean sessionInvalidateOnPasswordChange = true;
    
    private int passwordResetTokenExpirationMinutes = 30;
    private int invitationTokenExpirationDays = 7;
    private int rememberMeTokenExpirationDays = 30;
    
    private int idleTimeoutMinutes = 15;
    private int absoluteTimeoutMinutes = 480;
    
    private boolean twoFactorAuthEnabled = false;
    private boolean twoFactorAuthRequiredForAdmins = true;
    private int twoFactorAuthCodeExpirationSeconds = 300;
    private int twoFactorAuthBackupCodesCount = 10;
    
    private boolean rememberMeAllowed = true;
    private boolean trackIpAddressEnabled = true;
    private boolean trackUserAgentEnabled = true;
    private int requireCaptchaAfterFailures = 3;
    
    private boolean emailVerificationRequired = true;
    private int emailVerificationExpirationHours = 24;
    private boolean selfRegistrationAllowed = false;
    private String defaultRole = "REGULAR_USER";
    
    private boolean auditLogSuccessfulLoginsEnabled = true;
    private boolean auditLogFailedLoginsEnabled = true;
    private boolean auditLogLogoutsEnabled = true;
    private boolean auditLogPasswordChangesEnabled = true;
    private boolean auditLogProfileChangesEnabled = true;
    private boolean auditLogRoleChangesEnabled = true;
    private boolean auditLogStatusChangesEnabled = true;
    
    // Getters and setters for customizing test values
    
    public void setPasswordMinLength(int passwordMinLength) {
        this.passwordMinLength = passwordMinLength;
    }
    
    public void setPasswordHistoryCount(int passwordHistoryCount) {
        this.passwordHistoryCount = passwordHistoryCount;
    }

    public void setLockoutMaxAttempts(int lockoutMaxAttempts) {
        this.lockoutMaxAttempts = lockoutMaxAttempts;
    }
    
    public void setLockoutWindowMinutes(int lockoutWindowMinutes) {
        this.lockoutWindowMinutes = lockoutWindowMinutes;
    }

    public void setLockoutDurationMinutes(int lockoutDurationMinutes) {
        this.lockoutDurationMinutes = lockoutDurationMinutes;
    }
    
    public void setLockoutPermanentAfterConsecutiveTempLockouts(int lockoutPermanentAfterConsecutiveTempLockouts) {
        this.lockoutPermanentAfterConsecutiveTempLockouts = lockoutPermanentAfterConsecutiveTempLockouts;
    }
    
    public void setPasswordResetTokenExpirationMinutes(int passwordResetTokenExpirationMinutes) {
        this.passwordResetTokenExpirationMinutes = passwordResetTokenExpirationMinutes;
    }
    
    // AuthConfigProvider implementation
    
    @Override
    public int getPasswordMinLength() {
        return passwordMinLength;
    }
    
    @Override
    public boolean isPasswordUppercaseRequired() {
        return passwordUppercaseRequired;
    }
    
    @Override
    public boolean isPasswordLowercaseRequired() {
        return passwordLowercaseRequired;
    }
    
    @Override
    public boolean isPasswordDigitRequired() {
        return passwordDigitRequired;
    }
    
    @Override
    public boolean isPasswordSpecialCharRequired() {
        return passwordSpecialCharRequired;
    }
    
    @Override
    public String getPasswordSpecialChars() {
        return passwordSpecialChars;
    }
    
    @Override
    public int getPasswordMaxLength() {
        return passwordMaxLength;
    }
    
    @Override
    public int getPasswordHistoryCount() {
        return passwordHistoryCount;
    }
    
    @Override
    public int getLockoutMaxAttempts() {
        return lockoutMaxAttempts;
    }
    
    @Override
    public int getLockoutWindowMinutes() {
        return lockoutWindowMinutes;
    }
    
    @Override
    public int getLockoutDurationMinutes() {
        return lockoutDurationMinutes;
    }
    
    @Override
    public boolean isLockoutResetCounterAfterSuccess() {
        return lockoutResetCounterAfterSuccess;
    }
    
    @Override
    public int getLockoutPermanentAfterConsecutiveTempLockouts() {
        return lockoutPermanentAfterConsecutiveTempLockouts;
    }
    
    @Override
    public int getSessionExpirationMinutes() {
        return sessionExpirationMinutes;
    }
    
    @Override
    public boolean isSessionExtendOnActivity() {
        return sessionExtendOnActivity;
    }
    
    @Override
    public int getSessionMaxConcurrentSessions() {
        return sessionMaxConcurrentSessions;
    }
    
    @Override
    public boolean isSessionInvalidateOnPasswordChange() {
        return sessionInvalidateOnPasswordChange;
    }
    
    @Override
    public int getPasswordResetTokenExpirationMinutes() {
        return passwordResetTokenExpirationMinutes;
    }
    
    @Override
    public int getInvitationTokenExpirationDays() {
        return invitationTokenExpirationDays;
    }
    
    @Override
    public int getRememberMeTokenExpirationDays() {
        return rememberMeTokenExpirationDays;
    }
    
    @Override
    public int getIdleTimeoutMinutes() {
        return idleTimeoutMinutes;
    }
    
    @Override
    public int getAbsoluteTimeoutMinutes() {
        return absoluteTimeoutMinutes;
    }
    
    @Override
    public boolean isTwoFactorAuthEnabled() {
        return twoFactorAuthEnabled;
    }
    
    @Override
    public boolean isTwoFactorAuthRequiredForAdmins() {
        return twoFactorAuthRequiredForAdmins;
    }
    
    @Override
    public int getTwoFactorAuthCodeExpirationSeconds() {
        return twoFactorAuthCodeExpirationSeconds;
    }
    
    @Override
    public int getTwoFactorAuthBackupCodesCount() {
        return twoFactorAuthBackupCodesCount;
    }
    
    @Override
    public boolean isRememberMeAllowed() {
        return rememberMeAllowed;
    }
    
    @Override
    public boolean isTrackIpAddressEnabled() {
        return trackIpAddressEnabled;
    }
    
    @Override
    public boolean isTrackUserAgentEnabled() {
        return trackUserAgentEnabled;
    }
    
    @Override
    public int getRequireCaptchaAfterFailures() {
        return requireCaptchaAfterFailures;
    }
    
    @Override
    public boolean isEmailVerificationRequired() {
        return emailVerificationRequired;
    }
    
    @Override
    public int getEmailVerificationExpirationHours() {
        return emailVerificationExpirationHours;
    }
    
    @Override
    public boolean isSelfRegistrationAllowed() {
        return selfRegistrationAllowed;
    }
    
    @Override
    public String getDefaultRole() {
        return defaultRole;
    }
    
    @Override
    public boolean isAuditLogSuccessfulLoginsEnabled() {
        return auditLogSuccessfulLoginsEnabled;
    }
    
    @Override
    public boolean isAuditLogFailedLoginsEnabled() {
        return auditLogFailedLoginsEnabled;
    }
    
    @Override
    public boolean isAuditLogLogoutsEnabled() {
        return auditLogLogoutsEnabled;
    }
    
    @Override
    public boolean isAuditLogPasswordChangesEnabled() {
        return auditLogPasswordChangesEnabled;
    }
    
    @Override
    public boolean isAuditLogProfileChangesEnabled() {
        return auditLogProfileChangesEnabled;
    }
    
    @Override
    public boolean isAuditLogRoleChangesEnabled() {
        return auditLogRoleChangesEnabled;
    }
    
    @Override
    public boolean isAuditLogStatusChangesEnabled() {
        return auditLogStatusChangesEnabled;
    }
}