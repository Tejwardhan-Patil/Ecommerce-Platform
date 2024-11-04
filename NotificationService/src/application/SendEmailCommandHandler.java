package application;

import com.website.notification.core.NotificationEntity;
import com.website.notification.infrastructure.EmailProviderIntegration;
import com.website.notification.core.NotificationService;
import com.website.notification.application.dto.SendEmailRequest;
import com.website.notification.infrastructure.persistence.NotificationRepository;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SendEmailCommandHandler {

    private static final Logger LOGGER = Logger.getLogger(SendEmailCommandHandler.class.getName());

    private final EmailProviderIntegration emailProvider;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    public SendEmailCommandHandler(EmailProviderIntegration emailProvider, 
                                   NotificationRepository notificationRepository, 
                                   NotificationService notificationService) {
        this.emailProvider = emailProvider;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }

    public void handle(SendEmailRequest request) {
        LOGGER.log(Level.INFO, "Handling email send request for recipient: {0}", request.getRecipient());

        // Validate request
        validateRequest(request);

        // Create a new notification entity
        NotificationEntity notification = createNotificationEntity(request);

        // Persist notification to the database
        persistNotification(notification);

        // Attempt to send the email
        try {
            sendEmail(notification);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error occurred while sending email: {0}", e.getMessage());
            notification.setStatus(NotificationEntity.Status.FAILED);
            updateNotificationStatus(notification);
        }
    }

    private void validateRequest(SendEmailRequest request) {
        if (request == null || request.getRecipient() == null || request.getSubject() == null || request.getBody() == null) {
            throw new IllegalArgumentException("Invalid email request: Missing required fields.");
        }
    }

    private NotificationEntity createNotificationEntity(SendEmailRequest request) {
        NotificationEntity notification = new NotificationEntity();
        notification.setRecipient(request.getRecipient());
        notification.setSubject(request.getSubject());
        notification.setBody(request.getBody());
        notification.setCreatedAt(System.currentTimeMillis());
        notification.setStatus(NotificationEntity.Status.PENDING);
        return notification;
    }

    private void persistNotification(NotificationEntity notification) {
        LOGGER.log(Level.INFO, "Persisting notification for recipient: {0}", notification.getRecipient());
        notificationRepository.save(notification);
    }

    private void sendEmail(NotificationEntity notification) {
        LOGGER.log(Level.INFO, "Sending email to recipient: {0}", notification.getRecipient());
        boolean isSent = emailProvider.sendEmail(notification.getRecipient(), notification.getSubject(), notification.getBody());

        if (isSent) {
            notification.setStatus(NotificationEntity.Status.SENT);
            updateNotificationStatus(notification);
            LOGGER.log(Level.INFO, "Email successfully sent to recipient: {0}", notification.getRecipient());
        } else {
            throw new RuntimeException("Failed to send email to recipient: " + notification.getRecipient());
        }
    }

    private void updateNotificationStatus(NotificationEntity notification) {
        notificationRepository.updateStatus(notification.getId(), notification.getStatus());
    }

    public Optional<NotificationEntity> getNotificationById(Long id) {
        LOGGER.log(Level.INFO, "Fetching notification with ID: {0}", id);
        return notificationRepository.findById(id);
    }

    public void resendFailedNotification(Long notificationId) {
        Optional<NotificationEntity> notificationOptional = getNotificationById(notificationId);

        if (notificationOptional.isPresent()) {
            NotificationEntity notification = notificationOptional.get();
            if (notification.getStatus() == NotificationEntity.Status.FAILED) {
                handle(createSendEmailRequest(notification));
            } else {
                LOGGER.log(Level.WARNING, "Notification with ID {0} is not in FAILED state.", notificationId);
            }
        } else {
            LOGGER.log(Level.WARNING, "Notification with ID {0} not found.", notificationId);
        }
    }

    private SendEmailRequest createSendEmailRequest(NotificationEntity notification) {
        return new SendEmailRequest(notification.getRecipient(), notification.getSubject(), notification.getBody());
    }

    public static class SendEmailRequest {
        private String recipient;
        private String subject;
        private String body;

        public SendEmailRequest(String recipient, String subject, String body) {
            this.recipient = recipient;
            this.subject = subject;
            this.body = body;
        }

        public String getRecipient() {
            return recipient;
        }

        public String getSubject() {
            return subject;
        }

        public String getBody() {
            return body;
        }
    }
}