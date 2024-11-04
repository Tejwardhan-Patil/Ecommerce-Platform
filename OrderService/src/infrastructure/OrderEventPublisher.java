package infrastructure;

import core.OrderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Logger;

/**
 * OrderEventPublisher handles the publishing of order-related events.
 */
@Service
public class OrderEventPublisher {

    private static final Logger logger = Logger.getLogger(OrderEventPublisher.class.getName());

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    public OrderEventPublisher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Publishes an event when a new order is placed.
     * @param order The order that has been placed.
     */
    public void publishOrderPlacedEvent(OrderEntity order) {
        try {
            String orderJson = objectMapper.writeValueAsString(order);
            kafkaTemplate.send("order-placed-topic", orderJson);
            logger.info("OrderPlacedEvent published successfully for Order ID: " + order.getId());
        } catch (Exception e) {
            logger.severe("Failed to publish OrderPlacedEvent for Order ID: " + order.getId() + " Error: " + e.getMessage());
        }
    }

    /**
     * Publishes an event when an order is updated.
     * @param order The order that has been updated.
     */
    public void publishOrderUpdatedEvent(OrderEntity order) {
        try {
            String orderJson = objectMapper.writeValueAsString(order);
            kafkaTemplate.send("order-updated-topic", orderJson);
            logger.info("OrderUpdatedEvent published successfully for Order ID: " + order.getId());
        } catch (Exception e) {
            logger.severe("Failed to publish OrderUpdatedEvent for Order ID: " + order.getId() + " Error: " + e.getMessage());
        }
    }

    /**
     * Publishes an event when an order is cancelled.
     * @param order The order that has been cancelled.
     */
    public void publishOrderCancelledEvent(OrderEntity order) {
        try {
            String orderJson = objectMapper.writeValueAsString(order);
            kafkaTemplate.send("order-cancelled-topic", orderJson);
            logger.info("OrderCancelledEvent published successfully for Order ID: " + order.getId());
        } catch (Exception e) {
            logger.severe("Failed to publish OrderCancelledEvent for Order ID: " + order.getId() + " Error: " + e.getMessage());
        }
    }

    /**
     * Publishes an event when an order is shipped.
     * @param order The order that has been shipped.
     */
    public void publishOrderShippedEvent(OrderEntity order) {
        try {
            String orderJson = objectMapper.writeValueAsString(order);
            kafkaTemplate.send("order-shipped-topic", orderJson);
            logger.info("OrderShippedEvent published successfully for Order ID: " + order.getId());
        } catch (Exception e) {
            logger.severe("Failed to publish OrderShippedEvent for Order ID: " + order.getId() + " Error: " + e.getMessage());
        }
    }

    /**
     * Publishes an event when an order is delivered.
     * @param order The order that has been delivered.
     */
    public void publishOrderDeliveredEvent(OrderEntity order) {
        try {
            String orderJson = objectMapper.writeValueAsString(order);
            kafkaTemplate.send("order-delivered-topic", orderJson);
            logger.info("OrderDeliveredEvent published successfully for Order ID: " + order.getId());
        } catch (Exception e) {
            logger.severe("Failed to publish OrderDeliveredEvent for Order ID: " + order.getId() + " Error: " + e.getMessage());
        }
    }

    /**
     * Publishes an event when an order payment is completed.
     * @param order The order whose payment has been completed.
     */
    public void publishPaymentCompletedEvent(OrderEntity order) {
        try {
            String orderJson = objectMapper.writeValueAsString(order);
            kafkaTemplate.send("payment-completed-topic", orderJson);
            logger.info("PaymentCompletedEvent published successfully for Order ID: " + order.getId());
        } catch (Exception e) {
            logger.severe("Failed to publish PaymentCompletedEvent for Order ID: " + order.getId() + " Error: " + e.getMessage());
        }
    }

    /**
     * Publishes an event for order refund process initiated.
     * @param order The order for which refund is initiated.
     */
    public void publishOrderRefundInitiatedEvent(OrderEntity order) {
        try {
            String orderJson = objectMapper.writeValueAsString(order);
            kafkaTemplate.send("order-refund-initiated-topic", orderJson);
            logger.info("OrderRefundInitiatedEvent published successfully for Order ID: " + order.getId());
        } catch (Exception e) {
            logger.severe("Failed to publish OrderRefundInitiatedEvent for Order ID: " + order.getId() + " Error: " + e.getMessage());
        }
    }

    /**
     * Publishes an event when an order refund is completed.
     * @param order The order for which refund is completed.
     */
    public void publishOrderRefundCompletedEvent(OrderEntity order) {
        try {
            String orderJson = objectMapper.writeValueAsString(order);
            kafkaTemplate.send("order-refund-completed-topic", orderJson);
            logger.info("OrderRefundCompletedEvent published successfully for Order ID: " + order.getId());
        } catch (Exception e) {
            logger.severe("Failed to publish OrderRefundCompletedEvent for Order ID: " + order.getId() + " Error: " + e.getMessage());
        }
    }
}