package com.celestra.ai.claude;

import com.celestra.ai.ChatCompletionService;
import com.celestra.ai.config.AIConfigurationManager;
import com.celestra.ai.http.DefaultHttpClientWrapper;
import com.celestra.ai.http.HttpClientWrapper;
import com.celestra.ai.exception.AIServiceException;
import com.celestra.ai.exception.AuthenticationException;
import com.celestra.ai.exception.InvalidRequestException;
import com.celestra.ai.exception.RateLimitException;
import com.celestra.ai.exception.ServerException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Implementation of the ChatCompletionService interface for Claude.
 */
public class ClaudeChatCompletionService implements ChatCompletionService {
    
    private static final Logger LOGGER = Logger.getLogger(ClaudeChatCompletionService.class.getName());
    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String API_VERSION = "2023-06-01";
    private static final int DEFAULT_TIMEOUT_SECONDS = 60;
    
    private final AIConfigurationManager configManager;
    private final HttpClientWrapper httpClient;
    private final Gson gson;
    
    /**
     * Create a new Claude chat completion service.
     */
    public ClaudeChatCompletionService() {
        this.configManager = AIConfigurationManager.getInstance();
        this.httpClient = new DefaultHttpClientWrapper(DEFAULT_TIMEOUT_SECONDS);
        this.gson = new Gson();
    }
    
    /**
     * Create a new Claude chat completion service with a custom HTTP client wrapper.
     * This constructor is primarily used for testing.
     */
    public ClaudeChatCompletionService(AIConfigurationManager configManager, HttpClientWrapper httpClient) {
        this.configManager = configManager;
        this.httpClient = httpClient;
        this.gson = new Gson();
    }
    
    @Override
    public String getChatCompletion(List<ChatMessage> messages) throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("temperature", configManager.getClaudeTemperature());
        parameters.put("max_tokens", configManager.getClaudeMaxTokens());
        parameters.put("top_p", configManager.getClaudeTopP());
        
