package presentation;

import notificationservice.core.NotificationService;
import notificationservice.core.NotificationEntity;
import notificationservice.application.SendEmailCommandHandler;
import notificationservice.application.SendSmsCommandHandler;
import notificationservice.infrastructure.EmailProviderIntegration;
import notificationservice.infrastructure.SmsProviderIntegration;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NotificationViewModel {
    private final NotificationService notificationService;
    private final SendEmailCommandHandler emailCommandHandler;
    private final SendSmsCommandHandler smsCommandHandler;
    private final EmailProviderIntegration emailProvider;
    private final SmsProviderIntegration smsProvider;

    public NotificationViewModel(
            NotificationService notificationService,
            SendEmailCommandHandler emailCommandHandler,
            SendSmsCommandHandler smsCommandHandler,
            EmailProviderIntegration emailProvider,
            SmsProviderIntegration smsProvider
    ) {
        this.notificationService = notificationService;
        this.emailCommandHandler = emailCommandHandler;
        this.smsCommandHandler = smsCommandHandler;
        this.emailProvider = emailProvider;
        this.smsProvider = smsProvider;
    }

    // Logic for sending notifications
    public CompletableFuture<Void> sendNotification(NotificationEntity notification) {
        if (notification.getType().equalsIgnoreCase("email")) {
            return sendEmail(notification);
        } else if (notification.getType().equalsIgnoreCase("sms")) {
            return sendSms(notification);
        } else {
            throw new IllegalArgumentException("Unsupported notification type: " + notification.getType());
        }
    }

    private CompletableFuture<Void> sendEmail(NotificationEntity notification) {
        return CompletableFuture.runAsync(() -> {
            try {
                emailCommandHandler.handle(notification);
                notification.setStatus("delivered");
                logSuccess(notification);
            } catch (Exception e) {
                notification.setStatus("failed");
                logFailure(notification, e.getMessage());
            }
        });
    }

    private CompletableFuture<Void> sendSms(NotificationEntity notification) {
        return CompletableFuture.runAsync(() -> {
            try {
                smsCommandHandler.handle(notification);
                notification.setStatus("delivered");
                logSuccess(notification);
            } catch (Exception e) {
                notification.setStatus("failed");
                logFailure(notification, e.getMessage());
            }
        });
    }

    private void logSuccess(NotificationEntity notification) {
        System.out.println("Notification sent successfully: " + notification.toString());
    }

    private void logFailure(NotificationEntity notification, String errorMessage) {
        System.err.println("Failed to send notification: " + notification.toString() + ". Error: " + errorMessage);
    }

    // Logic for handling batch notifications
    public CompletableFuture<Void> sendBatchNotifications(List<NotificationEntity> notifications) {
        CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                notifications.stream()
                        .map(this::sendNotification)
                        .toArray(CompletableFuture[]::new)
        );
        return allTasks;
    }

    // Fetch notification history
    public CompletableFuture<List<NotificationEntity>> getNotificationHistory() {
        return CompletableFuture.supplyAsync(() -> notificationService.getNotificationHistory());
    }

    // Send email with attachments
    public CompletableFuture<Void> sendEmailWithAttachment(NotificationEntity notification, byte[] attachmentData, String fileName) {
        return CompletableFuture.runAsync(() -> {
            try {
                emailProvider.sendEmailWithAttachment(notification, attachmentData, fileName);
                notification.setStatus("delivered");
                logSuccess(notification);
            } catch (Exception e) {
                notification.setStatus("failed");
                logFailure(notification, e.getMessage());
            }
        });
    }

    // Validate notification entity
    public boolean validateNotification(NotificationEntity notification) {
        if (notification.getRecipient() == null || notification.getRecipient().isEmpty()) {
            throw new IllegalArgumentException("Recipient cannot be null or empty");
        }
        if (notification.getMessage() == null || notification.getMessage().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
        if (notification.getType() == null || notification.getType().isEmpty()) {
            throw new IllegalArgumentException("Notification type cannot be null or empty");
        }
        return true;
    }

    // Retry failed notifications
    public CompletableFuture<Void> retryFailedNotification(NotificationEntity failedNotification) {
        return CompletableFuture.runAsync(() -> {
            try {
                sendNotification(failedNotification);
                logSuccess(failedNotification);
            } catch (Exception e) {
                logFailure(failedNotification, e.getMessage());
            }
        });
    }

    // Send notifications with priority
    public CompletableFuture<Void> sendNotificationWithPriority(NotificationEntity notification, int priority) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (priority > 5) {
                    sendEmail(notification);
                } else {
                    sendSms(notification);
                }
                logSuccess(notification);
            } catch (Exception e) {
                logFailure(notification, e.getMessage());
            }
        });
    }

    // Calculate delivery status
    public String calculateDeliveryStatus(NotificationEntity notification) {
        String status = notification.getStatus();
        if ("delivered".equalsIgnoreCase(status)) {
            return "Notification successfully delivered.";
        } else if ("pending".equalsIgnoreCase(status)) {
            return "Notification is still pending.";
        } else if ("failed".equalsIgnoreCase(status)) {
            return "Notification delivery failed.";
        } else {
            return "Unknown notification status.";
        }
    }
}