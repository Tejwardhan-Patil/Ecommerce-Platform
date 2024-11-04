package application;

import com.notificationservice.core.NotificationEntity;
import com.notificationservice.core.NotificationService;
import com.notificationservice.infrastructure.SmsProviderIntegration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class SendSmsCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(SendSmsCommandHandler.class);
    private final NotificationService notificationService;
    private final SmsProviderIntegration smsProviderIntegration;

    public SendSmsCommandHandler(NotificationService notificationService, SmsProviderIntegration smsProviderIntegration) {
        this.notificationService = notificationService;
        this.smsProviderIntegration = smsProviderIntegration;
    }

    public void handle(SendSmsCommand command) {
        try {
            // Log the command receipt
            logger.info("Received SendSmsCommand: {}", command);

            // Validate the command
            validateCommand(command);

            // Create NotificationEntity
            NotificationEntity notificationEntity = createNotificationEntity(command);

            // Save notification
            notificationService.saveNotification(notificationEntity);

            // Send SMS
            sendSms(notificationEntity);

            // Update notification status
            notificationEntity.markAsSent();
            notificationService.updateNotification(notificationEntity);

            logger.info("SMS notification sent successfully for ID: {}", notificationEntity.getId());
        } catch (Exception e) {
            logger.error("Failed to send SMS notification: {}", e.getMessage(), e);
            handleFailedNotification(command);
        }
    }

    private void validateCommand(SendSmsCommand command) {
        if (command.getPhoneNumber() == null || command.getPhoneNumber().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required to send SMS");
        }

        if (command.getMessage() == null || command.getMessage().isEmpty()) {
            throw new IllegalArgumentException("Message is required to send SMS");
        }

        logger.debug("SendSmsCommand validated successfully.");
    }

    private NotificationEntity createNotificationEntity(SendSmsCommand command) {
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setId(UUID.randomUUID());
        notificationEntity.setPhoneNumber(command.getPhoneNumber());
        notificationEntity.setMessage(command.getMessage());
        notificationEntity.setType(NotificationEntity.Type.SMS);
        notificationEntity.setStatus(NotificationEntity.Status.PENDING);

        logger.debug("Notification entity created: {}", notificationEntity);
        return notificationEntity;
    }

    private void sendSms(NotificationEntity notificationEntity) {
        boolean isSent = smsProviderIntegration.sendSms(
                notificationEntity.getPhoneNumber(),
                notificationEntity.getMessage()
        );

        if (!isSent) {
            throw new RuntimeException("SMS sending failed");
        }
        logger.debug("SMS sent to: {}", notificationEntity.getPhoneNumber());
    }

    private void handleFailedNotification(SendSmsCommand command) {
        logger.warn("Handling failed notification for phone number: {}", command.getPhoneNumber());

        NotificationEntity failedNotificationEntity = new NotificationEntity();
        failedNotificationEntity.setId(UUID.randomUUID());
        failedNotificationEntity.setPhoneNumber(command.getPhoneNumber());
        failedNotificationEntity.setMessage("Failed to send: " + command.getMessage());
        failedNotificationEntity.setType(NotificationEntity.Type.SMS);
        failedNotificationEntity.setStatus(NotificationEntity.Status.FAILED);

        notificationService.saveNotification(failedNotificationEntity);

        // Implement retry logic
        retrySendSms(command, 3);
    }

    private void retrySendSms(SendSmsCommand command, int retryCount) {
        int attempts = 0;
        boolean isSent = false;

        while (attempts < retryCount && !isSent) {
            try {
                attempts++;
                logger.info("Retrying SMS send attempt {} for phone number: {}", attempts, command.getPhoneNumber());

                NotificationEntity retryNotificationEntity = createNotificationEntity(command);
                notificationService.saveNotification(retryNotificationEntity);
                isSent = smsProviderIntegration.sendSms(command.getPhoneNumber(), command.getMessage());

                if (isSent) {
                    logger.info("SMS sent successfully on retry attempt {} for phone number: {}", attempts, command.getPhoneNumber());
                    retryNotificationEntity.markAsSent();
                    notificationService.updateNotification(retryNotificationEntity);
                }

            } catch (Exception e) {
                logger.error("Retry attempt {} failed for phone number: {} - Error: {}", attempts, command.getPhoneNumber(), e.getMessage());
            }
        }

        if (!isSent) {
            logger.error("All retry attempts failed for phone number: {}", command.getPhoneNumber());
            NotificationEntity failedRetryNotification = new NotificationEntity();
            failedRetryNotification.setId(UUID.randomUUID());
            failedRetryNotification.setPhoneNumber(command.getPhoneNumber());
            failedRetryNotification.setMessage("Failed after retries: " + command.getMessage());
            failedRetryNotification.setType(NotificationEntity.Type.SMS);
            failedRetryNotification.setStatus(NotificationEntity.Status.FAILED);

            notificationService.saveNotification(failedRetryNotification);
        }
    }

    // Command class representing the SMS send request
    public static class SendSmsCommand {

        private final String phoneNumber;
        private final String message;

        public SendSmsCommand(String phoneNumber, String message) {
            this.phoneNumber = phoneNumber;
            this.message = message;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "SendSmsCommand{" +
                    "phoneNumber='" + phoneNumber + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}