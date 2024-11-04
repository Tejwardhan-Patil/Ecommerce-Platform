package core;

import entities.PaymentEntity;
import infrastructure.PaymentGatewayIntegration;
import infrastructure.PaymentRepositoryImpl;
import application.ProcessPaymentCommandHandler;
import application.PaymentStatusQueryHandler;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentService {

    private final PaymentRepositoryImpl paymentRepository;
    private final PaymentGatewayIntegration paymentGateway;
    private final ProcessPaymentCommandHandler paymentCommandHandler;
    private final PaymentStatusQueryHandler paymentStatusHandler;

    public PaymentService(PaymentRepositoryImpl paymentRepository, PaymentGatewayIntegration paymentGateway,
                          ProcessPaymentCommandHandler paymentCommandHandler, PaymentStatusQueryHandler paymentStatusHandler) {
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
        this.paymentCommandHandler = paymentCommandHandler;
        this.paymentStatusHandler = paymentStatusHandler;
    }

    public PaymentEntity processPayment(UUID orderId, BigDecimal amount, String paymentMethod) throws Exception {
        validatePaymentDetails(orderId, amount, paymentMethod);

        PaymentEntity payment = new PaymentEntity(UUID.randomUUID(), orderId, amount, paymentMethod, LocalDateTime.now(), "PENDING");
        paymentRepository.save(payment);

        boolean paymentSuccess = paymentGateway.processPayment(payment);
        if (paymentSuccess) {
            payment.setStatus("COMPLETED");
            paymentRepository.update(payment);
        } else {
            payment.setStatus("FAILED");
            paymentRepository.update(payment);
            throw new Exception("Payment failed for order ID: " + orderId);
        }

        return payment;
    }

    public String getPaymentStatus(UUID paymentId) {
        return paymentStatusHandler.handle(paymentId);
    }

    private void validatePaymentDetails(UUID orderId, BigDecimal amount, String paymentMethod) throws IllegalArgumentException {
        if (orderId == null || amount == null || paymentMethod == null) {
            throw new IllegalArgumentException("Invalid payment details. Order ID, amount, and payment method are required.");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
    }

    public boolean refundPayment(UUID paymentId) {
        PaymentEntity payment = paymentRepository.findById(paymentId);
        if (payment == null) {
            throw new IllegalArgumentException("Payment not found with ID: " + paymentId);
        }

        if (!payment.getStatus().equals("COMPLETED")) {
            throw new IllegalArgumentException("Refund is only applicable for completed payments.");
        }

        boolean refundSuccess = paymentGateway.processRefund(payment);
        if (refundSuccess) {
            payment.setStatus("REFUNDED");
            paymentRepository.update(payment);
        }

        return refundSuccess;
    }

    public PaymentEntity findPaymentById(UUID paymentId) {
        return paymentRepository.findById(paymentId);
    }

    public boolean updatePaymentMethod(UUID paymentId, String newPaymentMethod) {
        PaymentEntity payment = paymentRepository.findById(paymentId);
        if (payment == null) {
            throw new IllegalArgumentException("Payment not found with ID: " + paymentId);
        }

        if (payment.getStatus().equals("COMPLETED") || payment.getStatus().equals("FAILED")) {
            throw new IllegalArgumentException("Cannot update payment method for completed or failed payments.");
        }

        payment.setPaymentMethod(newPaymentMethod);
        paymentRepository.update(payment);
        return true;
    }

    public boolean cancelPayment(UUID paymentId) {
        PaymentEntity payment = paymentRepository.findById(paymentId);
        if (payment == null) {
            throw new IllegalArgumentException("Payment not found with ID: " + paymentId);
        }

        if (!payment.getStatus().equals("PENDING")) {
            throw new IllegalArgumentException("Only pending payments can be canceled.");
        }

        payment.setStatus("CANCELED");
        paymentRepository.update(payment);
        return true;
    }

    public String processRecurringPayment(UUID recurringPaymentId) {
        PaymentEntity recurringPayment = paymentRepository.findById(recurringPaymentId);
        if (recurringPayment == null) {
            throw new IllegalArgumentException("Recurring payment not found with ID: " + recurringPaymentId);
        }

        boolean paymentSuccess = paymentGateway.processPayment(recurringPayment);
        if (paymentSuccess) {
            recurringPayment.setStatus("COMPLETED");
            paymentRepository.update(recurringPayment);
            return "Recurring payment processed successfully.";
        } else {
            recurringPayment.setStatus("FAILED");
            paymentRepository.update(recurringPayment);
            return "Recurring payment failed.";
        }
    }

    public PaymentEntity initiatePayment(UUID orderId, BigDecimal amount, String paymentMethod) {
        validatePaymentDetails(orderId, amount, paymentMethod);

        PaymentEntity payment = new PaymentEntity(UUID.randomUUID(), orderId, amount, paymentMethod, LocalDateTime.now(), "PENDING");
        paymentRepository.save(payment);
        return payment;
    }

    public boolean retryFailedPayment(UUID paymentId) {
        PaymentEntity payment = paymentRepository.findById(paymentId);
        if (payment == null) {
            throw new IllegalArgumentException("Payment not found with ID: " + paymentId);
        }

        if (!payment.getStatus().equals("FAILED")) {
            throw new IllegalArgumentException("Retry is only applicable for failed payments.");
        }

        boolean paymentSuccess = paymentGateway.processPayment(payment);
        if (paymentSuccess) {
            payment.setStatus("COMPLETED");
            paymentRepository.update(payment);
        }

        return paymentSuccess;
    }

    public boolean setPaymentAsFraud(UUID paymentId) {
        PaymentEntity payment = paymentRepository.findById(paymentId);
        if (payment == null) {
            throw new IllegalArgumentException("Payment not found with ID: " + paymentId);
        }

        if (payment.getStatus().equals("COMPLETED") || payment.getStatus().equals("FAILED")) {
            throw new IllegalArgumentException("Cannot mark completed or failed payments as fraud.");
        }

        payment.setStatus("FRAUD");
        paymentRepository.update(payment);
        return true;
    }
}