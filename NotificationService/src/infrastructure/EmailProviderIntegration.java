package infrastructure;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailProviderIntegration {

    private static final Logger logger = LoggerFactory.getLogger(EmailProviderIntegration.class);

    private final String smtpHost;
    private final String smtpPort;
    private final String smtpUser;
    private final String smtpPassword;
    private final String fromEmail;

    private final ExecutorService executorService;

    public EmailProviderIntegration(String smtpHost, String smtpPort, String smtpUser, String smtpPassword, String fromEmail) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.smtpUser = smtpUser;
        this.smtpPassword = smtpPassword;
        this.fromEmail = fromEmail;
        this.executorService = Executors.newFixedThreadPool(10); // Handle parallel email sending
    }

    private Properties getEmailProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        return props;
    }

    private Session getSession() {
        return Session.getInstance(getEmailProperties(), new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUser, smtpPassword);
            }
        });
    }

    public void sendEmailAsync(String toEmail, String subject, String body, Map<String, String> headers) {
        executorService.submit(() -> {
            try {
                sendEmail(toEmail, subject, body, headers);
            } catch (Exception e) {
                logger.error("Error sending email asynchronously to {}", toEmail, e);
            }
        });
    }

    public void sendEmail(String toEmail, String subject, String body, Map<String, String> headers) throws MessagingException, IOException {
        MimeMessage message = new MimeMessage(getSession());
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);
        message.setText(body);

        if (headers != null) {
            headers.forEach((key, value) -> {
                try {
                    message.addHeader(key, value);
                } catch (MessagingException e) {
                    logger.error("Error adding header to email message", e);
                }
            });
        }

        Transport.send(message);
        logger.info("Email sent to {}", toEmail);
    }

    public void retryFailedEmails(String toEmail, String subject, String body, Map<String, String> headers) {
        int retries = 3;
        boolean success = false;
        while (retries > 0 && !success) {
            try {
                sendEmail(toEmail, subject, body, headers);
                success = true;
                logger.info("Email sent successfully on retry to {}", toEmail);
            } catch (MessagingException | IOException e) {
                retries--;
                logger.warn("Retrying email send to {}, attempts remaining: {}", toEmail, retries);
                if (retries == 0) {
                    logger.error("Failed to send email to {} after retries", toEmail, e);
                }
            }
        }
    }

    public void shutdown() {
        try {
            executorService.shutdown();
            logger.info("EmailProviderIntegration service shut down.");
        } catch (Exception e) {
            logger.error("Error during EmailProviderIntegration shutdown", e);
        }
    }

    // Usage
    public static void main(String[] args) {
        EmailProviderIntegration emailProvider = new EmailProviderIntegration(
                "smtp.website.com",
                "587",
                "noreply@website.com",
                "password123",
                "noreply@website.com"
        );

        String toEmail = "user@website.com";
        String subject = "Welcome to our platform!";
        String body = "Thank you for signing up with us.";

        Map<String, String> headers = new HashMap<>();
        headers.put("X-Priority", "1");

        emailProvider.sendEmailAsync(toEmail, subject, body, headers);

        // Simulate failed email for retry
        emailProvider.retryFailedEmails(toEmail, "Important: Action Required", "Please complete your registration.", headers);

        emailProvider.shutdown();
    }
}