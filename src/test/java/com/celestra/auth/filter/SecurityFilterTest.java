package com.celestra.auth.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.celestra.auth.service.LoginService;
import com.celestra.enums.UserRole;
import com.celestra.enums.UserStatus;
import com.celestra.model.User;
import com.celestra.model.UserSession;
import com.celestra.util.ServletUtil;

/**
 * Test class for SecurityFilter.
 */
public class SecurityFilterTest {
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private FilterChain chain;
    
    @Mock
    private HttpSession session;
    
    @Mock
    private LoginService loginService;
    
    private SecurityFilter filter;
    
    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Mock ServletUtil to return our mock LoginService
        mockStatic(ServletUtil.class);
        when(ServletUtil.getLoginService()).thenReturn(loginService);
        
        filter = new SecurityFilter();
        filter.init(null);
        
        // Set up common request behavior
        when(request.getContextPath()).thenReturn("");
        when(request.getMethod()).thenReturn("GET");
    }
    
    @Test
    public void testDoFilter_PublicPath() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/login");
        
        // Act
        filter.doFilter(request, response, chain);
        
        // Assert
        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
        verify(response, never()).sendError(anyInt(), anyString());
    }
    
    @Test
    public void testDoFilter_PublicPathWithPrefix() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/assets/css/style.css");
        
        // Act
        filter.doFilter(request, response, chain);
        
        // Assert
        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
        verify(response, never()).sendError(anyInt(), anyString());
    }
    
    @Test
    public void testDoFilter_AuthenticatedWithSession() throws IOException, ServletException, SQLException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/dashboard");
        when(request.getSession(false)).thenReturn(session);
        
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        user.setStatus(UserStatus.ACTIVE);
        
        when(session.getAttribute("user")).thenReturn(user);
        when(session.getAttribute("sessionToken")).thenReturn("valid-token");
        
        UserSession userSession = new UserSession();
        userSession.setId(1);
        userSession.setUserId(1);
        userSession.setSessionToken("valid-token");
        userSession.setExpiresAt(Timestamp.from(Instant.now().plusSeconds(3600)));
        
        when(loginService.validateSession("valid-token")).thenReturn(Optional.of(userSession));
        
        // Act
        filter.doFilter(request, response, chain);
        
        // Assert
        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
        verify(response, never()).sendError(anyInt(), anyString());
    }
    
    @Test
    public void testDoFilter_AuthenticatedWithCookie() throws IOException, ServletException, SQLException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/dashboard");
        when(request.getSession(false)).thenReturn(null);
        
        Cookie[] cookies = new Cookie[] { new Cookie("CELESTRA_SESSION", "valid-token") };
        when(request.getCookies()).thenReturn(cookies);
        
        UserSession userSession = new UserSession();
        userSession.setId(1);
        userSession.setUserId(1);
        userSession.setSessionToken("valid-token");
        userSession.setExpiresAt(Timestamp.from(Instant.now().plusSeconds(3600)));
        
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        user.setStatus(UserStatus.ACTIVE);
        
        when(loginService.validateSession("valid-token")).thenReturn(Optional.of(userSession));
        when(loginService.getUserById(1)).thenReturn(Optional.of(user));
        
        when(request.getSession(true)).thenReturn(session);
        
        // Act
        filter.doFilter(request, response, chain);
        
        // Assert
        verify(session).setAttribute("user", user);
        verify(session).setAttribute("sessionToken", "valid-token");
        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
        verify(response, never()).sendError(anyInt(), anyString());
    }
    
    @Test
    public void testDoFilter_Unauthenticated() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/dashboard");
        when(request.getSession(false)).thenReturn(null);
        when(request.getCookies()).thenReturn(null);
        
        // Act
        filter.doFilter(request, response, chain);
        
        // Assert
        verify(chain, never()).doFilter(request, response);
        verify(response).sendRedirect("/login?redirect=/dashboard");
        verify(response, never()).sendError(anyInt(), anyString());
    }
    
    @Test
    public void testDoFilter_InvalidSession() throws IOException, ServletException, SQLException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/dashboard");
        when(request.getSession(false)).thenReturn(session);
        
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        user.setStatus(UserStatus.ACTIVE);
        
        when(session.getAttribute("user")).thenReturn(user);
        when(session.getAttribute("sessionToken")).thenReturn("invalid-token");
        
        when(loginService.validateSession("invalid-token")).thenReturn(Optional.empty());
        
        // Act
        filter.doFilter(request, response, chain);
        
        // Assert
        verify(chain, never()).doFilter(request, response);
        verify(session).invalidate();
        verify(response).sendRedirect("/login?redirect=/dashboard");
        verify(response, never()).sendError(anyInt(), anyString());
    }
    
    @Test
    public void testDoFilter_NoPermission() throws IOException, ServletException, SQLException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/admin/users");
        when(request.getSession(false)).thenReturn(session);
        
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(UserRole.REGULAR_USER); // Regular user, not admin
        
        when(session.getAttribute("user")).thenReturn(user);
        when(session.getAttribute("sessionToken")).thenReturn("valid-token");
        
        UserSession userSession = new UserSession();
        userSession.setId(1);
        userSession.setUserId(1);
        userSession.setSessionToken("valid-token");
        userSession.setExpiresAt(Timestamp.from(Instant.now().plusSeconds(3600)));
        
        when(loginService.validateSession("valid-token")).thenReturn(Optional.of(userSession));
        
        // Override the hasPermission method to return false for this test
        SecurityFilter testFilter = new SecurityFilter() {
            @Override
            protected boolean hasPermission(User user, String path, String method) {
                return false;
            }
        };
        testFilter.init(null);
        
        // Act
        testFilter.doFilter(request, response, chain);
        
        // Assert
        verify(chain, never()).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }
    
    /**
     * Mock the static methods in ServletUtil.
     * 
     * @param clazz The class to mock
     * @return The mock
     */
    private static <T> T mockStatic(Class<T> clazz) {
        return mock(clazz);
    }
}