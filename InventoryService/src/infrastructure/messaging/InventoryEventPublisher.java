package infrastructure.messaging;

import core.entities.ProductEntity;
import core.entities.StockLevelEntity;
import core.valueobjects.ProductID;
import core.valueobjects.StockLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(InventoryEventPublisher.class);

    private final KafkaTemplate<String, InventoryEvent> kafkaTemplate;

    public InventoryEventPublisher(KafkaTemplate<String, InventoryEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public void publishProductCreatedEvent(ProductEntity product) {
        InventoryEvent event = new InventoryEvent();
        event.setEventType("ProductCreated");
        event.setProductID(product.getProductID().toString());
        event.setProductName(product.getName());
        event.setProductDescription(product.getDescription());
        event.setPrice(product.getPrice());
        event.setCategory(product.getCategory());
        event.setBrand(product.getBrand());

        logger.info("Publishing ProductCreated event for product ID: {}", product.getProductID());

        kafkaTemplate.send("inventory.product.created", event);
    }

    @Transactional
    public void publishStockLevelUpdatedEvent(ProductID productId, StockLevel stockLevel) {
        InventoryEvent event = new InventoryEvent();
        event.setEventType("StockLevelUpdated");
        event.setProductID(productId.toString());
        event.setStockLevel(stockLevel.getAvailableStock());

        logger.info("Publishing StockLevelUpdated event for product ID: {}", productId);

        kafkaTemplate.send("inventory.stock.updated", event);
    }

    @Transactional
    public void publishProductDeletedEvent(ProductID productId) {
        InventoryEvent event = new InventoryEvent();
        event.setEventType("ProductDeleted");
        event.setProductID(productId.toString());

        logger.info("Publishing ProductDeleted event for product ID: {}", productId);

        kafkaTemplate.send("inventory.product.deleted", event);
    }

    @Transactional
    public void publishLowStockAlert(ProductID productId, StockLevel stockLevel) {
        if (stockLevel.getAvailableStock() < stockLevel.getThreshold()) {
            InventoryEvent event = new InventoryEvent();
            event.setEventType("LowStockAlert");
            event.setProductID(productId.toString());
            event.setStockLevel(stockLevel.getAvailableStock());

            logger.warn("Publishing LowStockAlert event for product ID: {}", productId);

            kafkaTemplate.send("inventory.stock.low", event);
        }
    }

    @Transactional
    public void publishStockReplenishmentEvent(ProductID productId, StockLevelEntity stockLevelEntity) {
        InventoryEvent event = new InventoryEvent();
        event.setEventType("StockReplenished");
        event.setProductID(productId.toString());
        event.setStockLevel(stockLevelEntity.getCurrentStock());

        logger.info("Publishing StockReplenished event for product ID: {}", productId);

        kafkaTemplate.send("inventory.stock.replenished", event);
    }

    @Transactional
    public void publishStockAdjustmentEvent(ProductID productId, int adjustedStock) {
        InventoryEvent event = new InventoryEvent();
        event.setEventType("StockAdjusted");
        event.setProductID(productId.toString());
        event.setStockLevel(adjustedStock);

        logger.info("Publishing StockAdjusted event for product ID: {}", productId);

        kafkaTemplate.send("inventory.stock.adjusted", event);
    }
}

// Event Data Class
class InventoryEvent {

    private String eventType;
    private String productID;
    private String productName;
    private String productDescription;
    private double price;
    private String category;
    private String brand;
    private int stockLevel;

    // Getters and Setters

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getStockLevel() {
        return stockLevel;
    }

    public void setStockLevel(int stockLevel) {
        this.stockLevel = stockLevel;
    }
}