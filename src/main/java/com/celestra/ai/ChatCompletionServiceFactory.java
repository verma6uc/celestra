package com.celestra.ai;

import com.celestra.ai.claude.ClaudeChatCompletionService;
import com.celestra.ai.config.AIConfigurationManager;
import com.celestra.ai.openai.OpenAIChatCompletionService;

/**
 * Factory class for creating instances of ChatCompletionService.
 */
public class ChatCompletionServiceFactory {
    
    private static final AIConfigurationManager CONFIG_MANAGER = AIConfigurationManager.getInstance();
    
    /**
     * Get a chat completion service based on the default AI service configured.
     * 
     * @return A chat completion service
     */
    public static ChatCompletionService getDefaultService() {
        String defaultService = CONFIG_MANAGER.getDefaultAIService();
        return getService(defaultService);
    }
    
    /**
     * Get a chat completion service for the specified AI service.
     * 
     * @param serviceName The name of the AI service ("openai" or "claude")
     * @return A chat completion service
     * @throws IllegalArgumentException If the service name is not recognized
     */
    public static ChatCompletionService getService(String serviceName) {
        if ("openai".equalsIgnoreCase(serviceName)) {
            return new OpenAIChatCompletionService();
        } else if ("claude".equalsIgnoreCase(serviceName)) {
            return new ClaudeChatCompletionService();
        } else {
            throw new IllegalArgumentException("Unknown AI service: " + serviceName);
        }
    }
    
    /**
     * Get the OpenAI chat completion service.
     * 
     * @return The OpenAI chat completion service
     */
    public static ChatCompletionService getOpenAIService() {
        return new OpenAIChatCompletionService();
    }
    
    /**
     * Get the Claude chat completion service.
     * 
     * @return The Claude chat completion service
     */
    public static ChatCompletionService getClaudeService() {
        return new ClaudeChatCompletionService();
    }
}