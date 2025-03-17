package com.celestra.email;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration manager for email services.
 * Loads and provides access to email configuration properties.
 */
public class EmailConfigurationManager {
    
    private static final Logger LOGGER = Logger.getLogger(EmailConfigurationManager.class.getName());
    private static final String CONFIG_FILE = "email-config.properties";
    private static final EmailConfigurationManager INSTANCE = new EmailConfigurationManager();
    
    private Properties properties;
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private EmailConfigurationManager() {
        loadProperties();
    }
    
    /**
     * Get the singleton instance of the configuration manager.
     * 
     * @return The singleton instance
     */
    public static EmailConfigurationManager getInstance() {
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
            LOGGER.info("Successfully loaded email configuration properties");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error loading email configuration properties", ex);
            throw new RuntimeException("Error loading email configuration properties", ex);
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
    
    /**
     * Get the SMTP host.
     * 
     * @return The SMTP host
     */
    public String getSmtpHost() {
        return getProperty("email.smtp.host", "email-smtp.ap-south-1.amazonaws.com");
    }
    
    /**
     * Get the SMTP port.
     * 
     * @return The SMTP port
     */
    public int getSmtpPort() {
        return getIntProperty("email.smtp.port", 465);
    }
    
    /**
     * Check if SMTP authentication is enabled.
     * 
     * @return True if SMTP authentication is enabled, false otherwise
     */
    public boolean isSmtpAuthEnabled() {
        return getBooleanProperty("email.smtp.auth", true);
    }
    
    /**
     * Check if SMTP SSL is enabled.
     * 
     * @return True if SMTP SSL is enabled, false otherwise
     */
    public boolean isSmtpSslEnabled() {
        return getBooleanProperty("email.smtp.ssl.enable", true);
    }
    
    /**
     * Get the SMTP username.
     * 
     * @return The SMTP username
     */
    public String getSmtpUsername() {
        return getProperty("email.username");
    }
    
    /**
     * Get the SMTP password.
     * 
     * @return The SMTP password
     */
    public String getSmtpPassword() {
        return getProperty("email.password");
    }
    
    /**
     * Get the sender email address.
     * 
     * @return The sender email address
     */
    public String getFromAddress() {
        return getProperty("email.from.address", "no-reply@leucinetech.com");
    }
    
    /**
     * Get the sender name.
     * 
     * @return The sender name
     */
    public String getFromName() {
        return getProperty("email.from.name", "Celestra System");
    }
    
    /**
     * Get the number of retry attempts.
     * 
     * @return The number of retry attempts
     */
    public int getRetryAttempts() {
        return getIntProperty("email.retry.attempts", 3);
    }
    
    /**
     * Get the retry delay in milliseconds.
     * 
     * @return The retry delay in milliseconds
     */
    public int getRetryDelayMs() {
        return getIntProperty("email.retry.delay.ms", 1000);
    }
    
    /**
     * Get the connection timeout in milliseconds.
     * 
     * @return The connection timeout in milliseconds
     */
    public int getConnectionTimeout() {
        return getIntProperty("email.connection.timeout", 10000);
    }
    
    /**
     * Get the socket timeout in milliseconds.
     * 
     * @return The socket timeout in milliseconds
     */
    public int getSocketTimeout() {
        return getIntProperty("email.socket.timeout", 10000);
    }
    
    /**
     * Get the JavaMail properties.
     * 
     * @return The JavaMail properties
     */
    public Properties getJavaMailProperties() {
        Properties mailProps = new Properties();
        mailProps.put("mail.smtp.host", getSmtpHost());
        mailProps.put("mail.smtp.port", getSmtpPort());
        mailProps.put("mail.smtp.auth", isSmtpAuthEnabled());
        mailProps.put("mail.smtp.ssl.enable", isSmtpSslEnabled());
        mailProps.put("mail.smtp.connectiontimeout", getConnectionTimeout());
        mailProps.put("mail.smtp.timeout", getSocketTimeout());
        return mailProps;
    }
}