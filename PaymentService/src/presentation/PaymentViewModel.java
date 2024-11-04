package presentation;

import com.ecommerce.payment.core.PaymentEntity;
import com.ecommerce.payment.application.dto.PaymentDTO;
import com.ecommerce.payment.application.commands.ProcessPaymentCommandHandler;
import com.ecommerce.payment.application.queries.PaymentStatusQueryHandler;
import com.ecommerce.payment.application.events.PaymentEventPublisher;

import java.util.UUID;
import java.time.LocalDateTime;

public class PaymentViewModel {

    private final ProcessPaymentCommandHandler processPaymentCommandHandler;
    private final PaymentStatusQueryHandler paymentStatusQueryHandler;
    private final PaymentEventPublisher paymentEventPublisher;

    public PaymentViewModel(
            ProcessPaymentCommandHandler processPaymentCommandHandler,
            PaymentStatusQueryHandler paymentStatusQueryHandler,
            PaymentEventPublisher paymentEventPublisher) {
        this.processPaymentCommandHandler = processPaymentCommandHandler;
        this.paymentStatusQueryHandler = paymentStatusQueryHandler;
        this.paymentEventPublisher = paymentEventPublisher;
    }

    public PaymentDTO processPayment(String orderId, String userId, String paymentMethod, double amount) {
        // Validate payment method
        if (!isValidPaymentMethod(paymentMethod)) {
            throw new IllegalArgumentException("Invalid payment method");
        }

        // Create a PaymentEntity for processing
        PaymentEntity paymentEntity = new PaymentEntity(
                UUID.randomUUID().toString(),
                orderId,
                userId,
                paymentMethod,
                amount,
                LocalDateTime.now(),
                "PENDING"
        );

        // Execute the process payment command
        processPaymentCommandHandler.handle(paymentEntity);

        // Publish payment event
        paymentEventPublisher.publishPaymentInitiated(paymentEntity);

        // Convert PaymentEntity to PaymentDTO for response
        return toDTO(paymentEntity);
    }

    public PaymentDTO getPaymentStatus(String paymentId) {
        // Query the status of the payment
        PaymentEntity paymentEntity = paymentStatusQueryHandler.handle(paymentId);

        if (paymentEntity == null) {
            throw new IllegalArgumentException("Payment not found");
        }

        // Convert PaymentEntity to PaymentDTO for response
        return toDTO(paymentEntity);
    }

    private boolean isValidPaymentMethod(String paymentMethod) {
        // Payment method validation logic
        return paymentMethod.equalsIgnoreCase("CREDIT_CARD") ||
               paymentMethod.equalsIgnoreCase("DEBIT_CARD") ||
               paymentMethod.equalsIgnoreCase("PAYPAL") ||
               paymentMethod.equalsIgnoreCase("BANK_TRANSFER");
    }

    private PaymentDTO toDTO(PaymentEntity paymentEntity) {
        return new PaymentDTO(
                paymentEntity.getPaymentId(),
                paymentEntity.getOrderId(),
                paymentEntity.getUserId(),
                paymentEntity.getPaymentMethod(),
                paymentEntity.getAmount(),
                paymentEntity.getTimestamp(),
                paymentEntity.getStatus()
        );
    }

    public void updatePaymentStatus(String paymentId, String newStatus) {
        // Query the payment entity
        PaymentEntity paymentEntity = paymentStatusQueryHandler.handle(paymentId);

        if (paymentEntity == null) {
            throw new IllegalArgumentException("Payment not found");
        }

        // Update the payment status
        paymentEntity.setStatus(newStatus);

        // Publish event for status update
        paymentEventPublisher.publishPaymentStatusUpdated(paymentEntity);
    }

    public void cancelPayment(String paymentId) {
        // Query the payment entity
        PaymentEntity paymentEntity = paymentStatusQueryHandler.handle(paymentId);

        if (paymentEntity == null) {
            throw new IllegalArgumentException("Payment not found");
        }

        // Update payment status to "CANCELED"
        paymentEntity.setStatus("CANCELED");

        // Execute cancel command
        processPaymentCommandHandler.handleCancellation(paymentEntity);

        // Publish cancellation event
        paymentEventPublisher.publishPaymentCanceled(paymentEntity);
    }

    public PaymentDTO refundPayment(String paymentId, double refundAmount) {
        // Query the payment entity
        PaymentEntity paymentEntity = paymentStatusQueryHandler.handle(paymentId);

        if (paymentEntity == null) {
            throw new IllegalArgumentException("Payment not found");
        }

        // Validate refund amount
        if (refundAmount <= 0 || refundAmount > paymentEntity.getAmount()) {
            throw new IllegalArgumentException("Invalid refund amount");
        }

        // Update payment status to "REFUNDED"
        paymentEntity.setStatus("REFUNDED");

        // Handle refund logic
        processPaymentCommandHandler.handleRefund(paymentEntity, refundAmount);

        // Publish refund event
        paymentEventPublisher.publishPaymentRefunded(paymentEntity, refundAmount);

        // Convert PaymentEntity to PaymentDTO for response
        return toDTO(paymentEntity);
    }
}