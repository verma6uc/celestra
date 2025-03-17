package com.celestra.auth.util;

import java.util.regex.Pattern;

/**
 * Utility class for email operations.
 * Provides methods for email validation and normalization.
 */
public class EmailUtil {
    
    // RFC 5322 compliant email regex pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$"
    );
    
    // Maximum allowed length for an email address
    private static final int MAX_EMAIL_LENGTH = 254;
    
    /**
     * Private constructor to prevent instantiation.
     */
    private EmailUtil() {
        // Utility class, do not instantiate
    }
    
    /**
     * Validate an email address.
     * 
     * @param email The email address to validate
     * @return true if the email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty() || email.length() > MAX_EMAIL_LENGTH) {
            return false;
        }
        
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Normalize an email address by converting it to lowercase.
     * 
     * @param email The email address to normalize
     * @return The normalized email address
     */
    public static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        
        return email.trim().toLowerCase();
    }
    
    /**
     * Mask an email address for display purposes.
     * Example: "user@example.com" becomes "u***r@e***e.com"
     * 
     * @param email The email address to mask
     * @return The masked email address
     */
    public static String maskEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return email;
        }
        
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return email;
        }
        
        String username = parts[0];
        String domain = parts[1];
        
        // Mask username
        String maskedUsername;
        if (username.length() <= 2) {
            maskedUsername = username;
        } else {
            maskedUsername = username.charAt(0) + 
                             "*".repeat(username.length() - 2) + 
                             username.charAt(username.length() - 1);
        }
        
        // Mask domain (except TLD)
        String[] domainParts = domain.split("\\.");
        if (domainParts.length < 2) {
            return maskedUsername + "@" + domain;
        }
        
        String domainName = String.join(".", 
            java.util.Arrays.copyOf(domainParts, domainParts.length - 1));
        String tld = domainParts[domainParts.length - 1];
        
        String maskedDomain;
        if (domainName.length() <= 2) {
            maskedDomain = domainName;
        } else {
            maskedDomain = domainName.charAt(0) + 
                           "*".repeat(domainName.length() - 2) + 
                           domainName.charAt(domainName.length() - 1);
        }
        
        return maskedUsername + "@" + maskedDomain + "." + tld;
    }
    
    /**
     * Extract the domain part from an email address.
     * 
     * @param email The email address
     * @return The domain part of the email address, or null if the email is invalid
     */
    public static String extractDomain(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return null;
        }
        
        return parts[1];
    }
}