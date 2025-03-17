package com.celestra.ai;

import java.util.List;
import java.util.Map;

/**
 * Interface for AI chat completion services.
 * Defines common methods for interacting with AI chat completion APIs.
 */
public interface ChatCompletionService {
    
    /**
     * Represents a message in a chat conversation.
     */
    class ChatMessage {
        private String role;
        private String content;
        
        /**
         * Create a new chat message.
         * 
         * @param role The role of the message sender (e.g., "system", "user", "assistant")
         * @param content The content of the message
         */
        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
        
        /**
         * Get the role of the message sender.
         * 
         * @return The role of the message sender
         */
        public String getRole() {
            return role;
        }
        
        /**
         * Set the role of the message sender.
         * 
         * @param role The role of the message sender
         */
        public void setRole(String role) {
            this.role = role;
        }
        
        /**
         * Get the content of the message.
         * 
         * @return The content of the message
         */
        public String getContent() {
            return content;
        }
        
        /**
         * Set the content of the message.
         * 
         * @param content The content of the message
         */
        public void setContent(String content) {
            this.content = content;
        }
    }
    
    /**
     * Create a system message.
     * 
     * @param content The content of the message
     * @return A new chat message with the "system" role
     */
    default ChatMessage systemMessage(String content) {
        return new ChatMessage("system", content);
    }
    
    /**
     * Create a user message.
     * 
     * @param content The content of the message
     * @return A new chat message with the "user" role
     */
    default ChatMessage userMessage(String content) {
        return new ChatMessage("user", content);
    }
    
    /**
     * Create an assistant message.
     * 
     * @param content The content of the message
     * @return A new chat message with the "assistant" role
     */
    default ChatMessage assistantMessage(String content) {
        return new ChatMessage("assistant", content);
    }
    
    /**
     * Get a chat completion for a list of messages.
     * 
     * @param messages The list of messages in the conversation
     * @return The AI-generated response
     * @throws Exception If an error occurs during the API call
     */
    String getChatCompletion(List<ChatMessage> messages) throws Exception;
    
    /**
     * Get a chat completion for a list of messages with custom parameters.
     * 
     * @param messages The list of messages in the conversation
     * @param parameters Custom parameters for the API call (e.g., temperature, max_tokens)
     * @return The AI-generated response
     * @throws Exception If an error occurs during the API call
     */
    String getChatCompletion(List<ChatMessage> messages, Map<String, Object> parameters) throws Exception;
    
    /**
     * Get a simple chat completion for a single user message.
     * 
     * @param userMessage The user message
     * @return The AI-generated response
     * @throws Exception If an error occurs during the API call
     */
    default String getChatCompletion(String userMessage) throws Exception {
        return getChatCompletion(List.of(userMessage(userMessage)));
    }
    
    /**
     * Get a simple chat completion for a system message and a user message.
     * 
     * @param systemMessage The system message
     * @param userMessage The user message
     * @return The AI-generated response
     * @throws Exception If an error occurs during the API call
     */
    default String getChatCompletion(String systemMessage, String userMessage) throws Exception {
        return getChatCompletion(List.of(systemMessage(systemMessage), userMessage(userMessage)));
    }
}