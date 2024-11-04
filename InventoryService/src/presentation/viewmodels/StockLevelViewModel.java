package presentation.viewmodels;

import core.entities.StockLevelEntity;
import core.valueobjects.StockLevel;
import core.repositories.StockLevelRepository;
import core.services.StockUpdateService;
import application.dtos.StockLevelDTO;
import java.util.List;
import java.util.stream.Collectors;
import java.util.logging.Logger;

/**
 * ViewModel class for managing and presenting stock levels in the inventory.
 * This ViewModel interacts with the service layer and provides data for the UI layer.
 */
public class StockLevelViewModel {

    private final StockLevelRepository stockLevelRepository;
    private final StockUpdateService stockUpdateService;
    private static final Logger logger = Logger.getLogger(StockLevelViewModel.class.getName());

    /**
     * Constructor to initialize StockLevelViewModel with required dependencies.
     *
     * @param stockLevelRepository Repository for accessing stock data.
     * @param stockUpdateService    Service for updating stock levels.
     */
    public StockLevelViewModel(StockLevelRepository stockLevelRepository, StockUpdateService stockUpdateService) {
        this.stockLevelRepository = stockLevelRepository;
        this.stockUpdateService = stockUpdateService;
    }

    /**
     * Fetches the current stock levels from the repository.
     *
     * @return List of StockLevelDTO representing the current stock levels.
     */
    public List<StockLevelDTO> getAllStockLevels() {
        List<StockLevelEntity> stockLevels = stockLevelRepository.findAll();
        return stockLevels.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Updates the stock level for a specific product.
     *
     * @param productId The product ID.
     * @param newStock  The new stock level.
     * @return boolean indicating whether the update was successful.
     */
    public boolean updateStockLevel(String productId, int newStock) {
        try {
            StockLevelEntity stockEntity = stockLevelRepository.findByProductId(productId);
            if (stockEntity != null) {
                stockEntity.setStockLevel(new StockLevel(newStock));
                stockUpdateService.updateStock(stockEntity);
                logger.info("Stock updated for product ID: " + productId + " to new level: " + newStock);
                return true;
            } else {
                logger.warning("No stock found for product ID: " + productId);
                return false;
            }
        } catch (Exception e) {
            logger.severe("Error updating stock for product ID: " + productId + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * Reduces the stock level for a specific product after an order is placed.
     *
     * @param productId The product ID.
     * @param quantity  The quantity to reduce.
     * @return boolean indicating whether the operation was successful.
     */
    public boolean reduceStockLevel(String productId, int quantity) {
        try {
            StockLevelEntity stockEntity = stockLevelRepository.findByProductId(productId);
            if (stockEntity != null && stockEntity.getStockLevel().getValue() >= quantity) {
                int newStockLevel = stockEntity.getStockLevel().getValue() - quantity;
                stockEntity.setStockLevel(new StockLevel(newStockLevel));
                stockUpdateService.updateStock(stockEntity);
                logger.info("Stock reduced for product ID: " + productId + " by quantity: " + quantity);
                return true;
            } else {
                logger.warning("Not enough stock for product ID: " + productId + " or product not found.");
                return false;
            }
        } catch (Exception e) {
            logger.severe("Error reducing stock for product ID: " + productId + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * Increases the stock level for a specific product (after restocking).
     *
     * @param productId The product ID.
     * @param quantity  The quantity to increase.
     * @return boolean indicating whether the operation was successful.
     */
    public boolean increaseStockLevel(String productId, int quantity) {
        try {
            StockLevelEntity stockEntity = stockLevelRepository.findByProductId(productId);
            if (stockEntity != null) {
                int newStockLevel = stockEntity.getStockLevel().getValue() + quantity;
                stockEntity.setStockLevel(new StockLevel(newStockLevel));
                stockUpdateService.updateStock(stockEntity);
                logger.info("Stock increased for product ID: " + productId + " by quantity: " + quantity);
                return true;
            } else {
                logger.warning("No stock found for product ID: " + productId);
                return false;
            }
        } catch (Exception e) {
            logger.severe("Error increasing stock for product ID: " + productId + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * Converts a StockLevelEntity to a StockLevelDTO.
     *
     * @param entity The StockLevelEntity.
     * @return StockLevelDTO representing the stock level data.
     */
    private StockLevelDTO convertToDTO(StockLevelEntity entity) {
        return new StockLevelDTO(entity.getProductId(), entity.getStockLevel().getValue());
    }

    /**
     * Fetches stock level for a specific product by its ID.
     *
     * @param productId The product ID.
     * @return StockLevelDTO representing the stock level.
     */
    public StockLevelDTO getStockLevelByProductId(String productId) {
        StockLevelEntity stockEntity = stockLevelRepository.findByProductId(productId);
        if (stockEntity != null) {
            return convertToDTO(stockEntity);
        } else {
            logger.warning("No stock found for product ID: " + productId);
            return null;
        }
    }

    /**
     * Retrieves the stock status as a human-readable string.
     *
     * @param stockLevel The stock level.
     * @return String representing the stock status.
     */
    public String getStockStatus(int stockLevel) {
        if (stockLevel > 50) {
            return "In Stock";
        } else if (stockLevel > 10) {
            return "Low Stock";
        } else {
            return "Out of Stock";
        }
    }

    /**
     * Monitors stock levels to ensure they're within acceptable ranges.
     */
    public void monitorStockLevels() {
        List<StockLevelDTO> stockLevels = getAllStockLevels();
        stockLevels.forEach(stockLevelDTO -> {
            String stockStatus = getStockStatus(stockLevelDTO.getStockLevel());
            logger.info("Product ID: " + stockLevelDTO.getProductId() + " - Stock Status: " + stockStatus);
        });
    }
}