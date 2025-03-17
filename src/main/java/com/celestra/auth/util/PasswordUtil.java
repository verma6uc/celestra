package com.celestra.auth.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.celestra.auth.config.AuthConfigProvider;
import com.celestra.auth.config.AuthConfigurationManager;

/**
 * Utility class for password operations.
 * Provides methods for password hashing, validation, and strength evaluation.
 */
public class PasswordUtil {
    
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16; // bytes
    
    private static final AuthConfigProvider config = AuthConfigurationManager.getInstance();
    
    /**
     * Private constructor to prevent instantiation.
     */
    private PasswordUtil() {
        // Utility class, do not instantiate
    }
    
    /**
     * Generate a secure random salt.
     * 
     * @return A Base64-encoded salt string
     */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * Hash a password with a salt using SHA-256.
     * 
     * @param password The password to hash
     * @param salt The salt to use
     * @return The hashed password
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Create a complete password hash with embedded salt.
     * Format: base64(salt):base64(hash)
     * 
     * @param password The password to hash
     * @return The complete password hash with embedded salt
     */
    public static String createPasswordHash(String password) {
        String salt = generateSalt();
        String hash = hashPassword(password, salt);
        return salt + ":" + hash;
    }
    
    /**
     * Verify a password against a stored hash.
     * 
     * @param password The password to verify
     * @param storedHash The stored hash to verify against
     * @return true if the password matches the hash, false otherwise
     */
    public static boolean verifyPassword(String password, String storedHash) {
        String[] parts = storedHash.split(":");
        if (parts.length != 2) {
            return false;
        }
        
        String salt = parts[0];
        String hash = hashPassword(password, salt);
        
        return (salt + ":" + hash).equals(storedHash);
    }
    
    /**
     * Validate a password against complexity requirements.
     * 
     * @param password The password to validate
     * @return A map of validation results, with keys indicating the validation rule
     *         and values indicating whether the rule passed (true) or failed (false)
     */
    public static Map<String, Boolean> validatePassword(String password) {
        Map<String, Boolean> results = new HashMap<>();
        
        // Check length
        results.put("length", password.length() >= config.getPasswordMinLength() && 
                             password.length() <= config.getPasswordMaxLength());
        
        // Check for uppercase letters
        results.put("uppercase", config.isPasswordUppercaseRequired() ? 
                                Pattern.compile("[A-Z]").matcher(password).find() : true);
        
        // Check for lowercase letters
        results.put("lowercase", config.isPasswordLowercaseRequired() ? 
                                Pattern.compile("[a-z]").matcher(password).find() : true);
        
        // Check for digits
        results.put("digit", config.isPasswordDigitRequired() ? 
                           Pattern.compile("[0-9]").matcher(password).find() : true);
        
        // Check for special characters
        results.put("special", config.isPasswordSpecialCharRequired() ? 
                             Pattern.compile("[" + Pattern.quote(config.getPasswordSpecialChars()) + "]")
                                    .matcher(password).find() : true);
        
        return results;
    }
    
    /**
     * Check if a password meets all complexity requirements.
     * 
     * @param password The password to check
     * @return true if the password meets all requirements, false otherwise
     */
    public static boolean isPasswordValid(String password) {
        Map<String, Boolean> results = validatePassword(password);
        return !results.containsValue(false);
    }
    
    /**
     * Evaluate the strength of a password on a scale of 0-100.
     * 
     * @param password The password to evaluate
     * @return A score from 0 (very weak) to 100 (very strong)
     */
    public static int evaluatePasswordStrength(String password) {
        int score = 0;
        
        // Basic length score (up to 30 points)
        score += Math.min(30, password.length() * 2);
        
        // Character variety (up to 40 points)
        if (Pattern.compile("[A-Z]").matcher(password).find()) score += 10;
        if (Pattern.compile("[a-z]").matcher(password).find()) score += 10;
        if (Pattern.compile("[0-9]").matcher(password).find()) score += 10;
        if (Pattern.compile("[^A-Za-z0-9]").matcher(password).find()) score += 10;
        
        // Complexity patterns (up to 30 points)
        if (Pattern.compile("[A-Z].*[A-Z]").matcher(password).find()) score += 5;
        if (Pattern.compile("[a-z].*[a-z]").matcher(password).find()) score += 5;
        if (Pattern.compile("[0-9].*[0-9]").matcher(password).find()) score += 5;
        if (Pattern.compile("[^A-Za-z0-9].*[^A-Za-z0-9]").matcher(password).find()) score += 5;
        if (Pattern.compile("[A-Za-z0-9].*[^A-Za-z0-9]|[^A-Za-z0-9].*[A-Za-z0-9]").matcher(password).find()) score += 10;
        
        return Math.min(100, score);
    }
}