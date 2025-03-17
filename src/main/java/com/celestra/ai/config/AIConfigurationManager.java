package com.celestra.ai.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration manager for AI services.
 * Loads and provides access to configuration properties for OpenAI and Claude services.
 */
public class AIConfigurationManager {
    
    private static final Logger LOGGER = Logger.getLogger(AIConfigurationManager.class.getName());
    private static final String CONFIG_FILE = "ai-config.properties";
    private static final AIConfigurationManager INSTANCE = new AIConfigurationManager();
    
    private Properties properties;
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private AIConfigurationManager() {
        loadProperties();
    }
    
    /**
     * Get the singleton instance of the configuration manager.
     * 
     * @return The singleton instance
     */
    public static AIConfigurationManager getInstance() {
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
            LOGGER.info("Successfully loaded AI configuration properties");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error loading AI configuration properties", ex);
            throw new RuntimeException("Error loading AI configuration properties", ex);
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
     * Get a property value as a double.
     * 
     * @param key The property key
     * @param defaultValue The default value to return if the property is not found or not a valid double
     * @return The property value as a double, or the default value if not found or not a valid double
     */
    public double getDoubleProperty(String key, double defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid double property value for key: " + key + ", using default value: " + defaultValue);
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
    
    // OpenAI specific getters
    
    /**
     * Get the OpenAI API key.
     * 
     * @return The OpenAI API key
     */
    public String getOpenAIApiKey() {
        return getProperty("openai.api.key");
    }
    
    /**
     * Get the OpenAI model identifier.
     * 
     * @return The OpenAI model identifier
     */
    public String getOpenAIModel() {
        return getProperty("openai.model", "o3-mini-2025-01-31");
    }
    
    /**
     * Get the OpenAI token limit.
     * 
     * @return The OpenAI token limit
     */
    public int getOpenAITokenLimit() {
        return getIntProperty("openai.token.limit", 64000);
    }
    
    /**
     * Get the OpenAI temperature parameter.
     * 
     * @return The OpenAI temperature parameter
     */
    public double getOpenAITemperature() {
        return getDoubleProperty("openai.temperature", 0.7);
    }
    
    /**
     * Get the OpenAI top_p parameter.
     * 
     * @return The OpenAI top_p parameter
     */
    public double getOpenAITopP() {
        return getDoubleProperty("openai.top.p", 1.0);
    }
    
    /**
     * Get the OpenAI max tokens parameter.
     * 
     * @return The OpenAI max tokens parameter
     */
    public int getOpenAIMaxTokens() {
        return getIntProperty("openai.max.tokens", 1000);
    }
    
    /**
     * Get the OpenAI retry attempts.
     * 
     * @return The OpenAI retry attempts
     */
    public int getOpenAIRetryAttempts() {
        return getIntProperty("openai.retry.attempts", 3);
    }
    
    /**
     * Get the OpenAI retry delay in milliseconds.
     * 
     * @return The OpenAI retry delay in milliseconds
     */
    public int getOpenAIRetryDelayMs() {
        return getIntProperty("openai.retry.delay.ms", 1000);
    }
    
    // Claude specific getters
    
    /**
     * Get the Claude API key.
     * 
     * @return The Claude API key
     */
    public String getClaudeApiKey() {
        return getProperty("claude.api.key");
    }
    
    /**
     * Get the Claude model identifier.
     * 
     * @return The Claude model identifier
     */
    public String getClaudeModel() {
        return getProperty("claude.model", "claude-3-7-sonnet-20250219");
    }
    
    /**
     * Get the Claude token limit.
     * 
     * @return The Claude token limit
     */
    public int getClaudeTokenLimit() {
        return getIntProperty("claude.token.limit", 8192);
    }
    
    /**
     * Get the Claude temperature parameter.
     * 
     * @return The Claude temperature parameter
     */
    public double getClaudeTemperature() {
        return getDoubleProperty("claude.temperature", 0.7);
    }
    
    /**
     * Get the Claude top_p parameter.
     * 
     * @return The Claude top_p parameter
     */
    public double getClaudeTopP() {
        return getDoubleProperty("claude.top.p", 1.0);
    }
    
    /**
     * Get the Claude max tokens parameter.
     * 
     * @return The Claude max tokens parameter
     */
    public int getClaudeMaxTokens() {
        return getIntProperty("claude.max.tokens", 1000);
    }
    
    /**
     * Get the Claude retry attempts.
     * 
     * @return The Claude retry attempts
     */
    public int getClaudeRetryAttempts() {
        return getIntProperty("claude.retry.attempts", 3);
    }
    
    /**
     * Get the Claude retry delay in milliseconds.
     * 
     * @return The Claude retry delay in milliseconds
     */
    public int getClaudeRetryDelayMs() {
        return getIntProperty("claude.retry.delay.ms", 1000);
    }
    
    // Common getters
    
    /**
     * Get the default AI service.
     * 
     * @return The default AI service
     */
    public String getDefaultAIService() {
        return getProperty("ai.default.service", "openai");
    }
}