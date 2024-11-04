package core;

import java.time.LocalDateTime;
import java.util.Objects;

public class NotificationEntity {

    private String notificationId;
    private String recipientId;
    private String recipientEmail;
    private String recipientPhoneNumber;
    private NotificationType notificationType;
    private NotificationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime deliveredAt;
    private String message;
    private String deliveryChannel;
    private String providerResponse;
    private LocalDateTime lastRetryAt;
    private int retryCount;
    private boolean isRead;

    // Constructors
    public NotificationEntity() {}

    public NotificationEntity(String notificationId, String recipientId, String recipientEmail, 
                              String recipientPhoneNumber, NotificationType notificationType, 
                              NotificationStatus status, LocalDateTime createdAt, String message, 
                              String deliveryChannel) {
        this.notificationId = notificationId;
        this.recipientId = recipientId;
        this.recipientEmail = recipientEmail;
        this.recipientPhoneNumber = recipientPhoneNumber;
        this.notificationType = notificationType;
        this.status = status;
        this.createdAt = createdAt;
        this.message = message;
        this.deliveryChannel = deliveryChannel;
        this.isRead = false;
        this.retryCount = 0;
    }

    // Getters and Setters
    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getRecipientPhoneNumber() {
        return recipientPhoneNumber;
    }

    public void setRecipientPhoneNumber(String recipientPhoneNumber) {
        this.recipientPhoneNumber = recipientPhoneNumber;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDeliveryChannel() {
        return deliveryChannel;
    }

    public void setDeliveryChannel(String deliveryChannel) {
        this.deliveryChannel = deliveryChannel;
    }

    public String getProviderResponse() {
        return providerResponse;
    }

    public void setProviderResponse(String providerResponse) {
        this.providerResponse = providerResponse;
    }

    public LocalDateTime getLastRetryAt() {
        return lastRetryAt;
    }

    public void setLastRetryAt(LocalDateTime lastRetryAt) {
        this.lastRetryAt = lastRetryAt;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    // Methods for updating status
    public void markAsDelivered(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
        this.status = NotificationStatus.DELIVERED;
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public void incrementRetryCount() {
        this.retryCount++;
        this.lastRetryAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationEntity that = (NotificationEntity) o;
        return retryCount == that.retryCount && isRead == that.isRead && 
               Objects.equals(notificationId, that.notificationId) && 
               Objects.equals(recipientId, that.recipientId) && 
               Objects.equals(recipientEmail, that.recipientEmail) && 
               Objects.equals(recipientPhoneNumber, that.recipientPhoneNumber) && 
               notificationType == that.notificationType && 
               status == that.status && 
               Objects.equals(createdAt, that.createdAt) && 
               Objects.equals(deliveredAt, that.deliveredAt) && 
               Objects.equals(message, that.message) && 
               Objects.equals(deliveryChannel, that.deliveryChannel) && 
               Objects.equals(providerResponse, that.providerResponse) && 
               Objects.equals(lastRetryAt, that.lastRetryAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId, recipientId, recipientEmail, 
                            recipientPhoneNumber, notificationType, status, 
                            createdAt, deliveredAt, message, deliveryChannel, 
                            providerResponse, lastRetryAt, retryCount, isRead);
    }

    @Override
    public String toString() {
        return "NotificationEntity{" +
                "notificationId='" + notificationId + '\'' +
                ", recipientId='" + recipientId + '\'' +
                ", recipientEmail='" + recipientEmail + '\'' +
                ", recipientPhoneNumber='" + recipientPhoneNumber + '\'' +
                ", notificationType=" + notificationType +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", deliveredAt=" + deliveredAt +
                ", message='" + message + '\'' +
                ", deliveryChannel='" + deliveryChannel + '\'' +
                ", providerResponse='" + providerResponse + '\'' +
                ", lastRetryAt=" + lastRetryAt +
                ", retryCount=" + retryCount +
                ", isRead=" + isRead +
                '}';
    }

    public enum NotificationType {
        EMAIL, SMS, PUSH_NOTIFICATION
    }

    public enum NotificationStatus {
        PENDING, DELIVERED, FAILED
    }
}