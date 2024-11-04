package application.queries;

import core.entities.ProductEntity;
import core.repositories.ProductRepository;
import application.dtos.ProductDTO;
import core.valueobjects.ProductID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Handles the query for retrieving a product by its ID.
 */
public class GetProductQueryHandler {

    private final ProductRepository productRepository;
    private static final Logger logger = LoggerFactory.getLogger(GetProductQueryHandler.class);

    @Inject
    public GetProductQueryHandler(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Retrieves the product details based on the provided ProductID.
     * 
     * @param productId The unique identifier of the product.
     * @return ProductDTO containing the product details, or null if not found.
     */
    public ProductDTO handle(ProductID productId) {
        logger.info("Handling GetProductQuery for productId: {}", productId.getValue());

        Optional<ProductEntity> productEntityOpt = productRepository.findById(productId);
        if (productEntityOpt.isPresent()) {
            logger.info("Product found for productId: {}", productId.getValue());
            return mapToDTO(productEntityOpt.get());
        } else {
            logger.warn("Product not found for productId: {}", productId.getValue());
            return null;
        }
    }

    /**
     * Maps the ProductEntity to a ProductDTO.
     * 
     * @param productEntity The entity representing the product.
     * @return A ProductDTO representing the product data.
     */
    private ProductDTO mapToDTO(ProductEntity productEntity) {
        logger.debug("Mapping ProductEntity to ProductDTO for productId: {}", productEntity.getId().getValue());
        return new ProductDTO(
            productEntity.getId().getValue(),
            productEntity.getName(),
            productEntity.getDescription(),
            productEntity.getPrice(),
            productEntity.getStockLevel()
        );
    }

    /**
     * Logs details of the product for debugging.
     * 
     * @param productDTO The DTO representing the product.
     */
    private void logProductDetails(ProductDTO productDTO) {
        logger.debug("Product details: ID: {}, Name: {}, Price: {}, Stock Level: {}",
                productDTO.getId(), productDTO.getName(), productDTO.getPrice(), productDTO.getStockLevel());
    }

    /**
     * Retrieves a list of products that match a partial name search.
     * 
     * @param name The partial name of the product.
     * @return A list of matching ProductDTOs.
     */
    public List<ProductDTO> handleSearchByName(String name) {
        logger.info("Handling search query for product name: {}", name);
        List<ProductEntity> productEntities = productRepository.findByNameContaining(name);
        List<ProductDTO> productDTOs = new ArrayList<>();

        for (ProductEntity productEntity : productEntities) {
            ProductDTO dto = mapToDTO(productEntity);
            productDTOs.add(dto);
            logProductDetails(dto);
        }

        return productDTOs;
    }

    /**
     * Updates the stock level of a product.
     * 
     * @param productId The ID of the product to update.
     * @param newStockLevel The new stock level for the product.
     * @return true if the stock level was successfully updated, false otherwise.
     */
    public boolean updateProductStock(ProductID productId, int newStockLevel) {
        logger.info("Updating stock level for productId: {} to {}", productId.getValue(), newStockLevel);
        Optional<ProductEntity> productEntityOpt = productRepository.findById(productId);

        if (productEntityOpt.isPresent()) {
            ProductEntity productEntity = productEntityOpt.get();
            productEntity.setStockLevel(newStockLevel);
            productRepository.save(productEntity);
            logger.info("Stock level updated successfully for productId: {}", productId.getValue());
            return true;
        } else {
            logger.warn("Product not found for productId: {}", productId.getValue());
            return false;
        }
    }

    /**
     * Handles deleting a product by its ID.
     * 
     * @param productId The ID of the product to delete.
     * @return true if the product was deleted successfully, false if not found.
     */
    public boolean deleteProduct(ProductID productId) {
        logger.info("Handling delete for productId: {}", productId.getValue());
        Optional<ProductEntity> productEntityOpt = productRepository.findById(productId);

        if (productEntityOpt.isPresent()) {
            productRepository.delete(productEntityOpt.get());
            logger.info("Product deleted successfully for productId: {}", productId.getValue());
            return true;
        } else {
            logger.warn("Product not found for productId: {}", productId.getValue());
            return false;
        }
    }

    /**
     * Retrieves all products that have low stock levels.
     * 
     * @param threshold The stock level threshold.
     * @return A list of ProductDTOs for products with stock levels below the threshold.
     */
    public List<ProductDTO> getLowStockProducts(int threshold) {
        logger.info("Retrieving products with stock levels below {}", threshold);
        List<ProductEntity> lowStockProducts = productRepository.findByStockLevelLessThan(threshold);
        List<ProductDTO> productDTOs = new ArrayList<>();

        for (ProductEntity productEntity : lowStockProducts) {
            ProductDTO dto = mapToDTO(productEntity);
            productDTOs.add(dto);
            logProductDetails(dto);
        }

        return productDTOs;
    }

    /**
     * Handles bulk updating of stock levels for multiple products.
     * 
     * @param stockUpdates A map of product IDs to new stock levels.
     * @return true if all updates were successful, false otherwise.
     */
    public boolean bulkUpdateStock(Map<ProductID, Integer> stockUpdates) {
        logger.info("Handling bulk stock update for {} products", stockUpdates.size());
        boolean allUpdatesSuccessful = true;

        for (Map.Entry<ProductID, Integer> entry : stockUpdates.entrySet()) {
            ProductID productId = entry.getKey();
            int newStockLevel = entry.getValue();
            boolean success = updateProductStock(productId, newStockLevel);

            if (!success) {
                logger.error("Failed to update stock for productId: {}", productId.getValue());
                allUpdatesSuccessful = false;
            }
        }

        return allUpdatesSuccessful;
    }

    /**
     * Retrieves a paginated list of products.
     * 
     * @param page The page number to retrieve.
     * @param size The number of products per page.
     * @return A list of ProductDTOs for the specified page.
     */
    public List<ProductDTO> getPaginatedProducts(int page, int size) {
        logger.info("Retrieving page {} of products with page size {}", page, size);
        List<ProductEntity> productEntities = productRepository.findPaginated(page, size);
        List<ProductDTO> productDTOs = new ArrayList<>();

        for (ProductEntity productEntity : productEntities) {
            ProductDTO dto = mapToDTO(productEntity);
            productDTOs.add(dto);
            logProductDetails(dto);
        }

        return productDTOs;
    }
}