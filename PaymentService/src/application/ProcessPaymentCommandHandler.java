package application;

import com.website.paymentservice.core.PaymentEntity;
import com.website.paymentservice.core.PaymentService;
import com.website.paymentservice.infrastructure.PaymentGatewayIntegration;
import com.website.paymentservice.infrastructure.PaymentRepositoryImpl;
import com.website.common.commands.ProcessPaymentCommand;
import com.website.common.events.PaymentProcessedEvent;
import com.website.common.exceptions.PaymentFailedException;
import com.website.common.valueobjects.PaymentDetails;
import com.website.common.valueobjects.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class ProcessPaymentCommandHandler {

    private final PaymentService paymentService;
    private final PaymentGatewayIntegration paymentGateway;
    private final PaymentRepositoryImpl paymentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private static final Logger logger = LoggerFactory.getLogger(ProcessPaymentCommandHandler.class);

    @Autowired
    public ProcessPaymentCommandHandler(
            PaymentService paymentService,
            PaymentGatewayIntegration paymentGateway,
            PaymentRepositoryImpl paymentRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.paymentService = paymentService;
        this.paymentGateway = paymentGateway;
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public PaymentStatus handle(ProcessPaymentCommand command) {
        logger.info("Processing payment for Order ID: {}", command.getOrderId());
        try {
            PaymentDetails paymentDetails = preparePaymentDetails(command);
            PaymentEntity payment = initializePayment(command);
            PaymentStatus paymentStatus = processPaymentWithGateway(paymentDetails);

            if (paymentStatus == PaymentStatus.SUCCESS) {
                logger.info("Payment successful for Order ID: {}", command.getOrderId());
                finalizePayment(payment, paymentStatus);
                publishPaymentProcessedEvent(payment);
            } else {
                logger.warn("Payment failed for Order ID: {}", command.getOrderId());
                throw new PaymentFailedException("Payment failed for Order ID: " + command.getOrderId());
            }
            return paymentStatus;
        } catch (PaymentFailedException e) {
            logger.error("Payment processing failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error occurred during payment processing: {}", e.getMessage());
            throw new PaymentFailedException("Unexpected error during payment processing.");
        }
    }

    private PaymentDetails preparePaymentDetails(ProcessPaymentCommand command) {
        return new PaymentDetails(
                command.getCardNumber(),
                command.getCardExpiryDate(),
                command.getCardCVV(),
                command.getPaymentAmount()
        );
    }

    private PaymentEntity initializePayment(ProcessPaymentCommand command) {
        PaymentEntity payment = new PaymentEntity();
        payment.setId(UUID.randomUUID().toString());
        payment.setOrderId(command.getOrderId());
        payment.setAmount(command.getPaymentAmount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
        logger.info("Initialized payment entity for Order ID: {}", command.getOrderId());
        return payment;
    }

    private PaymentStatus processPaymentWithGateway(PaymentDetails paymentDetails) {
        logger.info("Sending payment details to payment gateway.");
        PaymentStatus paymentStatus = paymentGateway.processPayment(paymentDetails);
        logger.info("Payment gateway response: {}", paymentStatus);
        return paymentStatus;
    }

    private void finalizePayment(PaymentEntity payment, PaymentStatus paymentStatus) {
        payment.setStatus(paymentStatus);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.update(payment);
        logger.info("Payment finalized for Order ID: {}", payment.getOrderId());
    }

    private void publishPaymentProcessedEvent(PaymentEntity payment) {
        PaymentProcessedEvent event = new PaymentProcessedEvent(
                payment.getOrderId(),
                payment.getId(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getUpdatedAt()
        );
        eventPublisher.publishEvent(event);
        logger.info("Published PaymentProcessedEvent for Order ID: {}", payment.getOrderId());
    }

    public void handleRefund(ProcessPaymentCommand command) {
        logger.info("Processing refund for Order ID: {}", command.getOrderId());
        PaymentEntity payment = paymentRepository.findByOrderId(command.getOrderId());

        if (payment == null) {
            logger.error("Payment not found for Order ID: {}", command.getOrderId());
            throw new PaymentFailedException("Payment not found for Order ID: " + command.getOrderId());
        }

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            logger.info("Refund already processed for Order ID: {}", command.getOrderId());
            return;
        }

        try {
            PaymentStatus refundStatus = paymentGateway.processRefund(payment.getAmount());
            if (refundStatus == PaymentStatus.REFUNDED) {
                logger.info("Refund successful for Order ID: {}", command.getOrderId());
                finalizeRefund(payment, refundStatus);
                publishRefundProcessedEvent(payment);
            } else {
                logger.warn("Refund failed for Order ID: {}", command.getOrderId());
                throw new PaymentFailedException("Refund failed for Order ID: " + command.getOrderId());
            }
        } catch (Exception e) {
            logger.error("Unexpected error occurred during refund processing: {}", e.getMessage());
            throw new PaymentFailedException("Unexpected error during refund processing.");
        }
    }

    private void finalizeRefund(PaymentEntity payment, PaymentStatus refundStatus) {
        payment.setStatus(refundStatus);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.update(payment);
        logger.info("Refund finalized for Order ID: {}", payment.getOrderId());
    }

    private void publishRefundProcessedEvent(PaymentEntity payment) {
        PaymentProcessedEvent refundEvent = new PaymentProcessedEvent(
                payment.getOrderId(),
                payment.getId(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getUpdatedAt()
        );
        eventPublisher.publishEvent(refundEvent);
        logger.info("Published RefundProcessedEvent for Order ID: {}", payment.getOrderId());
    }

    public PaymentStatus retryPayment(ProcessPaymentCommand command) {
        logger.info("Retrying payment for Order ID: {}", command.getOrderId());
        PaymentEntity payment = paymentRepository.findByOrderId(command.getOrderId());

        if (payment == null || payment.getStatus() != PaymentStatus.FAILED) {
            logger.error("Cannot retry payment for Order ID: {} - invalid state.", command.getOrderId());
            throw new PaymentFailedException("Cannot retry payment for Order ID: " + command.getOrderId());
        }

        return handle(command); 
    }
}