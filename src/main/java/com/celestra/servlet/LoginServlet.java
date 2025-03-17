package com.celestra.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.celestra.auth.service.LoginService;
import com.celestra.dao.UserDao;
import com.celestra.auth.service.impl.LoginServiceImpl;
import com.celestra.model.User;
import com.celestra.model.UserSession;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Servlet for handling user login and authentication.
 */
@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private final UserDao userDao;
    private final LoginService loginService;
    private final Gson gson;
    
    /**
     * Default constructor.
     */
    public LoginServlet() {
        this.loginService = new LoginServiceImpl();
        this.userDao = new com.celestra.dao.impl.UserDaoImpl();
        this.gson = new Gson();
    }
    
    /**
     * Handle POST requests for user login.
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
            
            // Extract login data
            String email = jsonObject.has("email") ? jsonObject.get("email").getAsString() : null;
            String password = jsonObject.has("password") ? jsonObject.get("password").getAsString() : null;
            
            // Validate required fields
            if (email == null || password == null) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Email and password are required");
                return;
            }
            
            // Get client IP address
            String ipAddress = request.getRemoteAddr();
            
            // Create metadata map
            Map<String, String> metadata = new HashMap<>();
            metadata.put("user_agent", request.getHeader("User-Agent"));
            metadata.put("login_source", "web");
            
            // Authenticate the user
            Optional<User> userOpt = loginService.authenticate(email, password, ipAddress, metadata);
            
            if (!userOpt.isPresent()) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid email or password");
                return;
            }
            
            User user = userOpt.get();
            
            // Create a session for the authenticated user
            UserSession session = loginService.createSession(
                user.getId(), 
                ipAddress, 
                request.getHeader("User-Agent"), 
                metadata
            );
            
            // No cookies are set - the client will manage the session token
            
            // Send success response
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("success", true);
            responseJson.addProperty("message", "Login successful");
            responseJson.addProperty("userId", user.getId());
            responseJson.addProperty("email", user.getEmail());
            responseJson.addProperty("name", user.getName());
            responseJson.addProperty("role", user.getRole().toString());
            responseJson.addProperty("sessionToken", session.getSessionToken());
            
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
     * Handle GET requests for session validation.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        
        try {
            // Check if this is a session validation request
            String sessionToken = null;
            
            // Get token from the request parameter
            sessionToken = request.getParameter("token");
            
            if (sessionToken != null && !sessionToken.isEmpty()) {
                // Validate the session
                Optional<UserSession> sessionOpt = loginService.validateSession(sessionToken);
                
                if (sessionOpt.isPresent()) {
                    UserSession session = sessionOpt.get();
                    
                    // Get the user associated with the session
                    Optional<User> userOpt = userDao.findById(session.getUserId());
                    
                    if (userOpt.isPresent()) {
                        User user = userOpt.get(); 
                        
                        // Send success response
                        JsonObject responseJson = new JsonObject();
                        responseJson.addProperty("success", true);
                        responseJson.addProperty("message", "Session is valid");
                        responseJson.addProperty("userId", user.getId());
                        responseJson.addProperty("email", user.getEmail());
                        responseJson.addProperty("name", user.getName());
                        responseJson.addProperty("role", user.getRole().toString());
                        
                        response.getWriter().write(gson.toJson(responseJson));
                        return;
                    }
                }
                
                // Session is invalid
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired session");
            } else {
                // No session token provided
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "No session token provided");
            }
            
        } catch (SQLException e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }
    
    /**
     * Handle DELETE requests for logout.
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        
        try {
            // Get the session token
            String sessionToken = null;
            
            // Get token from the request parameter
            sessionToken = request.getParameter("token");
            
            if (sessionToken != null && !sessionToken.isEmpty()) {
                // End the session
                boolean success = loginService.endSession(sessionToken, "User logout");
                
                // Send success response
                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("success", success);
                responseJson.addProperty("message", success ? "Logout successful" : "Session not found");
                
                response.getWriter().write(gson.toJson(responseJson));
            } else {
                // No session token provided
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "No session token provided");
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