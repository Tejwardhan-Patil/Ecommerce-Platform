package com.website.eventbus.publishers;

import com.website.eventbus.events.PaymentCompletedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PaymentCompletedEventPublisher is responsible for publishing payment completion events 
 * to the Kafka topic.
 */
@Component
public class PaymentCompletedEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(PaymentCompletedEventPublisher.class);
    private static final String TOPIC = "payment-completed";

    private final KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;

    @Autowired
    public PaymentCompletedEventPublisher(KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publish a payment completed event.
     * 
     * @param event The event containing payment details.
     */
    public void publishEvent(PaymentCompletedEvent event) {
        logger.info("Publishing payment completed event for Order ID: {}", event.getOrderId());
        try {
            kafkaTemplate.send(TOPIC, event);
            logger.info("Payment completed event for Order ID: {} published successfully.", event.getOrderId());
        } catch (Exception e) {
            logger.error("Error publishing payment completed event for Order ID: {}", event.getOrderId(), e);
        }
    }
}