        return getChatCompletion(messages, parameters);
    }
    
    @Override
    public String getChatCompletion(List<ChatMessage> messages, Map<String, Object> parameters) throws Exception {
        String apiKey = configManager.getClaudeApiKey();
        if (apiKey == null || apiKey.isEmpty() || "your-claude-api-key".equals(apiKey)) {
            throw new AuthenticationException("Claude API key is not configured");
        }
        
        // Prepare the request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", configManager.getClaudeModel());
        requestBody.put("messages", convertMessages(messages));
        
        // Add parameters
        if (parameters.containsKey("temperature")) {
            requestBody.put("temperature", parameters.get("temperature"));
        }
        if (parameters.containsKey("max_tokens")) {
            requestBody.put("max_tokens", parameters.get("max_tokens"));
        }
        if (parameters.containsKey("top_p")) {
            requestBody.put("top_p", parameters.get("top_p"));
        }
        
        String requestBodyJson = gson.toJson(requestBody);
        
        // Create the HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", API_VERSION)
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
                .build();
        
        // Execute the request with retry logic
        int maxRetries = configManager.getClaudeRetryAttempts();
        int retryDelayMs = configManager.getClaudeRetryDelayMs();
        
        Exception lastException = null;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                if (attempt > 0) {
                    LOGGER.info("Retrying Claude API call (attempt " + attempt + " of " + maxRetries + ")");
                    Thread.sleep(retryDelayMs * attempt); // Exponential backoff
                }
                
                HttpClientWrapper.SimpleHttpResponse response = httpClient.sendRequest(request);
                return handleResponse(response);
            } catch (RateLimitException e) {
                LOGGER.warning("Rate limit exceeded: " + e.getMessage());
                lastException = e;
                // For rate limit exceptions, use the retry-after header if available
                long retryAfter = e.getRetryAfterMs();
                if (retryAfter > 0) {
                    Thread.sleep(retryAfter);
                }
            } catch (IOException | InterruptedException e) {
                LOGGER.log(Level.WARNING, "Error calling Claude API", e);
                lastException = e;
            } catch (AIServiceException e) {
                // Don't retry for authentication or invalid request errors
                if (e instanceof AuthenticationException || e instanceof InvalidRequestException) {
                    throw e;
                }
                LOGGER.log(Level.WARNING, "Claude API error: " + e.getMessage(), e);
                lastException = e;
            }
        }
        
        // If we've exhausted all retries, throw the last exception
        if (lastException != null) {
            throw new AIServiceException("Failed to get chat completion after " + maxRetries + " retries", 
                    lastException);
        }
        
        throw new AIServiceException("Failed to get chat completion");
    }
    
    /**
     * Convert a list of ChatMessage objects to the format expected by the Claude API.
     * 
     * @param messages The list of chat messages
     * @return A list of maps representing the messages in the Claude API format
     */
    private List<Map<String, String>> convertMessages(List<ChatMessage> messages) {
        List<Map<String, String>> result = new ArrayList<>();
        
        // Claude API expects a specific format for messages
        for (ChatMessage message : messages) {
            Map<String, String> messageMap = new HashMap<>();
            
            // Map our roles to Claude's roles
            String role = message.getRole();
            if ("system".equals(role)) {
                // For Claude, system messages are handled differently
                // We'll add them as user messages with a special prefix
                messageMap.put("role", "user");
                messageMap.put("content", "<system>\n" + message.getContent() + "\n</system>");
            } else if ("assistant".equals(role)) {
                messageMap.put("role", "assistant");
                messageMap.put("content", message.getContent());
            } else if ("user".equals(role)) {
                messageMap.put("role", "user");
                messageMap.put("content", message.getContent());
            } else {
                // Default to user role for unknown roles
                messageMap.put("role", "user");
                messageMap.put("content", message.getContent());
            }
            
            result.add(messageMap);
        }
        
        return result;
    }
    
    /**
     * Handle the HTTP response from the Claude API.
     * 
     * @param response The HTTP response
     * @return The generated text
     * @throws AIServiceException If an error occurs
     */
    private String handleResponse(HttpClientWrapper.SimpleHttpResponse response) throws AIServiceException {
        int statusCode = response.statusCode();
        String responseBody = response.body();
        
        if (statusCode == 200) {
            try {
                return extractResponseText(responseBody);
            } catch (Exception e) {
                throw new AIServiceException("Failed to parse Claude API response", e);
            }
        } else {
            String errorMessage = extractErrorMessage(responseBody);
            
            switch (statusCode) {
                case 400:
                    throw new InvalidRequestException("Invalid request: " + errorMessage);
                case 401:
                    throw new AuthenticationException("Authentication failed: " + errorMessage);
                case 429:
                    // Extract retry-after header if available
                    long retryAfterMs = 0;
                    String retryAfter = response.headers().containsKey("Retry-After") ? response.headers().get("Retry-After").get(0) : null;
                    if (retryAfter != null) {
                        try {
                            retryAfterMs = Long.parseLong(retryAfter) * 1000; // Convert seconds to milliseconds
                        } catch (NumberFormatException e) {
                            // Ignore parsing errors
                        }
                    }
                    throw new RateLimitException("Rate limit exceeded: " + errorMessage, retryAfterMs);
                case 500:
                case 502:
                case 503:
                case 504:
                    throw new ServerException("Server error: " + errorMessage, statusCode);
                default:
                    throw new AIServiceException("Claude API error: " + errorMessage, statusCode);
            }
        }
    }
    
    /**
     * Extract the generated text from the Claude API response.
     * 
     * @param responseBody The response body
     * @return The generated text
     * @throws Exception If an error occurs
     */
    private String extractResponseText(String responseBody) throws Exception {
        JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
        
        if (jsonResponse.has("content")) {
            JsonArray contentArray = jsonResponse.getAsJsonArray("content");
            if (contentArray != null && contentArray.size() > 0) {
                JsonObject contentObject = contentArray.get(0).getAsJsonObject();
                if (contentObject.has("text")) {
                    return contentObject.get("text").getAsString();
                }
            }
        }
        
        throw new AIServiceException("No content found in Claude API response");
    }
    
    /**
     * Extract the error message from the Claude API error response.
     * 
     * @param responseBody The response body
     * @return The error message
     */
    private String extractErrorMessage(String responseBody) {
        try {
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            if (jsonResponse.has("error")) {
                JsonObject error = jsonResponse.getAsJsonObject("error");
                if (error.has("message")) {
                    return error.get("message").getAsString();
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse error message from Claude API response", e);
        }
        
        return "Unknown error";
    }
}