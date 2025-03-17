package com.celestra.email;

import java.io.File;
import java.util.List;

import com.celestra.email.exception.EmailException;

/**
 * Simple test class for JavaMailEmailService.
 * This class contains a main method to test the service functionality.
 */
public class JavaMailEmailServiceTest {
    
    public static void main(String[] args) {
        System.out.println("Testing JavaMailEmailService...");
        
        // Create the service
        JavaMailEmailService emailService = new JavaMailEmailService();
        
        // Always run validation tests
        testValidation(emailService);
        
        // Since we have valid API keys, we can run these tests
        // Note: Update the email addresses in these tests before running
        testSendPlainTextEmail(emailService);
        testSendHtmlEmail(emailService);
        // testSendEmailWithCcAndBcc(emailService);
        // testSendEmailWithAttachments(emailService);
        
        System.out.println("All tests completed.");
    }
    
    /**
     * Test sending a plain text email to a single recipient.
     */
    private static void testSendPlainTextEmail(EmailService emailService) {
        System.out.println("\nTesting sending plain text email...");
        
        try {
            // Prepare test data
            String to = "nupur.bhaisare@leucinetech.com"; // Replace with a valid email address if needed
            String subject = "Test Email";
            String body = "This is a test email.";
            
            // Call the method
            emailService.sendPlainTextEmail(to, subject, body);
            
            System.out.println("Email sent successfully!");
            System.out.println("Test passed!");
        } catch (EmailException e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test sending an HTML email to multiple recipients.
     */
    private static void testSendHtmlEmail(EmailService emailService) {
        System.out.println("\nTesting sending HTML email...");
        
        try {
            // Prepare test data
            List<String> to = List.of("nupur.bhaisare@leucinetech.com"); // Replace with valid email addresses if needed
            String subject = "Test HTML Email";
            String htmlBody = "<html><body><h1>Test</h1><p>This is a test HTML email.</p></body></html>";
            
            // Call the method
            emailService.sendHtmlEmail(to, subject, htmlBody);
            
            System.out.println("HTML email sent successfully!");
            System.out.println("Test passed!");
        } catch (EmailException e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test sending an email with CC and BCC recipients.
     */
    private static void testSendEmailWithCcAndBcc(EmailService emailService) {
        System.out.println("\nTesting sending email with CC and BCC...");
        
        try {
            // Prepare test data
            List<String> to = List.of("nupur.bhaisare@leucinetech.com"); // Replace with a valid email address if needed
            List<String> cc = List.of("nupur.bhaisare@leucinetech.com"); // Replace with a valid email address if needed
            List<String> bcc = List.of("nupur.bhaisare@leucinetech.com"); // Replace with a valid email address if needed
            String subject = "Test Email with CC and BCC";
            String body = "This is a test email with CC and BCC recipients.";
            
            // Call the method
            emailService.sendPlainTextEmail(to, cc, bcc, subject, body);
            
            System.out.println("Email with CC and BCC sent successfully!");
            System.out.println("Test passed!");
        } catch (EmailException e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test sending an email with attachments.
     */
    private static void testSendEmailWithAttachments(EmailService emailService) {
        System.out.println("\nTesting sending email with attachments...");
        
        try {
            // Prepare test data
            List<String> to = List.of("nupur.bhaisare@leucinetech.com"); // Replace with a valid email address if needed
            String subject = "Test Email with Attachments";
            String body = "This is a test email with attachments.";
            String htmlBody = "<html><body><h1>Test</h1><p>This is a test email with attachments.</p></body></html>";
            File attachment = new File("pom.xml"); // Use an existing file
            
            // Call the method
            emailService.sendEmailWithAttachments(to, null, null, subject, body, htmlBody, List.of(attachment));
            
            System.out.println("Email with attachments sent successfully!");
            System.out.println("Test passed!");
        } catch (EmailException e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test validation of email parameters.
     */
    private static void testValidation(EmailService emailService) {
        System.out.println("\nTesting validation...");
        
        // Test with empty recipient list
        try {
            emailService.sendPlainTextEmail(List.of(), "Subject", "Body");
            System.err.println("Test failed: Expected EmailException for empty recipient list");
        } catch (EmailException e) {
            if (e.getMessage().contains("Recipient list cannot be empty")) {
                System.out.println("Got expected exception for empty recipient list: " + e.getMessage());
            } else {
                System.err.println("Test failed: Got unexpected exception message: " + e.getMessage());
            }
        }
        
        // Test with null subject
        try {
            emailService.sendPlainTextEmail("test@example.com", null, "Body");
            System.err.println("Test failed: Expected EmailException for null subject");
        } catch (EmailException e) {
            if (e.getMessage().contains("Subject cannot be empty")) {
                System.out.println("Got expected exception for null subject: " + e.getMessage());
            } else {
                System.err.println("Test failed: Got unexpected exception message: " + e.getMessage());
            }
        }
        
        // Test with empty subject
        try {
            emailService.sendPlainTextEmail("test@example.com", "", "Body");
            System.err.println("Test failed: Expected EmailException for empty subject");
        } catch (EmailException e) {
            if (e.getMessage().contains("Subject cannot be empty")) {
                System.out.println("Got expected exception for empty subject: " + e.getMessage());
                System.out.println("Test passed!");
            } else {
                System.err.println("Test failed: Got unexpected exception message: " + e.getMessage());
            }
        }
    }
}