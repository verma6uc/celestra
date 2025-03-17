package com.celestra.ai.http;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 * Wrapper interface for HttpClient to make it easier to test code that uses HttpClient.
 */
public interface HttpClientWrapper {
    
    /**
     * A simple HTTP response class that contains only the data we need for testing.
     */
    class SimpleHttpResponse {
        private final int statusCode;
        private final String body;
        private final Map<String, List<String>> headers;
        
        /**
         * Create a new SimpleHttpResponse.
         * 
         * @param statusCode The HTTP status code
         * @param body The response body
         * @param headers The response headers
         */
        public SimpleHttpResponse(int statusCode, String body, Map<String, List<String>> headers) {
            this.statusCode = statusCode;
            this.body = body;
            this.headers = headers;
        }
        
        public int statusCode() {
            return statusCode;
        }
        
        public String body() {
            return body;
        }
        
        public Map<String, List<String>> headers() {
            return headers;
        }
    }
    
    /**
     * Send an HTTP request and return a simple HTTP response.
     * 
     * @param request The HTTP request to send
     */
    SimpleHttpResponse sendRequest(HttpRequest request) throws IOException, InterruptedException;
}