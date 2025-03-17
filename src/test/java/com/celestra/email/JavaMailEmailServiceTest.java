package com.celestra.email;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import com.celestra.email.exception.EmailException;

/**
 * Test class for JavaMailEmailService.
 */
public class JavaMailEmailServiceTest {
    
    private JavaMailEmailService emailService;
    
    @Mock
    private EmailConfigurationManager configManager;
    
    @Mock
    private Session session;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Mock the configuration manager
        Field configManagerField = EmailConfigurationManager.class.getDeclaredField("INSTANCE");
        configManagerField.setAccessible(true);
        configManagerField.set(null, configManager);
        
        // Set up default configuration
        when(configManager.getSmtpHost()).thenReturn("email-smtp.ap-south-1.amazonaws.com");
        when(configManager.getSmtpPort()).thenReturn(465);
        when(configManager.isSmtpAuthEnabled()).thenReturn(true);
        when(configManager.isSmtpSslEnabled()).thenReturn(true);
        when(configManager.getSmtpUsername()).thenReturn("AKIAXWWAENHRKEXWAEU2");
        when(configManager.getSmtpPassword()).thenReturn("BJ+IiVs1NLQg/cOoWTF3Woedp1prO9crMRH0ZK2Cv2HY");
        when(configManager.getFromAddress()).thenReturn("no-reply@leucinetech.com");
        when(configManager.getFromName()).thenReturn("Celestra System");
        when(configManager.getRetryAttempts()).thenReturn(3);
        when(configManager.getRetryDelayMs()).thenReturn(0); // No delay for testing
        when(configManager.getConnectionTimeout()).thenReturn(10000);
        when(configManager.getSocketTimeout()).thenReturn(10000);
        
        Properties props = new Properties();
        props.put("mail.smtp.host", "email-smtp.ap-south-1.amazonaws.com");
        props.put("mail.smtp.port", 465);
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.ssl.enable", true);
        when(configManager.getJavaMailProperties()).thenReturn(props);
        
        // Create the service
        emailService = spy(new JavaMailEmailService());
        
