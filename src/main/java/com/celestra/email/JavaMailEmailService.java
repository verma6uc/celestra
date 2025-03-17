package com.celestra.email;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.celestra.email.exception.EmailException;

/**
 * Implementation of the EmailService interface using JavaMail.
 */
public class JavaMailEmailService implements EmailService {
    
    private static final Logger LOGGER = Logger.getLogger(JavaMailEmailService.class.getName());
    
    private final EmailConfigurationManager configManager;
    private final Session session;
    
    /**
     * Create a new JavaMail email service.
     */
    public JavaMailEmailService() {
        this.configManager = EmailConfigurationManager.getInstance();
        this.session = createSession();
    }
    
    /**
     * Create a JavaMail session.
     * 
     * @return The JavaMail session
     */
    private Session createSession() {
        Properties props = configManager.getJavaMailProperties();
        
        if (configManager.isSmtpAuthEnabled()) {
            return Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                            configManager.getSmtpUsername(), 
                            configManager.getSmtpPassword());
                }
            });
        } else {
            return Session.getInstance(props);
        }
    }
    
    @Override
    public void sendPlainTextEmail(String to, String subject, String body) throws EmailException {
        sendPlainTextEmail(List.of(to), subject, body);
    }
    
    @Override
    public void sendPlainTextEmail(List<String> to, String subject, String body) throws EmailException {
        sendPlainTextEmail(to, null, null, subject, body);
    }
    
    @Override
    public void sendPlainTextEmail(List<String> to, List<String> cc, List<String> bcc, String subject, String body) throws EmailException {
        sendEmail(to, cc, bcc, subject, body, null, null);
    }
    
    @Override
    public void sendHtmlEmail(String to, String subject, String htmlBody) throws EmailException {
        sendHtmlEmail(List.of(to), subject, htmlBody);
    }
    
    @Override
    public void sendHtmlEmail(List<String> to, String subject, String htmlBody) throws EmailException {
        sendHtmlEmail(to, null, null, subject, htmlBody);
    }
    
    @Override
    public void sendHtmlEmail(List<String> to, List<String> cc, List<String> bcc, String subject, String htmlBody) throws EmailException {
        sendEmail(to, cc, bcc, subject, null, htmlBody, null);
    }
    
    @Override
    public void sendEmailWithAttachments(List<String> to, List<String> cc, List<String> bcc, String subject, 
            String body, String htmlBody, List<File> attachments) throws EmailException {
        sendEmail(to, cc, bcc, subject, body, htmlBody, attachments);
    }
    
    /**
     * Send an email.
     * 
     * @param to The recipient email addresses
     * @param cc The CC recipient email addresses
     * @param bcc The BCC recipient email addresses
     * @param subject The email subject
     * @param plainTextBody The email body (plain text)
     * @param htmlBody The email body (HTML)
     * @param attachments The email attachments
     * @throws EmailException If an error occurs while sending the email
     */
    private void sendEmail(List<String> to, List<String> cc, List<String> bcc, String subject, 
            String plainTextBody, String htmlBody, List<File> attachments) throws EmailException {
        
        validateParameters(to, subject);
        
        try {
            MimeMessage message = createMimeMessage();
            
            // Set From
            message.setFrom(new InternetAddress(configManager.getFromAddress(), configManager.getFromName()));
            
            // Set To
            for (String recipient : to) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            }
            
            // Set CC
            if (cc != null && !cc.isEmpty()) {
                for (String recipient : cc) {
                    message.addRecipient(Message.RecipientType.CC, new InternetAddress(recipient));
                }
            }
            
            // Set BCC
            if (bcc != null && !bcc.isEmpty()) {
                for (String recipient : bcc) {
                    message.addRecipient(Message.RecipientType.BCC, new InternetAddress(recipient));
                }
            }
            
            // Set Subject
            message.setSubject(subject);
            
            // Set Content
            if (attachments != null && !attachments.isEmpty()) {
                // Email with attachments
                Multipart multipart = new MimeMultipart();
                
                // Add text part
                if (plainTextBody != null && htmlBody != null) {
                    // Both plain text and HTML
                    Multipart alternativePart = new MimeMultipart("alternative");
                    
                    MimeBodyPart textPart = new MimeBodyPart();
                    textPart.setText(plainTextBody, "utf-8");
                    alternativePart.addBodyPart(textPart);
                    
                    MimeBodyPart htmlPart = new MimeBodyPart();
                    htmlPart.setContent(htmlBody, "text/html; charset=utf-8");
                    alternativePart.addBodyPart(htmlPart);
                    
                    MimeBodyPart alternativeBodyPart = new MimeBodyPart();
                    alternativeBodyPart.setContent(alternativePart);
                    multipart.addBodyPart(alternativeBodyPart);
                } else if (htmlBody != null) {
                    // HTML only
                    MimeBodyPart htmlPart = new MimeBodyPart();
                    htmlPart.setContent(htmlBody, "text/html; charset=utf-8");
                    multipart.addBodyPart(htmlPart);
                } else {
                    // Plain text only
                    MimeBodyPart textPart = new MimeBodyPart();
                    textPart.setText(plainTextBody, "utf-8");
                    multipart.addBodyPart(textPart);
                }
                
                // Add attachments
                for (File file : attachments) {
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    DataSource source = new FileDataSource(file);
                    attachmentPart.setDataHandler(new DataHandler(source));
                    attachmentPart.setFileName(file.getName());
                    multipart.addBodyPart(attachmentPart);
                }
                
                message.setContent(multipart);
            } else if (plainTextBody != null && htmlBody != null) {
                // Both plain text and HTML
                Multipart multipart = new MimeMultipart("alternative");
                
                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setText(plainTextBody, "utf-8");
                multipart.addBodyPart(textPart);
                
                MimeBodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(htmlBody, "text/html; charset=utf-8");
                multipart.addBodyPart(htmlPart);
                
                message.setContent(multipart);
            } else if (htmlBody != null) {
                // HTML only
                message.setContent(htmlBody, "text/html; charset=utf-8");
            } else {
                // Plain text only
                message.setText(plainTextBody, "utf-8");
            }
            
            // Send the message
            sendWithRetry(message);
            
            LOGGER.info("Email sent successfully to " + String.join(", ", to));
        } catch (AddressException e) {
            LOGGER.log(Level.SEVERE, "Invalid email address", e);
            throw new EmailException("Invalid email address: " + e.getMessage(), e);
        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, "Error sending email", e);
            throw new EmailException("Error sending email: " + e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error sending email", e);
            throw new EmailException("Unexpected error sending email: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validate the email parameters.
     * 
     * @param to The recipient email addresses
     * @param subject The email subject
     * @throws EmailException If the parameters are invalid
     */
    private void validateParameters(List<String> to, String subject) throws EmailException {
        List<String> errors = new ArrayList<>();
        
        if (to == null || to.isEmpty()) {
            errors.add("Recipient list cannot be empty");
        }
        
        if (subject == null || subject.trim().isEmpty()) {
            errors.add("Subject cannot be empty");
        }
        
        if (!errors.isEmpty()) {
            throw new EmailException("Invalid email parameters: " + String.join(", ", errors));
        }
    }
    
    /**
     * Send an email with retry logic.
     * 
     * @param message The email message
     * @throws MessagingException If an error occurs while sending the email
     */
    private void sendWithRetry(MimeMessage message) throws MessagingException {
        int maxRetries = configManager.getRetryAttempts();
        int retryDelayMs = configManager.getRetryDelayMs();
        
        MessagingException lastException = null;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                if (attempt > 0) {
                    LOGGER.info("Retrying email send (attempt " + attempt + " of " + maxRetries + ")");
                    Thread.sleep(retryDelayMs * attempt); // Exponential backoff
                }
                
                Transport.send(message);
                return; // Success
            } catch (MessagingException e) {
                lastException = e;
                LOGGER.log(Level.WARNING, "Error sending email (attempt " + (attempt + 1) + " of " + (maxRetries + 1) + ")", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new MessagingException("Email sending interrupted", e);
            }
        }
        
        // If we've exhausted all retries, throw the last exception
        if (lastException != null) {
            throw new MessagingException("Failed to send email after " + maxRetries + " retries", lastException);
        }
    }
    
    /**
     * Create a new MimeMessage.
     * 
     * @return A new MimeMessage
     */
    protected MimeMessage createMimeMessage() {
        return new MimeMessage(session);
    }
}