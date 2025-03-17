package com.celestra.util;

import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.celestra.auth.config.AuthConfigProvider;
import com.celestra.auth.config.AuthConfigurationManager;
import com.celestra.auth.service.AuditService;
import com.celestra.auth.service.InvitationAcceptanceService;
import com.celestra.auth.service.InvitationService;
import com.celestra.auth.service.LoginService;
import com.celestra.auth.service.impl.AuditServiceImpl;
import com.celestra.auth.service.impl.InvitationAcceptanceServiceImpl;
import com.celestra.auth.service.impl.InvitationServiceImpl;
import com.celestra.auth.service.impl.LoginServiceImpl;
import com.celestra.dao.AuditLogDao;
import com.celestra.dao.CompanyDao;
import com.celestra.dao.FailedLoginDao;
import com.celestra.dao.InvitationDao;
import com.celestra.dao.UserDao;
import com.celestra.dao.UserLockoutDao;
import com.celestra.dao.UserSessionDao;
import com.celestra.dao.impl.AuditLogDaoImpl;
import com.celestra.dao.impl.CompanyDaoImpl;
import com.celestra.dao.impl.FailedLoginDaoImpl;
import com.celestra.dao.impl.InvitationDaoImpl;
import com.celestra.dao.impl.UserDaoImpl;
import com.celestra.dao.impl.UserLockoutDaoImpl;
import com.celestra.dao.impl.UserSessionDaoImpl;
import com.celestra.email.EmailService;
import com.celestra.email.JavaMailEmailService;

/**
 * Utility class for servlet-related operations.
 * Provides methods for getting service instances and other common servlet operations.
 */
public class ServletUtil {
    
    private static final Logger LOGGER = Logger.getLogger(ServletUtil.class.getName());
    
    // Service instances
    private static LoginService loginService;
    private static AuditService auditService;
    private static InvitationService invitationService;
    private static InvitationAcceptanceService invitationAcceptanceService;
    private static EmailService emailService;
    
    // DAO instances
    private static UserDao userDao;
    private static UserSessionDao userSessionDao;
    private static FailedLoginDao failedLoginDao;
    private static UserLockoutDao userLockoutDao;
    private static CompanyDao companyDao;
    private static AuditLogDao auditLogDao;
    private static InvitationDao invitationDao;
    
    // Config instances
    private static AuthConfigProvider authConfig;
    
    /**
     * Private constructor to prevent instantiation.
     */
    private ServletUtil() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Gets the client's IP address from the request.
     * 
     * @param request The HTTP request
     * @return The client's IP address
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
    
    /**
     * Gets the user agent from the request.
     * 
     * @param request The HTTP request
     * @return The user agent
     */
    public static String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
    
    /**
     * Gets the context path from the request.
     * 
     * @param request The HTTP request
     * @return The context path
     */
    public static String getContextPath(HttpServletRequest request) {
        return request.getContextPath();
    }
    
    /**
     * Gets the servlet context from the request.
     * 
     * @param request The HTTP request
     * @return The servlet context
     */
    public static ServletContext getServletContext(HttpServletRequest request) {
        return request.getServletContext();
    }
    
    /**
     * Gets the LoginService instance.
     * 
     * @return The LoginService instance
     */
    public static synchronized LoginService getLoginService() {
        if (loginService == null) {
            loginService = new LoginServiceImpl(
                    getUserDao(),
                    getUserSessionDao(),
                    getFailedLoginDao(),
                    getUserLockoutDao(),
                    getCompanyDao(),
                    getAuditLogDao(),
                    getAuditService(),
                    getAuthConfig());
        }
        return loginService;
    }
    
    /**
     * Gets the AuditService instance.
     * 
     * @return The AuditService instance
     */
    public static synchronized AuditService getAuditService() {
        if (auditService == null) {
            auditService = new AuditServiceImpl(getAuditLogDao());
        }
        return auditService;
    }
    
    /**
     * Gets the InvitationService instance.
     * 
     * @return The InvitationService instance
     */
    public static synchronized InvitationService getInvitationService() {
        if (invitationService == null) {
            invitationService = new InvitationServiceImpl(
                    getInvitationDao(),
                    getUserDao(),
                    getEmailService(),
                    getAuditService(),
                    getAuthConfig());
        }
        return invitationService;
    }
    
    /**
     * Gets the InvitationAcceptanceService instance.
     * 
     * @return The InvitationAcceptanceService instance
     */
    public static synchronized InvitationAcceptanceService getInvitationAcceptanceService() {
        if (invitationAcceptanceService == null) {
            invitationAcceptanceService = new InvitationAcceptanceServiceImpl(
                    getInvitationService(),
                    getUserDao(),
                    getAuditService(),
                    getEmailService(),
                    getAuthConfig());
        }
        return invitationAcceptanceService;
    }
    
    /**
     * Gets the EmailService instance.
     * 
     * @return The EmailService instance
     */
    public static synchronized EmailService getEmailService() {
        if (emailService == null) {
            emailService = new JavaMailEmailService();
        }
        return emailService;
    }
    
    /**
     * Gets the UserDao instance.
     * 
     * @return The UserDao instance
     */
    public static synchronized UserDao getUserDao() {
        if (userDao == null) {
            userDao = new UserDaoImpl();
        }
        return userDao;
    }
    
    /**
     * Gets the UserSessionDao instance.
     * 
     * @return The UserSessionDao instance
     */
    public static synchronized UserSessionDao getUserSessionDao() {
        if (userSessionDao == null) {
            userSessionDao = new UserSessionDaoImpl();
        }
        return userSessionDao;
    }
    
    /**
     * Gets the FailedLoginDao instance.
     * 
     * @return The FailedLoginDao instance
     */
    public static synchronized FailedLoginDao getFailedLoginDao() {
        if (failedLoginDao == null) {
            failedLoginDao = new FailedLoginDaoImpl();
        }
        return failedLoginDao;
    }
    
    /**
     * Gets the UserLockoutDao instance.
     * 
     * @return The UserLockoutDao instance
     */
    public static synchronized UserLockoutDao getUserLockoutDao() {
        if (userLockoutDao == null) {
            userLockoutDao = new UserLockoutDaoImpl();
        }
        return userLockoutDao;
    }
    
    /**
     * Gets the CompanyDao instance.
     * 
     * @return The CompanyDao instance
     */
    public static synchronized CompanyDao getCompanyDao() {
        if (companyDao == null) {
            companyDao = new CompanyDaoImpl();
        }
        return companyDao;
    }
    
    /**
     * Gets the AuditLogDao instance.
     * 
     * @return The AuditLogDao instance
     */
    public static synchronized AuditLogDao getAuditLogDao() {
        if (auditLogDao == null) {
            auditLogDao = new AuditLogDaoImpl();
        }
        return auditLogDao;
    }
    
    /**
     * Gets the InvitationDao instance.
     * 
     * @return The InvitationDao instance
     */
    public static synchronized InvitationDao getInvitationDao() {
        if (invitationDao == null) {
            invitationDao = new InvitationDaoImpl();
        }
        return invitationDao;
    }
    
    /**
     * Gets the AuthConfigProvider instance.
     * 
     * @return The AuthConfigProvider instance
     */
    public static synchronized AuthConfigProvider getAuthConfig() {
        if (authConfig == null) {
            authConfig = AuthConfigurationManager.getInstance();
        }
        return authConfig;
    }
}