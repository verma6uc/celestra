package com.celestra.ai.http;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of HttpClientWrapper that uses the real HttpClient.
 */
public class DefaultHttpClientWrapper implements HttpClientWrapper {
    
    private final HttpClient httpClient;
    
    /**
     * Create a new DefaultHttpClientWrapper with the specified timeout.
     * 
     * @param timeoutSeconds The timeout in seconds
     */
    public DefaultHttpClientWrapper(int timeoutSeconds) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }
    
    /**
     * Create a new DefaultHttpClientWrapper with a default timeout of 60 seconds.
     */
    public DefaultHttpClientWrapper() {
        this(60);
    }
    
    @Override
    public SimpleHttpResponse sendRequest(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Convert HttpResponse to SimpleHttpResponse
        Map<String, List<String>> headers = new HashMap<>();
        response.headers().map().forEach((key, values) -> {
            headers.put(key, values);
        });
        
        return new SimpleHttpResponse(response.statusCode(), response.body(), headers);
    }
}