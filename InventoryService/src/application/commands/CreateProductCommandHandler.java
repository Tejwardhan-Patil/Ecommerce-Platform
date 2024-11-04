package com.ecommerce.inventoryservice.application.commands;

import com.ecommerce.inventoryservice.core.entities.ProductEntity;
import com.ecommerce.inventoryservice.core.repositories.ProductRepository;
import com.ecommerce.inventoryservice.application.dtos.ProductDTO;
import com.ecommerce.inventoryservice.core.valueobjects.ProductID;
import com.ecommerce.inventoryservice.infrastructure.messaging.InventoryEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.UUID;
import java.util.Optional;

public class CreateProductCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(CreateProductCommandHandler.class);
    private final ProductRepository productRepository;
    private final InventoryEventPublisher eventPublisher;

    @Inject
    public CreateProductCommandHandler(ProductRepository productRepository, InventoryEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    public ProductDTO handle(CreateProductCommand command) {
        logger.info("Handling CreateProductCommand for product name: {}", command.getName());

        Optional<ProductEntity> existingProduct = productRepository.findByName(command.getName());
        if (existingProduct.isPresent()) {
            throw new IllegalArgumentException("Product with the same name already exists");
        }

        ProductID productId = new ProductID(UUID.randomUUID().toString());
        ProductEntity newProduct = new ProductEntity(productId, command.getName(), command.getDescription(), command.getPrice(), command.getStockLevel());

        productRepository.save(newProduct);
        logger.info("New product created with ID: {}", productId.getValue());

        ProductDTO productDTO = mapToProductDTO(newProduct);
        publishProductCreatedEvent(productDTO);

        return productDTO;
    }

    private void publishProductCreatedEvent(ProductDTO productDTO) {
        try {
            eventPublisher.publishProductCreatedEvent(productDTO);
            logger.info("Product created event published for product ID: {}", productDTO.getProductId());
        } catch (Exception e) {
            logger.error("Failed to publish ProductCreatedEvent for product ID: {}", productDTO.getProductId(), e);
        }
    }

    private ProductDTO mapToProductDTO(ProductEntity product) {
        return new ProductDTO(product.getProductId().getValue(), product.getName(), product.getDescription(), product.getPrice(), product.getStockLevel());
    }

    public static class CreateProductCommand {
        private final String name;
        private final String description;
        private final double price;
        private final int stockLevel;

        public CreateProductCommand(String name, String description, double price, int stockLevel) {
            this.name = name;
            this.description = description;
            this.price = price;
            this.stockLevel = stockLevel;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public double getPrice() {
            return price;
        }

        public int getStockLevel() {
            return stockLevel;
        }
    }
}