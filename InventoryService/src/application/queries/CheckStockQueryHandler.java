package com.website.inventoryservice.application.queries;

import com.website.inventoryservice.core.entities.ProductEntity;
import com.website.inventoryservice.core.entities.StockLevelEntity;
import com.website.inventoryservice.core.repositories.ProductRepository;
import com.website.inventoryservice.core.repositories.StockLevelRepository;
import com.website.inventoryservice.application.dtos.StockLevelDTO;
import com.website.inventoryservice.core.valueobjects.ProductID;
import com.website.inventoryservice.application.exceptions.ProductNotFoundException;
import com.website.inventoryservice.application.exceptions.StockLevelNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.List;

/**
 * Query handler responsible for checking stock levels for a product.
 */
public class CheckStockQueryHandler {

    private final ProductRepository productRepository;
    private final StockLevelRepository stockLevelRepository;
    private static final Logger logger = LoggerFactory.getLogger(CheckStockQueryHandler.class);

    public CheckStockQueryHandler(ProductRepository productRepository, StockLevelRepository stockLevelRepository) {
        this.productRepository = productRepository;
        this.stockLevelRepository = stockLevelRepository;
    }

    /**
     * Method to handle the query for checking the stock of a product.
     *
     * @param productID The ID of the product to check stock for.
     * @return StockLevelDTO containing stock information.
     * @throws ProductNotFoundException if product not found.
     * @throws StockLevelNotFoundException if stock information is not found.
     */
    public StockLevelDTO handle(ProductID productID) throws ProductNotFoundException, StockLevelNotFoundException {
        logger.info("Handling check stock query for productID: {}", productID);

        validateProductID(productID);

        Optional<ProductEntity> productEntityOpt = productRepository.findById(productID);
        if (!productEntityOpt.isPresent()) {
            logger.error("Product with ID {} not found", productID);
            throw new ProductNotFoundException("Product with ID " + productID + " not found");
        }

        ProductEntity productEntity = productEntityOpt.get();
        logger.info("Product found: {}", productEntity.getProductName());

        Optional<StockLevelEntity> stockLevelEntityOpt = stockLevelRepository.findByProductID(productID);
        if (!stockLevelEntityOpt.isPresent()) {
            logger.error("Stock level for product with ID {} not found", productID);
            throw new StockLevelNotFoundException("Stock level for product with ID " + productID + " not found");
        }

        StockLevelEntity stockLevelEntity = stockLevelEntityOpt.get();
        logger.info("Stock level found: {}", stockLevelEntity.getStockLevel());

        return new StockLevelDTO(productID, productEntity.getProductName(), stockLevelEntity.getStockLevel());
    }

    /**
     * Handles stock check asynchronously.
     *
     * @param productID The ID of the product to check stock for.
     * @return CompletableFuture containing StockLevelDTO.
     */
    public CompletableFuture<StockLevelDTO> handleAsync(ProductID productID) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return handle(productID);
            } catch (ProductNotFoundException | StockLevelNotFoundException e) {
                logger.error("Error handling async stock check: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Batch method to check stock levels for a list of products.
     *
     * @param productIDs List of ProductID.
     * @return List of StockLevelDTO containing stock information.
     */
    public List<StockLevelDTO> handleBatch(List<ProductID> productIDs) {
        logger.info("Handling batch check stock for products: {}", productIDs);

        return productIDs.stream()
                .map(this::handleSilently)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    /**
     * Silently handles stock checking, ignoring exceptions for batch operations.
     *
     * @param productID The ID of the product to check stock for.
     * @return Optional StockLevelDTO or empty if product/stock not found.
     */
    private Optional<StockLevelDTO> handleSilently(ProductID productID) {
        try {
            return Optional.of(handle(productID));
        } catch (ProductNotFoundException | StockLevelNotFoundException e) {
            logger.warn("Skipping productID {} due to error: {}", productID, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Validates that the ProductID is valid.
     *
     * @param productID The product ID to validate.
     */
    private void validateProductID(ProductID productID) {
        if (productID == null || productID.getId() == null || productID.getId().isEmpty()) {
            logger.error("Invalid ProductID: {}", productID);
            throw new IllegalArgumentException("ProductID cannot be null or empty.");
        }
    }

    /**
     * Demonstrates usage of handleAsync to check stock asynchronously.
     */
    public void demonstrateAsyncHandling(ProductID productID) {
        CompletableFuture<StockLevelDTO> future = handleAsync(productID);
        future.thenAccept(stockLevelDTO -> logger.info("Async Stock Level: {}", stockLevelDTO));

        // Exception handling in async context
        future.exceptionally(ex -> {
            logger.error("Error occurred during async stock check: {}", ex.getMessage());
            return null;
        });

        // Waiting for completion
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Async operation interrupted or failed: {}", e.getMessage());
        }
    }

    /**
     * Method to handle the query for checking the stock of a product and log performance metrics.
     *
     * @param productID The ID of the product to check stock for.
     * @return StockLevelDTO containing stock information.
     * @throws ProductNotFoundException if product not found.
     * @throws StockLevelNotFoundException if stock information is not found.
     */
    public StockLevelDTO handleWithMetrics(ProductID productID) throws ProductNotFoundException, StockLevelNotFoundException {
        long startTime = System.currentTimeMillis();
        logger.info("Starting check stock with metrics for productID: {}", productID);

        StockLevelDTO stockLevelDTO = handle(productID);

        long endTime = System.currentTimeMillis();
        logger.info("Stock check completed in {} ms for productID: {}", (endTime - startTime), productID);

        return stockLevelDTO;
    }

    /**
     * Additional helper method to handle bulk stock updates, potentially for flash sale scenarios.
     *
     * @param productID The product ID to check and update stock.
     * @param quantity The quantity to deduct from the current stock level.
     * @return Updated StockLevelDTO after deducting the quantity.
     * @throws StockLevelNotFoundException if stock information is not found.
     */
    public StockLevelDTO handleStockDeduction(ProductID productID, int quantity) throws StockLevelNotFoundException {
        logger.info("Handling stock deduction for productID: {} with quantity: {}", productID, quantity);

        Optional<StockLevelEntity> stockLevelEntityOpt = stockLevelRepository.findByProductID(productID);
        if (!stockLevelEntityOpt.isPresent()) {
            logger.error("Stock level for product with ID {} not found", productID);
            throw new StockLevelNotFoundException("Stock level for product with ID " + productID + " not found");
        }

        StockLevelEntity stockLevelEntity = stockLevelEntityOpt.get();
        int updatedStock = stockLevelEntity.getStockLevel() - quantity;

        if (updatedStock < 0) {
            logger.warn("Insufficient stock for productID: {}", productID);
            throw new IllegalStateException("Insufficient stock for productID: " + productID);
        }

        stockLevelEntity.setStockLevel(updatedStock);
        stockLevelRepository.save(stockLevelEntity);

        logger.info("Stock deduction successful, updated stock: {}", updatedStock);
        return new StockLevelDTO(productID, stockLevelEntity.getProduct().getProductName(), updatedStock);
    }
}