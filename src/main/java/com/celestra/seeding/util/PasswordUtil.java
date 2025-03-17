package com.celestra.seeding.util;

import org.mindrot.jbcrypt.BCrypt;

import java.util.Random;

/**
 * Utility class for password operations during data seeding.
 * This class provides methods for generating and hashing passwords.
 */
public class PasswordUtil {
    
    private static final Random random = new Random();
    private static final String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGIT_CHARS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=[]{}|;:,.<>?";
    
    /**
     * Private constructor to prevent instantiation.
     */
    private PasswordUtil() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Generate a random password with the specified length and complexity.
     * 
     * @param length Password length
     * @param includeUppercase Include uppercase letters
     * @param includeDigits Include digits
     * @param includeSpecial Include special characters
     * @return A random password
     */
    public static String generateRandomPassword(int length, boolean includeUppercase, 
                                               boolean includeDigits, boolean includeSpecial) {
        if (length < 8) {
            throw new IllegalArgumentException("Password length must be at least 8 characters");
        }
        
        StringBuilder validChars = new StringBuilder(LOWERCASE_CHARS);
        
        if (includeUppercase) {
            validChars.append(UPPERCASE_CHARS);
        }
        
        if (includeDigits) {
            validChars.append(DIGIT_CHARS);
        }
        
        if (includeSpecial) {
            validChars.append(SPECIAL_CHARS);
        }
        
        StringBuilder password = new StringBuilder(length);
        
        // Ensure at least one character from each required character set
        if (includeUppercase) {
            password.append(UPPERCASE_CHARS.charAt(random.nextInt(UPPERCASE_CHARS.length())));
        }
        
        if (includeDigits) {
            password.append(DIGIT_CHARS.charAt(random.nextInt(DIGIT_CHARS.length())));
        }
        
        if (includeSpecial) {
            password.append(SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length())));
        }
        
        // Add at least one lowercase character
        password.append(LOWERCASE_CHARS.charAt(random.nextInt(LOWERCASE_CHARS.length())));
        
        // Fill the rest of the password with random characters
        for (int i = password.length(); i < length; i++) {
            int index = random.nextInt(validChars.length());
            password.append(validChars.charAt(index));
        }
        
        // Shuffle the password characters
        char[] passwordArray = password.toString().toCharArray();
        for (int i = 0; i < passwordArray.length; i++) {
            int j = random.nextInt(passwordArray.length);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }
        
        return new String(passwordArray);
    }
    
    /**
     * Generate a random password with default complexity (uppercase, digits, special characters).
     * 
     * @param length Password length
     * @return A random password
     */
    public static String generateRandomPassword(int length) {
        return generateRandomPassword(length, true, true, true);
    }
    
    /**
     * Generate a random password with default length (12) and complexity.
     * 
     * @return A random password
     */
    public static String generateRandomPassword() {
        return generateRandomPassword(12);
    }
    
    /**
     * Hash a password using BCrypt.
     * 
     * @param password The password to hash
     * @return The BCrypt hash of the password
     */
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
    
    /**
     * Generate a random password and return its hash.
     * 
     * @return The BCrypt hash of a random password
     */
    public static String generateRandomPasswordHash() {
        return hashPassword(generateRandomPassword());
    }
    
    /**
     * Check if a password matches a hash.
     * 
     * @param password The password to check
     * @param hash The hash to check against
     * @return True if the password matches the hash, false otherwise
     */
    public static boolean checkPassword(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }
}