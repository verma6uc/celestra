package com.celestra.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.celestra.auth.service.ForgotPasswordService;
import com.celestra.auth.service.impl.ForgotPasswordServiceImpl;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Servlet for handling forgot password requests.
 */
@WebServlet("/api/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private final ForgotPasswordService forgotPasswordService;
    private final Gson gson;
    
    /**
     * Default constructor.
     */
    public ForgotPasswordServlet() {
        this.forgotPasswordService = new ForgotPasswordServiceImpl();
        this.gson = new Gson();
    }
    
    /**
     * Handle POST requests for initiating password reset.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        
        try {
            // Parse request body
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            // Convert JSON to map
            JsonObject jsonObject = gson.fromJson(sb.toString(), JsonObject.class);
            
            // Extract email
            String email = jsonObject.has("email") ? jsonObject.get("email").getAsString() : null;
            
            // Validate required fields
            if (email == null) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Email is required");
                return;
            }
            
            // Get client IP address
            String ipAddress = request.getRemoteAddr();
            
            // Create metadata map
            Map<String, String> metadata = new HashMap<>();
            metadata.put("user_agent", request.getHeader("User-Agent"));
            metadata.put("request_source", "web");
            
            // Initiate password reset
            boolean success = forgotPasswordService.initiatePasswordReset(email, ipAddress, metadata);
            
            // Always return success to prevent email enumeration
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("success", true);
            responseJson.addProperty("message", "If your email is registered, you will receive a password reset link shortly.");
            
            response.getWriter().write(gson.toJson(responseJson));
            
        } catch (SQLException e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }
    
    /**
     * Handle GET requests for validating reset tokens.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        
        try {
            // Get token from request parameter
            String token = request.getParameter("token");
            
            if (token == null || token.isEmpty()) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Token is required");
                return;
            }
            
            // Validate token
            boolean isValid = forgotPasswordService.validateResetToken(token);
            
            if (isValid) {
                // Get email associated with token
                String email = forgotPasswordService.getEmailFromToken(token);
                
                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("success", true);
                responseJson.addProperty("message", "Token is valid");
                responseJson.addProperty("email", email);
                
                response.getWriter().write(gson.toJson(responseJson));
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid or expired token");
            }
            
        } catch (SQLException e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }
    
    /**
     * Handle PUT requests for resetting passwords.
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        
        try {
            // Parse request body
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            // Convert JSON to map
            JsonObject jsonObject = gson.fromJson(sb.toString(), JsonObject.class);
            
            // Extract data
            String token = jsonObject.has("token") ? jsonObject.get("token").getAsString() : null;
            String newPassword = jsonObject.has("newPassword") ? jsonObject.get("newPassword").getAsString() : null;
            
            // Validate required fields
            if (token == null || newPassword == null) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Token and new password are required");
                return;
            }
            
            // Get client IP address
            String ipAddress = request.getRemoteAddr();
            
            // Create metadata map
            Map<String, String> metadata = new HashMap<>();
            metadata.put("user_agent", request.getHeader("User-Agent"));
            metadata.put("request_source", "web");
            
            // Reset password
            boolean success = forgotPasswordService.resetPassword(token, newPassword, ipAddress, metadata);
            
            if (success) {
                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("success", true);
                responseJson.addProperty("message", "Password has been reset successfully");
                
                response.getWriter().write(gson.toJson(responseJson));
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Failed to reset password");
            }
            
        } catch (SQLException e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }
    
    /**
     * Send an error response to the client.
     * 
     * @param response The HTTP response
     * @param status The HTTP status code
     * @param message The error message
     * @throws IOException If an I/O error occurs
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        
        JsonObject errorJson = new JsonObject();
        errorJson.addProperty("success", false);
        errorJson.addProperty("error", message);
        
        response.getWriter().write(gson.toJson(errorJson));
    }
}