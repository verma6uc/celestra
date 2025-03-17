package com.celestra.util;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * Utility class for handling JSON responses in servlets.
 * Provides methods to send JSON data as HTTP responses.
 */
public class JsonResponseUtil {
    
    private static final Gson gson = new Gson();
    
    /**
     * Sends a JSON response to the client.
     * 
     * @param response The HTTP response object
     * @param jsonElement The JSON element to send
     * @throws IOException If an I/O error occurs
     */
    public static void sendJsonResponse(HttpServletResponse response, JsonElement jsonElement) 
            throws IOException {
        // Set content type and character encoding
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Write the JSON response
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(jsonElement));
            out.flush();
        }
    }
    
    /**
     * Sends a Java object as a JSON response to the client.
     * The object will be converted to JSON using Gson.
     * 
     * @param response The HTTP response object
     * @param object The object to convert to JSON and send
     * @throws IOException If an I/O error occurs
     */
    public static void sendObjectAsJson(HttpServletResponse response, Object object) 
            throws IOException {
        // Set content type and character encoding
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Write the JSON response
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(object));
            out.flush();
        }
    }
}