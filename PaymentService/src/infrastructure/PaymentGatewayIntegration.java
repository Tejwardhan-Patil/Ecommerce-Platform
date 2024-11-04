package infrastructure;

import com.website.paymentservice.core.PaymentEntity;
import com.website.paymentservice.core.PaymentStatus;
import com.website.paymentservice.application.dto.PaymentRequest;
import com.website.paymentservice.application.dto.PaymentResponse;
import com.website.paymentservice.infrastructure.gateway.PayPalGateway;
import com.website.paymentservice.infrastructure.gateway.StripeGateway;
import com.website.paymentservice.infrastructure.gateway.SquareGateway;
import com.website.paymentservice.infrastructure.gateway.PaymentGateway;
import com.website.paymentservice.infrastructure.exceptions.PaymentProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PaymentGatewayIntegration {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentGatewayIntegration.class);

    private final Map<String, PaymentGateway> gatewayMap = new HashMap<>();

    public PaymentGatewayIntegration() {
        initializeGateways();
    }

    private void initializeGateways() {
        gatewayMap.put("PAYPAL", new PayPalGateway());
        gatewayMap.put("STRIPE", new StripeGateway());
        gatewayMap.put("SQUARE", new SquareGateway());
        LOGGER.info("Payment gateways initialized: PAYPAL, STRIPE, SQUARE");
    }

    public PaymentResponse processPayment(PaymentRequest request) {
        LOGGER.info("Starting payment processing for amount: {} using gateway: {}", request.getAmount(), request.getGateway());

        // Validate payment request
        validateRequest(request);

        // Select appropriate gateway
        PaymentGateway gateway = selectGateway(request.getGateway());

        // Generate a unique transaction ID
        String transactionId = UUID.randomUUID().toString();

        try {
            // Process payment via the selected gateway
            PaymentEntity paymentEntity = gateway.processPayment(request, transactionId);

            // Log success
            LOGGER.info("Payment successful. Transaction ID: {}", transactionId);

            // Build and return the success response
            return new PaymentResponse(transactionId, paymentEntity.getStatus(), paymentEntity.getConfirmationNumber(), null);
        } catch (PaymentProcessingException e) {
            LOGGER.error("Payment processing failed. Error: {}", e.getMessage());

            // Return failure response
            return new PaymentResponse(transactionId, PaymentStatus.FAILED, null, e.getMessage());
        }
    }

    private void validateRequest(PaymentRequest request) {
        LOGGER.info("Validating payment request for amount: {}", request.getAmount());

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentProcessingException("Invalid payment amount");
        }

        if (!gatewayMap.containsKey(request.getGateway())) {
            throw new PaymentProcessingException("Unsupported payment gateway: " + request.getGateway());
        }
    }

    private PaymentGateway selectGateway(String gatewayName) {
        LOGGER.info("Selecting payment gateway: {}", gatewayName);
        return Optional.ofNullable(gatewayMap.get(gatewayName))
                .orElseThrow(() -> new PaymentProcessingException("No gateway found for: " + gatewayName));
    }

    // Implementation for refund processing
    public PaymentResponse processRefund(String transactionId, BigDecimal refundAmount, String gateway) {
        LOGGER.info("Initiating refund for Transaction ID: {} using gateway: {}", transactionId, gateway);

        PaymentGateway paymentGateway = selectGateway(gateway);

        try {
            boolean refundSuccess = paymentGateway.processRefund(transactionId, refundAmount);

            if (refundSuccess) {
                LOGGER.info("Refund successful for Transaction ID: {}", transactionId);
                return new PaymentResponse(transactionId, PaymentStatus.REFUNDED, null, null);
            } else {
                LOGGER.warn("Refund failed for Transaction ID: {}", transactionId);
                return new PaymentResponse(transactionId, PaymentStatus.REFUND_FAILED, null, "Refund failed");
            }
        } catch (PaymentProcessingException e) {
            LOGGER.error("Refund processing failed. Error: {}", e.getMessage());
            return new PaymentResponse(transactionId, PaymentStatus.REFUND_FAILED, null, e.getMessage());
        }
    }
}