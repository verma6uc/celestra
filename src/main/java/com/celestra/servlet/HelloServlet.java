package com.celestra.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.celestra.util.JsonResponseUtil;
import com.google.gson.JsonObject;

/**
 * A simple servlet that demonstrates the basic functionality of the Celestra application.
 * This servlet responds to GET requests with a JSON greeting message.
 */
@WebServlet("/hello")
public class HelloServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    /**
     * Handles GET requests by returning a JSON greeting message.
     * 
     * @param request The HTTP request
     * @param response The HTTP response
     * @throws ServletException If a servlet-specific error occurs
     * @throws IOException If an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Create a JSON response
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("message", "Hello from Celestra!");
        jsonResponse.addProperty("status", "success");
        jsonResponse.addProperty("timestamp", System.currentTimeMillis());
        
        // Send the JSON response
        JsonResponseUtil.sendJsonResponse(response, jsonResponse);
    }
    
    /**
     * Handles POST requests by echoing back the received data as JSON.
     * 
     * @param request The HTTP request
     * @param response The HTTP response
     * @throws ServletException If a servlet-specific error occurs
     * @throws IOException If an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Create a JSON response
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("message", "POST request received by Celestra");
        jsonResponse.addProperty("status", "success");
        jsonResponse.addProperty("timestamp", System.currentTimeMillis());
        
        // Send the JSON response
        JsonResponseUtil.sendJsonResponse(response, jsonResponse);
    }
}