        // Inject mock session
        injectMock(emailService, "session", session);
    }
    
    /**
     * Test sending a plain text email to a single recipient.
     */
    @Test
    public void testSendPlainTextEmail() throws Exception {
        // Prepare test data
        String to = "nupur.bhaisare@leucinetech.com";
        String subject = "Test Email";
        String body = "This is a test email.";
        
        // Mock MimeMessage
        MimeMessage mockMessage = mock(MimeMessage.class);
        doReturn(mockMessage).when(emailService).createMimeMessage();
        
        // Use MockedStatic for Transport
        try (MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
            // Call the method
            emailService.sendPlainTextEmail(to, subject, body);
            
            // Verify Transport.send was called
            mockedTransport.verify(() -> Transport.send(mockMessage));
            
            // Verify message properties
            verify(mockMessage).setFrom(any(InternetAddress.class));
            verify(mockMessage).addRecipient(eq(Message.RecipientType.TO), any(InternetAddress.class));
            verify(mockMessage).setSubject(subject);
            verify(mockMessage).setText(body, "utf-8");
        }
    }
    
    /**
     * Test sending an HTML email to multiple recipients.
     */
    @Test
    public void testSendHtmlEmail() throws Exception {
        // Prepare test data
        List<String> to = List.of("nupur.bhaisare@leucinetech.com", "test@example.com");
        String subject = "Test HTML Email";
        String htmlBody = "<html><body><h1>Test</h1><p>This is a test HTML email.</p></body></html>";
        
        // Mock MimeMessage
        MimeMessage mockMessage = mock(MimeMessage.class);
        doReturn(mockMessage).when(emailService).createMimeMessage();
        
        // Use MockedStatic for Transport
        try (MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
            // Call the method
            emailService.sendHtmlEmail(to, subject, htmlBody);
            
            // Verify Transport.send was called
            mockedTransport.verify(() -> Transport.send(mockMessage));
            
            // Verify message properties
            verify(mockMessage).setFrom(any(InternetAddress.class));
            verify(mockMessage, times(2)).addRecipient(eq(Message.RecipientType.TO), any(InternetAddress.class));
            verify(mockMessage).setSubject(subject);
            verify(mockMessage).setContent(htmlBody, "text/html; charset=utf-8");
        }
    }
    
    /**
     * Test sending an email with CC and BCC recipients.
     */
    @Test
    public void testSendEmailWithCcAndBcc() throws Exception {
        // Prepare test data
        List<String> to = List.of("nupur.bhaisare@leucinetech.com");
        List<String> cc = List.of("cc@example.com");
        List<String> bcc = List.of("bcc@example.com");
        String subject = "Test Email with CC and BCC";
        String body = "This is a test email with CC and BCC recipients.";
        
        // Mock MimeMessage
        MimeMessage mockMessage = mock(MimeMessage.class);
        doReturn(mockMessage).when(emailService).createMimeMessage();
        
        // Use MockedStatic for Transport
        try (MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
            // Call the method
            emailService.sendPlainTextEmail(to, cc, bcc, subject, body);
            
            // Verify Transport.send was called
            mockedTransport.verify(() -> Transport.send(mockMessage));
            
            // Verify message properties
            verify(mockMessage).setFrom(any(InternetAddress.class));
            verify(mockMessage).addRecipient(eq(Message.RecipientType.TO), any(InternetAddress.class));
            verify(mockMessage).addRecipient(eq(Message.RecipientType.CC), any(InternetAddress.class));
            verify(mockMessage).addRecipient(eq(Message.RecipientType.BCC), any(InternetAddress.class));
            verify(mockMessage).setSubject(subject);
            verify(mockMessage).setText(body, "utf-8");
        }
    }
    
    /**
     * Test sending an email with attachments.
     */
    @Test
    public void testSendEmailWithAttachments() throws Exception {
        // Prepare test data
        List<String> to = List.of("nupur.bhaisare@leucinetech.com");
        String subject = "Test Email with Attachments";
        String body = "This is a test email with attachments.";
        String htmlBody = "<html><body><h1>Test</h1><p>This is a test email with attachments.</p></body></html>";
        File attachment = new File("pom.xml"); // Use an existing file
        
        // Mock MimeMessage
        MimeMessage mockMessage = mock(MimeMessage.class);
        doReturn(mockMessage).when(emailService).createMimeMessage();
        
        // Use MockedStatic for Transport
        try (MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
            // Call the method
            emailService.sendEmailWithAttachments(to, null, null, subject, body, htmlBody, List.of(attachment));
            
            // Verify Transport.send was called
            mockedTransport.verify(() -> Transport.send(mockMessage));
            
            // Verify message properties
            verify(mockMessage).setFrom(any(InternetAddress.class));
            verify(mockMessage).addRecipient(eq(Message.RecipientType.TO), any(InternetAddress.class));
            verify(mockMessage).setSubject(subject);
            verify(mockMessage).setContent(any(MimeMultipart.class));
        }
    }
    
    /**
     * Test validation of email parameters.
     */
    @Test
    public void testValidation() throws Exception {
        // Test with empty recipient list
        try {
            emailService.sendPlainTextEmail(List.of(), "Subject", "Body");
            fail("Expected EmailException for empty recipient list");
        } catch (EmailException e) {
            assertTrue(e.getMessage().contains("Recipient list cannot be empty"));
        }
        
        // Test with null subject
        try {
            emailService.sendPlainTextEmail("nupur.bhaisare@leucinetech.com", null, "Body");
            fail("Expected EmailException for null subject");
        } catch (EmailException e) {
            assertTrue(e.getMessage().contains("Subject cannot be empty"));
        }
        
        // Test with empty subject
        try {
            emailService.sendPlainTextEmail("nupur.bhaisare@leucinetech.com", "", "Body");
            fail("Expected EmailException for empty subject");
        } catch (EmailException e) {
            assertTrue(e.getMessage().contains("Subject cannot be empty"));
        }
    }
    
    /**
     * Test retry mechanism.
     */
    @Test
    public void testRetryMechanism() throws Exception {
        // Prepare test data
        String to = "nupur.bhaisare@leucinetech.com";
        String subject = "Test Email";
        String body = "This is a test email.";
        
        // Mock MimeMessage
        MimeMessage mockMessage = mock(MimeMessage.class);
        doReturn(mockMessage).when(emailService).createMimeMessage();
        
        // Use MockedStatic for Transport
        try (MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
            // First call throws exception, second call succeeds
            mockedTransport.when(() -> Transport.send(mockMessage))
                    .thenThrow(new MessagingException("Test exception"))
                    .thenReturn(null);
            
            // Call the method
            emailService.sendPlainTextEmail(to, subject, body);
            
            // Verify Transport.send was called twice
            mockedTransport.verify(() -> Transport.send(mockMessage), times(2));
        }
    }
    
    /**
     * Test retry mechanism with all attempts failing.
     */
    @Test
    public void testRetryMechanismAllFailing() throws Exception {
        // Prepare test data
        String to = "nupur.bhaisare@leucinetech.com";
        String subject = "Test Email";
        String body = "This is a test email.";
        
        // Mock MimeMessage
        MimeMessage mockMessage = mock(MimeMessage.class);
        doReturn(mockMessage).when(emailService).createMimeMessage();
        
        // Set retry attempts to 1 for faster testing
        when(configManager.getRetryAttempts()).thenReturn(1);
        
        // Use MockedStatic for Transport
        try (MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
            // All calls throw exception
            mockedTransport.when(() -> Transport.send(mockMessage))
                    .thenThrow(new MessagingException("Test exception"));
            
            // Call the method and expect exception
            try {
                emailService.sendPlainTextEmail(to, subject, body);
                fail("Expected EmailException");
            } catch (EmailException e) {
                assertTrue(e.getMessage().contains("Error sending email"));
            }
            
            // Verify Transport.send was called the expected number of times (initial + retries)
            mockedTransport.verify(() -> Transport.send(mockMessage), times(2));
        }
    }
    
    /**
     * Helper method to inject a mock into a private field using reflection.
     */
    private void injectMock(Object target, String fieldName, Object mockObject) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mockObject);
    }
}