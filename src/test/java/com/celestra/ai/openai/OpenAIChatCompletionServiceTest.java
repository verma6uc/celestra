package com.celestra.ai.openai;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.celestra.ai.ChatCompletionService.ChatMessage;
import com.celestra.ai.config.AIConfigurationManager;
import com.celestra.ai.exception.AuthenticationException;
import com.celestra.ai.exception.InvalidRequestException;
import com.celestra.ai.exception.RateLimitException;
import com.celestra.ai.exception.ServerException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Test class for OpenAIChatCompletionService.
 */
public class OpenAIChatCompletionServiceTest {
    
    private OpenAIChatCompletionService service;
    
    @Mock
    private HttpClient httpClient;
    
    @Mock
    private HttpResponse<Object> httpResponse;
    
    @Mock
    private AIConfigurationManager configManager;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Create the service
        service = new OpenAIChatCompletionService();
        
        // Inject mocks using reflection
        injectMock(service, "httpClient", httpClient);
        
        // Mock the configuration manager
        Field configManagerField = AIConfigurationManager.class.getDeclaredField("INSTANCE");
        configManagerField.setAccessible(true);
        configManagerField.set(null, configManager);
        
        // Set up default configuration
        when(configManager.getOpenAIApiKey()).thenReturn("test-api-key");
        when(configManager.getOpenAIModel()).thenReturn("o3-mini-2025-01-31");
        when(configManager.getOpenAITemperature()).thenReturn(0.7);
        when(configManager.getOpenAIMaxTokens()).thenReturn(1000);
        when(configManager.getOpenAITopP()).thenReturn(1.0);
        when(configManager.getOpenAIRetryAttempts()).thenReturn(3);
        when(configManager.getOpenAIRetryDelayMs()).thenReturn(1000);
    }
    
    /**
     * Test successful chat completion.
     */
    @Test
    public void testSuccessfulChatCompletion() throws Exception {
        // Prepare test data
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", "You are a helpful assistant."));
        messages.add(new ChatMessage("user", "Hello, how are you?"));
        
        // Mock HTTP response
        String responseBody = "{\"id\":\"chatcmpl-123\",\"object\":\"chat.completion\",\"created\":1677652288,\"model\":\"o3-mini-2025-01-31\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"I'm doing well, thank you for asking!\"},\"finish_reason\":\"stop\"}]}";
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(responseBody);
        when(httpClient.send(any(), any())).thenReturn(httpResponse);
        
        // Call the method
        String result = service.getChatCompletion(messages);
        
        // Verify the result
        assertEquals("I'm doing well, thank you for asking!", result);
        
        // Verify the request
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(requestCaptor.capture(), any());
        
        HttpRequest capturedRequest = requestCaptor.getValue();
        assertEquals("POST", capturedRequest.method());
        assertEquals("https://api.openai.com/v1/chat/completions", capturedRequest.uri().toString());
        assertTrue(capturedRequest.headers().firstValue("Authorization").isPresent());
        assertEquals("Bearer test-api-key", capturedRequest.headers().firstValue("Authorization").get());
        
        // Verify request body
        String requestBody = capturedRequest.bodyPublisher().map(p -> {
            try {
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                p.subscribe(new java.util.concurrent.Flow.Subscriber<>() {
                    @Override
                    public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) {
                        subscription.request(Long.MAX_VALUE);
                    }
                    
                    @Override
                    public void onNext(java.nio.ByteBuffer item) {
                        byte[] bytes = new byte[item.remaining()];
                        item.get(bytes);
                        try {
                            baos.write(bytes);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    
                    @Override
                    public void onError(Throwable throwable) {}
                    
                    @Override
                    public void onComplete() {}
                });
                return baos.toString();
            } catch (Exception e) {
                return null;
            }
        }).orElse(null);
        
        assertNotNull(requestBody);
        JsonObject requestJson = JsonParser.parseString(requestBody).getAsJsonObject();
        assertEquals("o3-mini-2025-01-31", requestJson.get("model").getAsString());
        assertEquals(0.7, requestJson.get("temperature").getAsDouble(), 0.001);
        assertEquals(1000, requestJson.get("max_tokens").getAsInt());
    }
    
    /**
     * Test authentication error.
     */
    @Test
    public void testAuthenticationError() throws Exception {
        // Set up an invalid API key
        when(configManager.getOpenAIApiKey()).thenReturn(null);
        
        // Prepare test data
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "Hello"));
        
        // Call the method and expect an exception
        try {
            service.getChatCompletion(messages);
            fail("Expected AuthenticationException");
        } catch (AuthenticationException e) {
            assertEquals("OpenAI API key is not configured", e.getMessage());
        }
    }
    
    /**
     * Test invalid request error.
     */
    @Test
    public void testInvalidRequestError() throws Exception {
        // Prepare test data
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "Hello"));
        
        // Mock HTTP response for invalid request
        String responseBody = "{\"error\":{\"message\":\"Invalid request\",\"type\":\"invalid_request_error\",\"param\":null,\"code\":null}}";
        when(httpResponse.statusCode()).thenReturn(400);
        when(httpResponse.body()).thenReturn(responseBody);
        when(httpClient.send(any(), any())).thenReturn(httpResponse);
        
        // Call the method and expect an exception
        try {
            service.getChatCompletion(messages);
            fail("Expected InvalidRequestException");
        } catch (InvalidRequestException e) {
            assertEquals("Invalid request: Invalid request", e.getMessage());
        }
    }
    
    /**
     * Test rate limit error.
     */
    @Test
    public void testRateLimitError() throws Exception {
        // Prepare test data
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "Hello"));
        
        // Mock HTTP response for rate limit
        String responseBody = "{\"error\":{\"message\":\"Rate limit exceeded\",\"type\":\"rate_limit_error\",\"param\":null,\"code\":null}}";
        when(httpResponse.statusCode()).thenReturn(429);
        when(httpResponse.body()).thenReturn(responseBody);
        when(httpClient.send(any(), any())).thenReturn(httpResponse);
        
        // Set up retry attempts to 0 to avoid waiting in the test
        when(configManager.getOpenAIRetryAttempts()).thenReturn(0);
        
        // Call the method and expect an exception
        try {
            service.getChatCompletion(messages);
            fail("Expected RateLimitException");
        } catch (RateLimitException e) {
            assertEquals("Rate limit exceeded: Rate limit exceeded", e.getMessage());
        }
    }
    
    /**
     * Test server error.
     */
    @Test
    public void testServerError() throws Exception {
        // Prepare test data
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "Hello"));
        
        // Mock HTTP response for server error
        String responseBody = "{\"error\":{\"message\":\"Server error\",\"type\":\"server_error\",\"param\":null,\"code\":null}}";
        when(httpResponse.statusCode()).thenReturn(500);
        when(httpResponse.body()).thenReturn(responseBody);
        when(httpClient.send(any(), any())).thenReturn(httpResponse);
        
        // Set up retry attempts to 0 to avoid waiting in the test
        when(configManager.getOpenAIRetryAttempts()).thenReturn(0);
        
        // Call the method and expect an exception
        try {
            service.getChatCompletion(messages);
            fail("Expected ServerException");
        } catch (ServerException e) {
            assertEquals("Server error: Server error", e.getMessage());
        }
    }
    
    /**
     * Test custom parameters.
     */
    @Test
    public void testCustomParameters() throws Exception {
        // Prepare test data
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "Hello"));
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("temperature", 0.5);
        parameters.put("max_tokens", 500);
        parameters.put("top_p", 0.8);
        
        // Mock HTTP response
        String responseBody = "{\"id\":\"chatcmpl-123\",\"object\":\"chat.completion\",\"created\":1677652288,\"model\":\"o3-mini-2025-01-31\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"Hello there!\"},\"finish_reason\":\"stop\"}]}";
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(responseBody);
        when(httpClient.send(any(), any())).thenReturn(httpResponse);
        
        // Call the method
        String result = service.getChatCompletion(messages, parameters);
        
        // Verify the result
        assertEquals("Hello there!", result);
        
        // Verify the request
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(requestCaptor.capture(), any());
        
        HttpRequest capturedRequest = requestCaptor.getValue();
        
        // Verify request body
        String requestBody = capturedRequest.bodyPublisher().map(p -> {
            try {
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                p.subscribe(new java.util.concurrent.Flow.Subscriber<>() {
                    @Override
                    public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) {
                        subscription.request(Long.MAX_VALUE);
                    }
                    
                    @Override
                    public void onNext(java.nio.ByteBuffer item) {
                        byte[] bytes = new byte[item.remaining()];
                        item.get(bytes);
                        try {
                            baos.write(bytes);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    
                    @Override
                    public void onError(Throwable throwable) {}
                    
                    @Override
                    public void onComplete() {}
                });
                return baos.toString();
            } catch (Exception e) {
                return null;
            }
        }).orElse(null);
        
        assertNotNull(requestBody);
        JsonObject requestJson = JsonParser.parseString(requestBody).getAsJsonObject();
        assertEquals(0.5, requestJson.get("temperature").getAsDouble(), 0.001);
        assertEquals(500, requestJson.get("max_tokens").getAsInt());
        assertEquals(0.8, requestJson.get("top_p").getAsDouble(), 0.001);
    }
    
    /**
     * Test retry mechanism.
     */
    @Test
    public void testRetryMechanism() throws Exception {
        // Prepare test data
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "Hello"));
        
        // Mock HTTP response for rate limit, then success
        String errorResponseBody = "{\"error\":{\"message\":\"Rate limit exceeded\",\"type\":\"rate_limit_error\",\"param\":null,\"code\":null}}";
        String successResponseBody = "{\"id\":\"chatcmpl-123\",\"object\":\"chat.completion\",\"created\":1677652288,\"model\":\"o3-mini-2025-01-31\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"Hello there!\"},\"finish_reason\":\"stop\"}]}";
        
        HttpResponse<Object> errorResponse = mock(HttpResponse.class);
        when(errorResponse.statusCode()).thenReturn(429);
        when(errorResponse.body()).thenReturn(errorResponseBody);
        
        HttpResponse<Object> successResponse = mock(HttpResponse.class);
        when(successResponse.statusCode()).thenReturn(200);
        when(successResponse.body()).thenReturn(successResponseBody);
        
        // First call returns error, second call returns success
        when(httpClient.send(any(), any()))
                .thenReturn(errorResponse)
                .thenReturn(successResponse);
        
        // Set up retry attempts to 1 to test retry
        when(configManager.getOpenAIRetryAttempts()).thenReturn(1);
        when(configManager.getOpenAIRetryDelayMs()).thenReturn(0); // No delay for testing
        
        // Call the method
        String result = service.getChatCompletion(messages);
        
        // Verify the result
        assertEquals("Hello there!", result);
        
        // Verify the request was sent twice
        verify(httpClient, times(2)).send(any(), any());
    }
    
    /**
     * Helper method to inject a mock into a private field using reflection.
     */
    private void injectMock(Object target, String fieldName, Object mockObject) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mockObject);
    }
}