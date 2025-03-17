package com.celestra.ai.openai;

import com.celestra.ai.ChatCompletionService;
import com.celestra.ai.config.AIConfigurationManager;
import com.celestra.ai.exception.AIServiceException;
import com.celestra.ai.exception.AuthenticationException;
import com.celestra.ai.exception.InvalidRequestException;
import com.celestra.ai.exception.RateLimitException;
import com.celestra.ai.exception.ServerException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Implementation of the ChatCompletionService interface for OpenAI.
 */
public class OpenAIChatCompletionService implements ChatCompletionService {
    
    private static final Logger LOGGER = Logger.getLogger(OpenAIChatCompletionService.class.getName());
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final int DEFAULT_TIMEOUT_SECONDS = 60;
    
    private final AIConfigurationManager configManager;
    private final HttpClient httpClient;
    private final Gson gson;
    
    /**
     * Create a new OpenAI chat completion service.
     */
    public OpenAIChatCompletionService() {
        this.configManager = AIConfigurationManager.getInstance();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
                .build();
        this.gson = new Gson();
    }
    
    @Override
    public String getChatCompletion(List<ChatMessage> messages) throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("temperature", configManager.getOpenAITemperature());
        parameters.put("max_tokens", configManager.getOpenAIMaxTokens());
        parameters.put("top_p", configManager.getOpenAITopP());
        
        return getChatCompletion(messages, parameters);
    }
    
    @Override
    public String getChatCompletion(List<ChatMessage> messages, Map<String, Object> parameters) throws Exception {
        String apiKey = configManager.getOpenAIApiKey();
        if (apiKey == null || apiKey.isEmpty() || "your-openai-api-key".equals(apiKey)) {
            throw new AuthenticationException("OpenAI API key is not configured");
        }
        
        // Prepare the request body
        Map<String, Object> requestBody = new HashMap<>(parameters);
        requestBody.put("model", configManager.getOpenAIModel());
        requestBody.put("messages", convertMessages(messages));
        
        String requestBodyJson = gson.toJson(requestBody);
        
        // Create the HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS))
                .build();
        
        // Execute the request with retry logic
        int maxRetries = configManager.getOpenAIRetryAttempts();
        int retryDelayMs = configManager.getOpenAIRetryDelayMs();
        
        Exception lastException = null;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                if (attempt > 0) {
                    LOGGER.info("Retrying OpenAI API call (attempt " + attempt + " of " + maxRetries + ")");
                    Thread.sleep(retryDelayMs * attempt); // Exponential backoff
                }
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
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
                LOGGER.log(Level.WARNING, "Error calling OpenAI API", e);
                lastException = e;
            } catch (AIServiceException e) {
                // Don't retry for authentication or invalid request errors
                if (e instanceof AuthenticationException || e instanceof InvalidRequestException) {
                    throw e;
                }
                LOGGER.log(Level.WARNING, "OpenAI API error: " + e.getMessage(), e);
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
     * Convert a list of ChatMessage objects to the format expected by the OpenAI API.
     * 
     * @param messages The list of chat messages
     * @return A list of maps representing the messages in the OpenAI API format
     */
    private List<Map<String, String>> convertMessages(List<ChatMessage> messages) {
        List<Map<String, String>> result = new ArrayList<>();
        for (ChatMessage message : messages) {
            Map<String, String> messageMap = new HashMap<>();
            messageMap.put("role", message.getRole());
            messageMap.put("content", message.getContent());
            result.add(messageMap);
        }
        return result;
    }
    
    /**
     * Handle the HTTP response from the OpenAI API.
     * 
     * @param response The HTTP response
     * @return The generated text
     * @throws AIServiceException If an error occurs
     */
    private String handleResponse(HttpResponse<String> response) throws AIServiceException {
        int statusCode = response.statusCode();
        String responseBody = response.body();
        
        if (statusCode == 200) {
            try {
                return extractResponseText(responseBody);
            } catch (Exception e) {
                throw new AIServiceException("Failed to parse OpenAI API response", e);
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
                    String retryAfter = response.headers().firstValue("Retry-After").orElse(null);
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
                    throw new AIServiceException("OpenAI API error: " + errorMessage, statusCode);
            }
        }
    }
    
    /**
     * Extract the generated text from the OpenAI API response.
     * 
     * @param responseBody The response body
     * @return The generated text
     * @throws Exception If an error occurs
     */
    private String extractResponseText(String responseBody) throws Exception {
        JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonArray choices = jsonResponse.getAsJsonArray("choices");
        
        if (choices != null && choices.size() > 0) {
            JsonObject choice = choices.get(0).getAsJsonObject();
            JsonObject message = choice.getAsJsonObject("message");
            if (message != null && message.has("content")) {
                return message.get("content").getAsString();
            }
        }
        
        throw new AIServiceException("No content found in OpenAI API response");
    }
    
    /**
     * Extract the error message from the OpenAI API error response.
     * 
     * @param responseBody The response body
     * @return The error message
     */
    private String extractErrorMessage(String responseBody) {
        try {
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonObject error = jsonResponse.getAsJsonObject("error");
            if (error != null && error.has("message")) {
                return error.get("message").getAsString();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to parse error message from OpenAI API response", e);
        }
        
        return "Unknown error";
    }
}