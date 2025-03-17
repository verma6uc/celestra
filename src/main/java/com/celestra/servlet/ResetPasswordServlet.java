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
import com.celestra.auth.util.PasswordUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Servlet for handling password reset requests.
 */
@WebServlet("/api/reset-password")
public class ResetPasswordServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private final ForgotPasswordService forgotPasswordService;
    private final Gson gson;
    
    /**
     * Default constructor.
     */
    public ResetPasswordServlet() {
        this.forgotPasswordService = new ForgotPasswordServiceImpl();
        this.gson = new Gson();
    }
    
    /**
     * Handle GET requests for validating reset tokens and displaying the reset form.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        // Get token from request parameter
        String token = request.getParameter("token");
        
        if (token == null || token.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/forgot-password.html?error=missing_token");
            return;
        }
        
        try {
            // Validate token
            boolean isValid = forgotPasswordService.validateResetToken(token);
            
            if (isValid) {
                // Forward to reset password form
                request.setAttribute("token", token);
                request.getRequestDispatcher("/WEB-INF/views/reset-password.jsp").forward(request, response);
            } else {
                // Redirect to error page
                response.sendRedirect(request.getContextPath() + "/forgot-password.html?error=invalid_token");
            }
        } catch (Exception e) {
            // Redirect to error page
            response.sendRedirect(request.getContextPath() + "/error.html?message=" + e.getMessage());
        }
    }
    
    /**
     * Handle POST requests for resetting passwords.
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
            
            // Extract data
            String token = jsonObject.has("token") ? jsonObject.get("token").getAsString() : null;
            String newPassword = jsonObject.has("newPassword") ? jsonObject.get("newPassword").getAsString() : null;
            String confirmPassword = jsonObject.has("confirmPassword") ? jsonObject.get("confirmPassword").getAsString() : null;
            
            // Validate required fields
            if (token == null || newPassword == null || confirmPassword == null) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "All fields are required");
                return;
            }
            
            // Validate passwords match
            if (!newPassword.equals(confirmPassword)) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Passwords do not match");
                return;
            }
            
            // Validate password complexity
            if (!PasswordUtil.isPasswordValid(newPassword)) {
                Map<String, Boolean> validationResults = PasswordUtil.validatePassword(newPassword);
                StringBuilder errorMessage = new StringBuilder("Password does not meet complexity requirements: ");
                
                if (!validationResults.get("length")) {
                    errorMessage.append("length, ");
                }
                if (!validationResults.get("uppercase")) {
                    errorMessage.append("uppercase, ");
                }
                if (!validationResults.get("lowercase")) {
                    errorMessage.append("lowercase, ");
                }
                if (!validationResults.get("digit")) {
                    errorMessage.append("digit, ");
                }
                if (!validationResults.get("special")) {
                    errorMessage.append("special character, ");
                }
                
                // Remove trailing comma and space
                String message = errorMessage.toString().trim();
                if (message.endsWith(",")) {
                    message = message.substring(0, message.length() - 1);
                }
                
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, message);
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
                responseJson.addProperty("redirectUrl", request.getContextPath() + "/login.html?message=password_reset_success");
                
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