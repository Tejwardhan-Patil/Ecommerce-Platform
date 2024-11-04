package publishers;

import java.util.UUID;
import java.util.logging.Logger;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.errors.TimeoutException;
import java.util.Properties;

class OrderPlacedEvent {
    private String orderId;
    private String userId;
    private List<String> productIds;
    private LocalDateTime orderDate;
    private String orderStatus;

    public OrderPlacedEvent(String orderId, String userId, List<String> productIds, LocalDateTime orderDate, String orderStatus) {
        this.orderId = orderId;
        this.userId = userId;
        this.productIds = productIds;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getUserId() {
        return userId;
    }

    public List<String> getProductIds() {
        return productIds;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    @Override
    public String toString() {
        return "OrderPlacedEvent{" +
                "orderId='" + orderId + '\'' +
                ", userId='" + userId + '\'' +
                ", productIds=" + productIds +
                ", orderDate=" + orderDate +
                ", orderStatus='" + orderStatus + '\'' +
                '}';
    }
}

// Publisher class for publishing the OrderPlacedEvent to the event bus (Kafka)
public class OrderPlacedEventPublisher {
    private static final Logger logger = Logger.getLogger(OrderPlacedEventPublisher.class.getName());
    private KafkaProducer<String, String> producer;
    private ObjectMapper objectMapper = new ObjectMapper();
    private ExecutorService executorService;

    public OrderPlacedEventPublisher() {
        this.producer = createKafkaProducer();
        this.executorService = Executors.newFixedThreadPool(10);  // Create a thread pool for asynchronous publishing
    }

    // Create the Kafka producer with appropriate configurations
    private KafkaProducer<String, String> createKafkaProducer() {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.RETRIES_CONFIG, "10");
        properties.put(ProducerConfig.LINGER_MS_CONFIG, "1");

        return new KafkaProducer<>(properties);
    }

    // Method to publish an OrderPlacedEvent
    public CompletableFuture<RecordMetadata> publishOrderPlacedEvent(OrderPlacedEvent event) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String key = UUID.randomUUID().toString();
                String value = objectMapper.writeValueAsString(event);
                ProducerRecord<String, String> record = new ProducerRecord<>("OrderPlacedEvents", key, value);
                RecordMetadata metadata = producer.send(record).get();
                logger.info("Published OrderPlacedEvent: " + event.toString() + " with metadata: " + metadata.toString());
                return metadata;
            } catch (Exception e) {
                logger.severe("Failed to publish OrderPlacedEvent: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    // Method to gracefully shut down the publisher and producer
    public void shutdown() {
        try {
            producer.flush();
            producer.close();
            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
            logger.info("OrderPlacedEventPublisher has been shutdown gracefully.");
        } catch (InterruptedException e) {
            logger.severe("Shutdown interrupted: " + e.getMessage());
        }
    }

    // Main method to simulate publishing events
    public static void main(String[] args) {
        OrderPlacedEventPublisher eventPublisher = new OrderPlacedEventPublisher();
        
        // Event creation
        List<String> productIds = new ArrayList<>();
        productIds.add("prod-123");
        productIds.add("prod-456");
        OrderPlacedEvent event = new OrderPlacedEvent(
            UUID.randomUUID().toString(),
            "user-789",
            productIds,
            LocalDateTime.now(),
            "PLACED"
        );

        // Publish the event
        CompletableFuture<RecordMetadata> future = eventPublisher.publishOrderPlacedEvent(event);
        future.whenComplete((metadata, ex) -> {
            if (ex != null) {
                System.out.println("Failed to publish event: " + ex.getMessage());
            } else {
                System.out.println("Event published successfully, metadata: " + metadata.toString());
            }
        });

        // Shutdown the publisher after publishing
        eventPublisher.shutdown();
    }
}