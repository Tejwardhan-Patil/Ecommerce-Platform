package application.commands;

import core.entities.ProductEntity;
import core.entities.StockLevelEntity;
import core.repositories.ProductRepository;
import core.repositories.StockLevelRepository;
import core.services.StockUpdateService;
import infrastructure.messaging.InventoryEventPublisher;
import application.dtos.StockLevelDTO;
import valueobjects.ProductID;
import valueobjects.StockLevel;

import java.util.Optional;

public class UpdateStockCommandHandler {

    private final ProductRepository productRepository;
    private final StockLevelRepository stockLevelRepository;
    private final StockUpdateService stockUpdateService;
    private final InventoryEventPublisher inventoryEventPublisher;

    public UpdateStockCommandHandler(ProductRepository productRepository,
                                     StockLevelRepository stockLevelRepository,
                                     StockUpdateService stockUpdateService,
                                     InventoryEventPublisher inventoryEventPublisher) {
        this.productRepository = productRepository;
        this.stockLevelRepository = stockLevelRepository;
        this.stockUpdateService = stockUpdateService;
        this.inventoryEventPublisher = inventoryEventPublisher;
    }

    public void handle(UpdateStockCommand command) {
        // Validate the product
        Optional<ProductEntity> productOptional = productRepository.findById(new ProductID(command.getProductId()));
        if (!productOptional.isPresent()) {
            throw new IllegalArgumentException("Product not found for ID: " + command.getProductId());
        }

        ProductEntity product = productOptional.get();
        // Load existing stock level
        Optional<StockLevelEntity> stockLevelOptional = stockLevelRepository.findByProductId(new ProductID(command.getProductId()));
        StockLevelEntity stockLevel;

        if (stockLevelOptional.isPresent()) {
            stockLevel = stockLevelOptional.get();
        } else {
            // Initialize stock level if it does not exist
            stockLevel = new StockLevelEntity(new ProductID(command.getProductId()), new StockLevel(0));
        }

        // Perform stock update
        StockLevel updatedStockLevel = stockUpdateService.updateStock(stockLevel.getStockLevel(), new StockLevel(command.getNewQuantity()));

        // Update stock level entity
        stockLevel.setStockLevel(updatedStockLevel);
        stockLevelRepository.save(stockLevel);

        // Publish stock update event
        inventoryEventPublisher.publishStockUpdatedEvent(stockLevel.toDTO());
    }

    // Inner class to represent the command
    public static class UpdateStockCommand {
        private final String productId;
        private final int newQuantity;

        public UpdateStockCommand(String productId, int newQuantity) {
            this.productId = productId;
            this.newQuantity = newQuantity;
        }

        public String getProductId() {
            return productId;
        }

        public int getNewQuantity() {
            return newQuantity;
        }
    }
}