package core.services;

import core.entities.ProductEntity;
import core.entities.StockLevelEntity;
import core.repositories.ProductRepository;
import core.repositories.StockLevelRepository;
import valueobjects.ProductID;
import valueobjects.StockLevel;
import infrastructure.messaging.InventoryEventPublisher;
import infrastructure.messaging.StockUpdateEventSubscriber;
import java.util.Optional;
import java.util.logging.Logger;
import javax.transaction.Transactional;

public class StockUpdateService {

    private final ProductRepository productRepository;
    private final StockLevelRepository stockLevelRepository;
    private final InventoryEventPublisher eventPublisher;
    private final StockUpdateEventSubscriber eventSubscriber;
    private static final Logger LOGGER = Logger.getLogger(StockUpdateService.class.getName());

    public StockUpdateService(
        ProductRepository productRepository,
        StockLevelRepository stockLevelRepository,
        InventoryEventPublisher eventPublisher,
        StockUpdateEventSubscriber eventSubscriber
    ) {
        this.productRepository = productRepository;
        this.stockLevelRepository = stockLevelRepository;
        this.eventPublisher = eventPublisher;
        this.eventSubscriber = eventSubscriber;
    }

    @Transactional
    public void updateStock(ProductID productId, int quantity) {
        Optional<ProductEntity> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            ProductEntity product = productOpt.get();
            StockLevelEntity stockLevel = stockLevelRepository.findByProduct(product);
            stockLevel.updateQuantity(quantity);
            stockLevelRepository.save(stockLevel);
            LOGGER.info("Stock updated for product: " + productId);
            eventPublisher.publishStockUpdateEvent(productId, quantity);
        } else {
            throw new IllegalArgumentException("Product with ID " + productId + " not found.");
        }
    }

    @Transactional
    public void handleStockUpdateEvent(ProductID productId, int quantity) {
        eventSubscriber.subscribeToStockUpdateEvents((event) -> {
            if (event.getProductId().equals(productId)) {
                updateStock(event.getProductId(), event.getQuantity());
            }
        });
    }

    public StockLevel checkStock(ProductID productId) {
        Optional<ProductEntity> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            StockLevelEntity stockLevel = stockLevelRepository.findByProduct(productOpt.get());
            return new StockLevel(stockLevel.getQuantity());
        } else {
            throw new IllegalArgumentException("Product with ID " + productId + " not found.");
        }
    }

    public void adjustStockForMultipleProducts(ProductID[] productIds, int[] quantities) {
        if (productIds.length != quantities.length) {
            throw new IllegalArgumentException("Product IDs and quantities length mismatch.");
        }

        for (int i = 0; i < productIds.length; i++) {
            updateStock(productIds[i], quantities[i]);
        }
    }

    public boolean hasSufficientStock(ProductID productId, int requiredQuantity) {
        Optional<ProductEntity> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            StockLevelEntity stockLevel = stockLevelRepository.findByProduct(productOpt.get());
            return stockLevel.getQuantity() >= requiredQuantity;
        } else {
            throw new IllegalArgumentException("Product with ID " + productId + " not found.");
        }
    }

    public void reserveStock(ProductID productId, int quantity) {
        if (!hasSufficientStock(productId, quantity)) {
            throw new IllegalStateException("Insufficient stock for product: " + productId);
        }

        updateStock(productId, -quantity);
    }

    public void releaseStock(ProductID productId, int quantity) {
        updateStock(productId, quantity);
    }

    public void batchStockUpdate(StockLevelEntity[] stockLevelEntities) {
        for (StockLevelEntity stock : stockLevelEntities) {
            stockLevelRepository.save(stock);
            eventPublisher.publishStockUpdateEvent(stock.getProduct().getProductId(), stock.getQuantity());
            LOGGER.info("Batch stock update performed for product: " + stock.getProduct().getProductId());
        }
    }

    public int getTotalAvailableStock(ProductID productId) {
        Optional<ProductEntity> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            StockLevelEntity stockLevel = stockLevelRepository.findByProduct(productOpt.get());
            return stockLevel.getQuantity();
        } else {
            throw new IllegalArgumentException("Product with ID " + productId + " not found.");
        }
    }

    public void reconcileStock() {
        Iterable<StockLevelEntity> allStockLevels = stockLevelRepository.findAll();
        for (StockLevelEntity stockLevel : allStockLevels) {
            int actualStock = checkExternalStock(stockLevel.getProduct().getProductId());
            if (stockLevel.getQuantity() != actualStock) {
                stockLevel.updateQuantity(actualStock);
                stockLevelRepository.save(stockLevel);
                eventPublisher.publishStockUpdateEvent(stockLevel.getProduct().getProductId(), actualStock);
                LOGGER.info("Reconciled stock for product: " + stockLevel.getProduct().getProductId());
            }
        }
    }

    private int checkExternalStock(ProductID productId) {
        // Call to external system for stock verification
        return 100;
    }

    public void restockFromExternalSupplier(ProductID productId, int quantity) {
        updateStock(productId, quantity);
        LOGGER.info("Restocked product from external supplier: " + productId + " by quantity: " + quantity);
    }

    public void scheduleStockUpdate(ProductID productId, int quantity, String cronExpression) {
        // Logic for scheduling a stock update using the cronExpression
        LOGGER.info("Scheduled stock update for product: " + productId + " with cron: " + cronExpression);
    }

    public void subscribeToLowStockAlert(ProductID productId, int threshold) {
        eventSubscriber.subscribeToStockUpdateEvents(event -> {
            if (event.getProductId().equals(productId) && event.getQuantity() <= threshold) {
                LOGGER.warning("Low stock alert for product: " + productId + ". Remaining stock: " + event.getQuantity());
            }
        });
    }

    public void handleReturnStock(ProductID productId, int quantity) {
        releaseStock(productId, quantity);
        LOGGER.info("Returned stock for product: " + productId + " with quantity: " + quantity);
    }
}