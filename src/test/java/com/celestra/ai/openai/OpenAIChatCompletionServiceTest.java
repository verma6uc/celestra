package com.celestra.ai.openai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.celestra.ai.ChatCompletionService;
import com.celestra.ai.ChatCompletionService.ChatMessage;
import com.celestra.ai.exception.AIServiceException;
import com.celestra.ai.exception.AuthenticationException;

/**
 * Simple test class for OpenAIChatCompletionService.
 * This class contains a main method to test the service functionality.
 */
public class OpenAIChatCompletionServiceTest {
    
    public static void main(String[] args) {
        System.out.println("Testing OpenAIChatCompletionService...");
        
        // Create the service
        OpenAIChatCompletionService service = new OpenAIChatCompletionService();
        
        // Since we have valid API keys, we can run these tests
        testSuccessfulChatCompletion(service);
        testCustomParameters(service);
        // Uncomment them if you have a valid API key
        
        // testSuccessfulChatCompletion(service);
        // testCustomParameters(service);
        
        System.out.println("All tests completed.");
    }
    
    /**
     * Test successful chat completion.
     */
    private static void testSuccessfulChatCompletion(ChatCompletionService service) {
        System.out.println("\nTesting successful chat completion...");
        
        try {
            // Prepare test data
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("system", "You are a helpful assistant."));
            messages.add(new ChatMessage("user", "Hello, how are you?"));
            
            // Call the method
            String result = service.getChatCompletion(messages);
            
            // Print the result
            System.out.println("Response: " + result);
            System.out.println("Test passed!");
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test authentication error.
     */
    private static void testAuthenticationError(ChatCompletionService service) {
        System.out.println("\nTesting authentication error...");
        
        try {
            // Prepare test data
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("user", "Hello"));
            
            // Call the method
            service.getChatCompletion(messages);
            
            // If we get here, the test failed
            System.err.println("Test failed: Expected AuthenticationException");
        } catch (AuthenticationException e) {
            // This is expected
            System.out.println("Got expected exception: " + e.getMessage());
            System.out.println("Test passed!");
        } catch (Exception e) {
            // This is not expected
            System.err.println("Test failed: Got unexpected exception: " + e.getClass().getName());
            e.printStackTrace();
        }
    }
    
    /**
     * Test custom parameters.
     */
    private static void testCustomParameters(ChatCompletionService service) {
        System.out.println("\nTesting custom parameters...");
        
        try {
            // Prepare test data
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("user", "Write a short poem about Java programming."));
            
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("max_completion_tokens", 100);
            
            // Call the method
            String result = service.getChatCompletion(messages, parameters);
            
            // Print the result
            System.out.println("Response with custom parameters: " + result);
            System.out.println("Test passed!");
        } catch (AIServiceException e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Test failed with unexpected exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}