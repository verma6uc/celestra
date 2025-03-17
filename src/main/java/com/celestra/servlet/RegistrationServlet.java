package com.celestra.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.celestra.auth.service.RegistrationService;
import com.celestra.auth.service.impl.RegistrationServiceImpl;
import com.celestra.enums.UserRole;
import com.celestra.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Servlet for handling user registration.
 */
@WebServlet("/api/register")
public class RegistrationServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private final RegistrationService registrationService;
    private final Gson gson;
    
    /**
     * Default constructor.
     */
    public RegistrationServlet() {
        this.registrationService = new RegistrationServiceImpl();
        this.gson = new Gson();
    }
    
    /**
     * Handle POST requests for user registration.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
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
            
            // Extract registration data
            String email = jsonObject.has("email") ? jsonObject.get("email").getAsString() : null;
            String name = jsonObject.has("name") ? jsonObject.get("name").getAsString() : null;
            String password = jsonObject.has("password") ? jsonObject.get("password").getAsString() : null;
            String roleStr = jsonObject.has("role") ? jsonObject.get("role").getAsString() : null;
            String companyIdStr = jsonObject.has("companyId") ? jsonObject.get("companyId").getAsString() : null;
            
            // Validate required fields
            if (email == null || name == null || password == null || roleStr == null) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing required fields");
                return;
            }
            
            // Parse role and company ID
            UserRole role;
            try {
                role = UserRole.valueOf(roleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid role: " + roleStr);
                return;
            }
            
            Integer companyId = null;
            if (companyIdStr != null && !companyIdStr.isEmpty()) {
                try {
                    companyId = Integer.parseInt(companyIdStr);
                } catch (NumberFormatException e) {
                    sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid company ID: " + companyIdStr);
                    return;
                }
            }
            
            // Get client IP address
            String ipAddress = request.getRemoteAddr();
            
            // Create metadata map
            Map<String, String> metadata = new HashMap<>();
            metadata.put("user_agent", request.getHeader("User-Agent"));
            metadata.put("registration_source", "web");
            
            // Register the user
            User user = registrationService.registerUser(email, name, password, role, companyId, ipAddress, metadata);
            
            // Send success response
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("success", true);
            responseJson.addProperty("message", "User registered successfully");
            responseJson.addProperty("userId", user.getId());
            responseJson.addProperty("email", user.getEmail());
            responseJson.addProperty("name", user.getName());
            responseJson.addProperty("role", user.getRole().toString());
            responseJson.addProperty("status", user.getStatus().toString());
            
            response.getWriter().write(gson.toJson(responseJson));
            
        } catch (IllegalArgumentException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SecurityException e) {
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (SQLException e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }
    
    /**
     * Handle GET requests for email verification.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if this is an email verification request
        String token = request.getParameter("token");
        
        if (token != null && !token.isEmpty()) {
            try {
                boolean verified = registrationService.verifyEmail(token);
                
                if (verified) {
                    // Redirect to success page
                    response.sendRedirect(request.getContextPath() + "/verification-success.html");
                } else {
                    // Redirect to failure page
                    response.sendRedirect(request.getContextPath() + "/verification-failed.html");
                }
                
            } catch (SQLException e) {
                // Redirect to error page
                response.sendRedirect(request.getContextPath() + "/error.html?message=" + e.getMessage());
            }
        } else {
            // Invalid request
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing verification token");
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