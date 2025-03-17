package com.celestra.email.config;

import java.util.Properties;

/**
 * Interface for providing email configuration.
 * This interface is used to make the EmailConfigurationManager more testable.
 */
public interface EmailConfigProvider {
    
    /**
     * Get the SMTP host.
     * 
     * @return The SMTP host
     */
    String getSmtpHost();
    
    /**
     * Get the SMTP port.
     * 
     * @return The SMTP port
     */
    int getSmtpPort();
    
    /**
     * Check if SMTP authentication is enabled.
     * 
     * @return True if SMTP authentication is enabled, false otherwise
     */
    boolean isSmtpAuthEnabled();
    
    /**
     * Check if SMTP SSL is enabled.
     * 
     * @return True if SMTP SSL is enabled, false otherwise
     */
    boolean isSmtpSslEnabled();
    
    /**
     * Get the SMTP username.
     * 
     * @return The SMTP username
     */
    String getSmtpUsername();
    
    /**
     * Get the SMTP password.
     * 
     * @return The SMTP password
     */
    String getSmtpPassword();
    
    /**
     * Get the sender email address.
     * 
     * @return The sender email address
     */
    String getFromAddress();
    
    /**
     * Get the sender name.
     * 
     * @return The sender name
     */
    String getFromName();
    
    /**
     * Get the number of retry attempts.
     * 
     * @return The number of retry attempts
     */
    int getRetryAttempts();
    
    /**
     * Get the retry delay in milliseconds.
     * 
     * @return The retry delay in milliseconds
     */
    int getRetryDelayMs();
    
    /**
     * Get the connection timeout in milliseconds.
     * 
     * @return The connection timeout in milliseconds
     */
    int getConnectionTimeout();
    
    /**
     * Get the socket timeout in milliseconds.
     * 
     * @return The socket timeout in milliseconds
     */
    int getSocketTimeout();
    
    /**
     * Get the JavaMail properties.
     * 
     * @return The JavaMail properties
     */
    Properties getJavaMailProperties();
}