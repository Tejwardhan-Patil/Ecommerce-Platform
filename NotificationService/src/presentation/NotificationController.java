package presentation;

import com.website.notificationservice.application.commands.SendEmailCommandHandler;
import com.website.notificationservice.application.commands.SendSmsCommandHandler;
import com.website.notificationservice.application.dtos.NotificationRequestDTO;
import com.website.notificationservice.application.dtos.NotificationResponseDTO;
import com.website.notificationservice.core.NotificationEntity;
import com.website.notificationservice.core.NotificationService;
import com.website.notificationservice.infrastructure.EmailProviderIntegration;
import com.website.notificationservice.infrastructure.SmsProviderIntegration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final SendEmailCommandHandler sendEmailCommandHandler;
    private final SendSmsCommandHandler sendSmsCommandHandler;
    private final EmailProviderIntegration emailProviderIntegration;
    private final SmsProviderIntegration smsProviderIntegration;

    public NotificationController(NotificationService notificationService,
                                  SendEmailCommandHandler sendEmailCommandHandler,
                                  SendSmsCommandHandler sendSmsCommandHandler,
                                  EmailProviderIntegration emailProviderIntegration,
                                  SmsProviderIntegration smsProviderIntegration) {
        this.notificationService = notificationService;
        this.sendEmailCommandHandler = sendEmailCommandHandler;
        this.sendSmsCommandHandler = sendSmsCommandHandler;
        this.emailProviderIntegration = emailProviderIntegration;
        this.smsProviderIntegration = smsProviderIntegration;
    }

    @PostMapping("/email")
    public ResponseEntity<NotificationResponseDTO> sendEmail(@RequestBody NotificationRequestDTO notificationRequest) {
        try {
            NotificationEntity notificationEntity = sendEmailCommandHandler.handle(notificationRequest);
            NotificationResponseDTO responseDTO = new NotificationResponseDTO(notificationEntity.getId(), "Email sent successfully.");
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new NotificationResponseDTO(null, "Failed to send email."));
        }
    }

    @PostMapping("/sms")
    public ResponseEntity<NotificationResponseDTO> sendSms(@RequestBody NotificationRequestDTO notificationRequest) {
        try {
            NotificationEntity notificationEntity = sendSmsCommandHandler.handle(notificationRequest);
            NotificationResponseDTO responseDTO = new NotificationResponseDTO(notificationEntity.getId(), "SMS sent successfully.");
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new NotificationResponseDTO(null, "Failed to send SMS."));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponseDTO> getNotificationById(@PathVariable Long id) {
        Optional<NotificationEntity> notificationEntity = notificationService.getNotificationById(id);
        if (notificationEntity.isPresent()) {
            NotificationResponseDTO responseDTO = new NotificationResponseDTO(notificationEntity.get().getId(), "Notification found.");
            return ResponseEntity.ok(responseDTO);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new NotificationResponseDTO(null, "Notification not found."));
        }
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getAllNotifications() {
        List<NotificationEntity> notificationEntities = notificationService.getAllNotifications();
        List<NotificationResponseDTO> responseDTOs = notificationEntities.stream()
                .map(entity -> new NotificationResponseDTO(entity.getId(), "Notification retrieved."))
                .toList();
        return ResponseEntity.ok(responseDTOs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotificationResponseDTO> updateNotification(@PathVariable Long id,
                                                                      @RequestBody NotificationRequestDTO notificationRequest) {
        try {
            NotificationEntity updatedEntity = notificationService.updateNotification(id, notificationRequest);
            NotificationResponseDTO responseDTO = new NotificationResponseDTO(updatedEntity.getId(), "Notification updated successfully.");
            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new NotificationResponseDTO(null, "Failed to update notification."));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<NotificationResponseDTO> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            return ResponseEntity.ok(new NotificationResponseDTO(id, "Notification deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new NotificationResponseDTO(null, "Failed to delete notification."));
        }
    }

    @PostMapping("/send-test-email")
    public ResponseEntity<NotificationResponseDTO> sendTestEmail() {
        try {
            boolean isSent = emailProviderIntegration.sendTestEmail();
            if (isSent) {
                return ResponseEntity.ok(new NotificationResponseDTO(null, "Test email sent successfully."));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new NotificationResponseDTO(null, "Failed to send test email."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new NotificationResponseDTO(null, "Error occurred while sending test email."));
        }
    }

    @PostMapping("/send-test-sms")
    public ResponseEntity<NotificationResponseDTO> sendTestSms() {
        try {
            boolean isSent = smsProviderIntegration.sendTestSms();
            if (isSent) {
                return ResponseEntity.ok(new NotificationResponseDTO(null, "Test SMS sent successfully."));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new NotificationResponseDTO(null, "Failed to send test SMS."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new NotificationResponseDTO(null, "Error occurred while sending test SMS."));
        }
    }
}