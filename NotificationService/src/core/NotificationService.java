package core;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class NotificationService {

    private final EmailProviderIntegration emailProvider;
    private final SmsProviderIntegration smsProvider;

    public NotificationService(EmailProviderIntegration emailProvider, SmsProviderIntegration smsProvider) {
        this.emailProvider = emailProvider;
        this.smsProvider = smsProvider;
    }

    public enum NotificationType {
        EMAIL, SMS
    }

    public void sendNotification(NotificationType type, String recipient, String message) {
        switch (type) {
            case EMAIL:
                sendEmailNotification(recipient, message);
                break;
            case SMS:
                sendSmsNotification(recipient, message);
                break;
            default:
                throw new IllegalArgumentException("Unsupported notification type: " + type);
        }
    }

    private void sendEmailNotification(String recipient, String message) {
        try {
            emailProvider.sendEmail(recipient, message);
            System.out.println("Email sent to " + recipient);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    private void sendSmsNotification(String recipient, String message) {
        try {
            smsProvider.sendSms(recipient, message);
            System.out.println("SMS sent to " + recipient);
        } catch (Exception e) {
            System.err.println("Failed to send SMS: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        EmailProviderIntegration emailProvider = new EmailProviderIntegration();
        SmsProviderIntegration smsProvider = new SmsProviderIntegration();
        NotificationService service = new NotificationService(emailProvider, smsProvider);

        // Usage
        service.sendNotification(NotificationType.EMAIL, "user@website.com", "Welcome to our platform!");
        service.sendNotification(NotificationType.SMS, "+123456789", "Your order has been shipped.");
    }
}

class EmailProviderIntegration {
    private Map<String, String> emailServerConfig;

    public EmailProviderIntegration() {
        emailServerConfig = new HashMap<>();
        emailServerConfig.put("smtp_host", "smtp.website.com");
        emailServerConfig.put("smtp_port", "587");
        emailServerConfig.put("username", "notification@website.com");
        emailServerConfig.put("password", "password");
    }

    public void sendEmail(String recipient, String message) throws MessagingException {
        // Set up the email properties
        Properties props = new Properties();
        props.put("mail.smtp.host", emailServerConfig.get("smtp_host"));
        props.put("mail.smtp.port", emailServerConfig.get("smtp_port"));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        // Authenticate and get session
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        emailServerConfig.get("username"),
                        emailServerConfig.get("password"));
            }
        });

        // Compose the email
        Message emailMessage = new MimeMessage(session);
        emailMessage.setFrom(new InternetAddress(emailServerConfig.get("username")));
        emailMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        emailMessage.setSubject("Notification");
        emailMessage.setText(message);

        // Send the email
        Transport.send(emailMessage);
    }
}

class SmsProviderIntegration {
    private static final String ACCOUNT_SID = "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
    private static final String AUTH_TOKEN = "auth_token";

    public SmsProviderIntegration() {
        // Initialize Twilio client
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public void sendSms(String recipient, String message) {
        Message smsMessage = Message.creator(
                new PhoneNumber(recipient),
                new PhoneNumber("+123456789"),
                message
        ).create();
        System.out.println("SMS sent to " + recipient + ": " + smsMessage.getSid());
    }
}

// Notification Entity Class
class NotificationEntity {
    private String id;
    private String recipient;
    private String message;
    private NotificationService.NotificationType type;
    private boolean delivered;

    public NotificationEntity(String id, String recipient, String message, NotificationService.NotificationType type) {
        this.id = id;
        this.recipient = recipient;
        this.message = message;
        this.type = type;
        this.delivered = false;
    }

    public String getId() {
        return id;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessage() {
        return message;
    }

    public NotificationService.NotificationType getType() {
        return type;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    @Override
    public String toString() {
        return "NotificationEntity{" +
                "id='" + id + '\'' +
                ", recipient='" + recipient + '\'' +
                ", message='" + message + '\'' +
                ", type=" + type +
                ", delivered=" + delivered +
                '}';
    }
}

// Notification Service Configuration
class NotificationServiceConfig {
    private String emailServerHost;
    private String emailServerPort;
    private String smsApiUrl;
    private String smsApiKey;

    public NotificationServiceConfig() {
        // Load configuration settings
        this.emailServerHost = "smtp.website.com";
        this.emailServerPort = "587";
        this.smsApiUrl = "https://smsapi.website.com/send";
        this.smsApiKey = "apikey12345";
    }

    public String getEmailServerHost() {
        return emailServerHost;
    }

    public String getEmailServerPort() {
        return emailServerPort;
    }

    public String getSmsApiUrl() {
        return smsApiUrl;
    }

    public String getSmsApiKey() {
        return smsApiKey;
    }
}

// Command Handlers
class SendEmailCommandHandler {
    private final NotificationService notificationService;

    public SendEmailCommandHandler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void handle(String recipient, String message) {
        notificationService.sendNotification(NotificationService.NotificationType.EMAIL, recipient, message);
    }
}

class SendSmsCommandHandler {
    private final NotificationService notificationService;

    public SendSmsCommandHandler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void handle(String recipient, String message) {
        notificationService.sendNotification(NotificationService.NotificationType.SMS, recipient, message);
    }
}