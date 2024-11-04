package core.services;

import core.entities.ProductEntity;
import core.entities.StockLevelEntity;
import core.repositories.ProductRepository;
import core.repositories.StockLevelRepository;
import core.valueobjects.ProductID;
import core.valueobjects.StockLevel;
import java.util.List;
import java.util.Optional;

public class InventoryService {

    private final ProductRepository productRepository;
    private final StockLevelRepository stockLevelRepository;

    public InventoryService(ProductRepository productRepository, StockLevelRepository stockLevelRepository) {
        this.productRepository = productRepository;
        this.stockLevelRepository = stockLevelRepository;
    }

    // Adds a new product to the inventory
    public ProductEntity addProduct(ProductEntity product) {
        return productRepository.save(product);
    }

    // Retrieves a product by its ID
    public Optional<ProductEntity> getProductById(ProductID productId) {
        return productRepository.findById(productId);
    }

    // Updates stock level for a given product
    public void updateStockLevel(ProductID productId, int newStock) {
        Optional<ProductEntity> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            ProductEntity product = productOpt.get();
            StockLevelEntity stockLevel = stockLevelRepository.findByProductId(productId)
                .orElse(new StockLevelEntity(productId, new StockLevel(0)));
            stockLevel.setStockLevel(new StockLevel(newStock));
            stockLevelRepository.save(stockLevel);
        } else {
            throw new RuntimeException("Product not found for the given ID");
        }
    }

    // Returns all products
    public List<ProductEntity> getAllProducts() {
        return productRepository.findAll();
    }

    // Removes a product from the inventory
    public void removeProduct(ProductID productId) {
        productRepository.deleteById(productId);
    }

    // Checks stock level for a product
    public int checkStockLevel(ProductID productId) {
        Optional<StockLevelEntity> stockLevelOpt = stockLevelRepository.findByProductId(productId);
        return stockLevelOpt.map(stockLevel -> stockLevel.getStockLevel().getQuantity()).orElse(0);
    }

    // Handles bulk stock update
    public void updateStockLevels(List<ProductID> productIds, List<Integer> stockQuantities) {
        if (productIds.size() != stockQuantities.size()) {
            throw new IllegalArgumentException("Product IDs and Stock Quantities list size mismatch");
        }

        for (int i = 0; i < productIds.size(); i++) {
            updateStockLevel(productIds.get(i), stockQuantities.get(i));
        }
    }

    // Retrieves products below a stock threshold
    public List<ProductEntity> getProductsBelowStockThreshold(int threshold) {
        return productRepository.findByStockLevelLessThan(threshold);
    }

    // Handles product restocking
    public void restockProduct(ProductID productId, int additionalStock) {
        Optional<ProductEntity> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            StockLevelEntity stockLevel = stockLevelRepository.findByProductId(productId)
                .orElse(new StockLevelEntity(productId, new StockLevel(0)));
            int currentStock = stockLevel.getStockLevel().getQuantity();
            stockLevel.setStockLevel(new StockLevel(currentStock + additionalStock));
            stockLevelRepository.save(stockLevel);
        } else {
            throw new RuntimeException("Product not found for restocking");
        }
    }

    // List products with a specific stock level
    public List<ProductEntity> getProductsByStockLevel(int stockLevel) {
        return productRepository.findByStockLevel(stockLevel);
    }

    // Get product stock details
    public StockLevelEntity getStockDetails(ProductID productId) {
        return stockLevelRepository.findByProductId(productId)
            .orElseThrow(() -> new RuntimeException("Stock information not found for the given product"));
    }

    // Sets a specific stock level
    public void setStockLevel(ProductID productId, int stockLevel) {
        StockLevelEntity stockLevelEntity = stockLevelRepository.findByProductId(productId)
            .orElse(new StockLevelEntity(productId, new StockLevel(0)));
        stockLevelEntity.setStockLevel(new StockLevel(stockLevel));
        stockLevelRepository.save(stockLevelEntity);
    }

    // Check if the product is out of stock
    public boolean isProductOutOfStock(ProductID productId) {
        return checkStockLevel(productId) == 0;
    }

    // Process inventory transactions
    public void processTransaction(ProductID productId, int quantitySold) {
        StockLevelEntity stockLevel = stockLevelRepository.findByProductId(productId)
            .orElseThrow(() -> new RuntimeException("Stock information not found"));
        int currentStock = stockLevel.getStockLevel().getQuantity();
        if (currentStock < quantitySold) {
            throw new RuntimeException("Not enough stock available");
        }
        stockLevel.setStockLevel(new StockLevel(currentStock - quantitySold));
        stockLevelRepository.save(stockLevel);
    }

    // Generates inventory reports for products
    public String generateInventoryReport() {
        List<ProductEntity> products = productRepository.findAll();
        StringBuilder report = new StringBuilder("Inventory Report:\n");
        for (ProductEntity product : products) {
            StockLevelEntity stockLevel = stockLevelRepository.findByProductId(product.getId())
                .orElse(new StockLevelEntity(product.getId(), new StockLevel(0)));
            report.append("Product: ").append(product.getName())
                .append(", Stock: ").append(stockLevel.getStockLevel().getQuantity()).append("\n");
        }
        return report.toString();
    }
}