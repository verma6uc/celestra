package com.celestra.email;

import java.io.File;
import java.util.List;

import com.celestra.email.exception.EmailException;

/**
 * Interface for email services.
 */
public interface EmailService {
    
    /**
     * Send a plain text email to a single recipient.
     * 
     * @param to The recipient email address
     * @param subject The email subject
     * @param body The email body (plain text)
     * @throws EmailException If an error occurs while sending the email
     */
    void sendPlainTextEmail(String to, String subject, String body) throws EmailException;
    
    /**
     * Send a plain text email to multiple recipients.
     * 
     * @param to The recipient email addresses
     * @param subject The email subject
     * @param body The email body (plain text)
     * @throws EmailException If an error occurs while sending the email
     */
    void sendPlainTextEmail(List<String> to, String subject, String body) throws EmailException;
    
    /**
     * Send a plain text email with CC and BCC recipients.
     * 
     * @param to The recipient email addresses
     * @param cc The CC recipient email addresses
     * @param bcc The BCC recipient email addresses
     * @param subject The email subject
     * @param body The email body (plain text)
     * @throws EmailException If an error occurs while sending the email
     */
    void sendPlainTextEmail(List<String> to, List<String> cc, List<String> bcc, String subject, String body) throws EmailException;
    
    /**
     * Send an HTML email to a single recipient.
     * 
     * @param to The recipient email address
     * @param subject The email subject
     * @param htmlBody The email body (HTML)
     * @throws EmailException If an error occurs while sending the email
     */
    void sendHtmlEmail(String to, String subject, String htmlBody) throws EmailException;
    
    /**
     * Send an HTML email to multiple recipients.
     * 
     * @param to The recipient email addresses
     * @param subject The email subject
     * @param htmlBody The email body (HTML)
     * @throws EmailException If an error occurs while sending the email
     */
    void sendHtmlEmail(List<String> to, String subject, String htmlBody) throws EmailException;
    
    /**
     * Send an HTML email with CC and BCC recipients.
     * 
     * @param to The recipient email addresses
     * @param cc The CC recipient email addresses
     * @param bcc The BCC recipient email addresses
     * @param subject The email subject
     * @param htmlBody The email body (HTML)
     * @throws EmailException If an error occurs while sending the email
     */
    void sendHtmlEmail(List<String> to, List<String> cc, List<String> bcc, String subject, String htmlBody) throws EmailException;
    
    /**
     * Send an email with attachments.
     * 
     * @param to The recipient email addresses
     * @param cc The CC recipient email addresses
     * @param bcc The BCC recipient email addresses
     * @param subject The email subject
     * @param body The email body (plain text)
     * @param htmlBody The email body (HTML)
     * @param attachments The email attachments
     * @throws EmailException If an error occurs while sending the email
     */
    void sendEmailWithAttachments(List<String> to, List<String> cc, List<String> bcc, String subject, 
            String body, String htmlBody, List<File> attachments) throws EmailException;
}