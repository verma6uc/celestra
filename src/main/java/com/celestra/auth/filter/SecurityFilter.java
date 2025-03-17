package com.celestra.auth.filter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.celestra.auth.service.LoginService;
import com.celestra.auth.service.impl.LoginServiceImpl;
import com.celestra.model.User;
import com.celestra.model.UserSession;
import com.celestra.util.ServletUtil;

/**
 * Security filter that intercepts all requests to validate authentication and authorization.
 * This filter:
 * - Validates active sessions
 * - Checks user permissions against requested resources
 * - Handles authentication failures
 * - Redirects to login when needed
 * - Provides security context to application components
 */
@WebFilter(urlPatterns = "/*")
public class SecurityFilter implements Filter {
    
    private static final Logger LOGGER = Logger.getLogger(SecurityFilter.class.getName());
    
    // Paths that don't require authentication
    private static final Set<String> PUBLIC_PATHS = new HashSet<>(Arrays.asList(
            "/login",
            "/logout",
            "/register",
            "/forgot-password",
            "/reset-password",
            "/accept-invitation",
            "/assets/",
            "/css/",
            "/js/",
            "/images/",
            "/favicon.ico",
            "/error"
    ));
    
    // Session attribute names
    private static final String USER_ATTRIBUTE = "user";
    private static final String SESSION_TOKEN_ATTRIBUTE = "sessionToken";
    
    // Cookie names
    private static final String SESSION_COOKIE_NAME = "CELESTRA_SESSION";
    
    private LoginService loginService;
    
    /**
     * Default constructor.
     */
    public SecurityFilter() {
        // Default constructor
    }
    
    /**
     * Constructor with LoginService for testing.
     */
    public SecurityFilter(LoginService loginService) {
        this.loginService = loginService;
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (this.loginService == null) {
            this.loginService = ServletUtil.getLoginService();
        }
        
        LOGGER.info("SecurityFilter initialized");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        
        // Check if the path is public
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Check if user is authenticated
        if (!isAuthenticated(httpRequest)) {
            // User is not authenticated, redirect to login page
            LOGGER.log(Level.INFO, "isAuthenticated returned false for path: {0}", path);
            LOGGER.log(Level.INFO, "Unauthenticated access attempt to {0}, redirecting to login", path);
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login?redirect=" + path);
            return;
        }
        
        // User is authenticated, check if session is valid
        try {
            if (!validateSession(httpRequest)) {
                // Session is invalid, redirect to login page
                LOGGER.log(Level.INFO, "validateSession returned false for path: {0}", path);
                LOGGER.log(Level.INFO, "Invalid session for access to {0}, redirecting to login", path);
                invalidateSession(httpRequest, httpResponse);
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login?redirect=" + path);
                return;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error validating session", e);
            httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error validating session");
            return;
        }
        
        // Check if user has permission to access the resource
        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            LOGGER.log(Level.INFO, "Session is null for path: {0}", path);
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login?redirect=" + path);
            return;
        }
        User user = (User) session.getAttribute(USER_ATTRIBUTE);
        if (!hasPermission(user, path, httpRequest.getMethod())) {
            // User doesn't have permission, return 403 Forbidden
            LOGGER.log(Level.INFO, "Access denied for user {0} to {1}", new Object[]{user.getId(), path});
            LOGGER.log(Level.INFO, "hasPermission returned false for user {0} and path: {1}", new Object[]{user.getId(), path});
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }
        
        // All checks passed, continue with the request
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {
        LOGGER.info("SecurityFilter destroyed");
    }
    
    /**
     * Checks if the path is public (doesn't require authentication).
     * 
     * @param path The request path
     * @return true if the path is public, false otherwise
     */
    private boolean isPublicPath(String path) {
        // Check exact matches
        if (PUBLIC_PATHS.contains(path)) {
            return true;
        }
        
        // Check path prefixes
        for (String publicPath : PUBLIC_PATHS) {
            if (publicPath.endsWith("/") && path.startsWith(publicPath)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Checks if the user is authenticated.
     * 
     * @param request The HTTP request
     * @return true if the user is authenticated, false otherwise
     */
    private boolean isAuthenticated(HttpServletRequest request) {
        // Check if user is in session
        LOGGER.log(Level.INFO, "Checking if user is authenticated");
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(USER_ATTRIBUTE) != null) {
            return true;
        }
        
        // Check if session token is in cookie
        String sessionToken = getSessionTokenFromCookie(request);
        if (sessionToken != null) {
            LOGGER.log(Level.INFO, "Found session token in cookie: {0}", sessionToken);
            try {
                // Validate session token
                Optional<UserSession> userSession = loginService.validateSession(sessionToken);
                if (userSession.isPresent()) {
                    // Session is valid, get user and store in session
                    Optional<User> user = loginService.getUserById(userSession.get().getUserId());
                    if (user.isPresent()) {
                        LOGGER.log(Level.INFO, "User found for session token: {0}", user.get().getId());
                        // Create session if it doesn't exist
                        session = request.getSession(true);
                        session.setAttribute(USER_ATTRIBUTE, user.get());
                        LOGGER.log(Level.INFO, "Set user attribute in session: {0}", user.get().getId());
                        session.setAttribute(SESSION_TOKEN_ATTRIBUTE, sessionToken);
                        return true;
                    }
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error validating session token", e);
            }
        }
        
        return false;
    }
    
    /**
     * Validates the user's session.
     * 
     * @param request The HTTP request
     * @return true if the session is valid, false otherwise
     * @throws SQLException if a database error occurs
     */
    private boolean validateSession(HttpServletRequest request) throws SQLException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        
        String sessionToken = (String) session.getAttribute(SESSION_TOKEN_ATTRIBUTE);
        if (sessionToken == null) {
            return false;
        }
        
        // Validate session token
        Optional<UserSession> userSession = loginService.validateSession(sessionToken);
        return userSession.isPresent();
    }
    
    /**
     * Invalidates the user's session.
     * 
     * @param request The HTTP request
     * @param response The HTTP response
     */
    private void invalidateSession(HttpServletRequest request, HttpServletResponse response) {
        // Invalidate HTTP session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        // Clear session cookie
        Cookie cookie = new Cookie(SESSION_COOKIE_NAME, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
    
    /**
     * Gets the session token from the request cookies.
     * 
     * @param request The HTTP request
     * @return The session token, or null if not found
     */
    private String getSessionTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (SESSION_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
    
    /**
     * Checks if the user has permission to access the resource.
     * 
     * @param user The user
     * @param path The request path
     * @param method The HTTP method
     * @return true if the user has permission, false otherwise
     */
    protected boolean hasPermission(User user, String path, String method) {
        // In a real application, this would check the user's role and permissions
        // For now, we'll just return true for all authenticated users
        return true;
    }
}