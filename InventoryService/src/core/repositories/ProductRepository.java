package core.repositories;

import com.website.inventory.core.entities.ProductEntity;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    /**
     * Saves a product entity to the database.
     * 
     * @param product The product entity to save.
     * @return The saved product entity.
     */
    ProductEntity save(ProductEntity product);

    /**
     * Finds a product entity by its unique identifier.
     * 
     * @param id The unique identifier of the product.
     * @return An optional containing the product entity if found, or empty if not found.
     */
    Optional<ProductEntity> findById(Long id);

    /**
     * Finds a product entity by its name.
     * 
     * @param name The name of the product.
     * @return A list of product entities with matching names.
     */
    List<ProductEntity> findByName(String name);

    /**
     * Finds all products in the repository.
     * 
     * @return A list of all product entities.
     */
    List<ProductEntity> findAll();

    /**
     * Finds all products by their category.
     * 
     * @param category The category of the products.
     * @return A list of product entities in the given category.
     */
    List<ProductEntity> findByCategory(String category);

    /**
     * Deletes a product entity from the repository.
     * 
     * @param product The product entity to delete.
     */
    void delete(ProductEntity product);

    /**
     * Deletes a product entity by its unique identifier.
     * 
     * @param id The unique identifier of the product.
     */
    void deleteById(Long id);

    /**
     * Updates stock quantity for a specific product.
     * 
     * @param id The unique identifier of the product.
     * @param quantity The new stock quantity to set.
     */
    void updateStock(Long id, int quantity);

    /**
     * Finds products that have a stock quantity below a certain threshold.
     * 
     * @param threshold The stock quantity threshold.
     * @return A list of product entities with stock quantity below the given threshold.
     */
    List<ProductEntity> findByStockBelow(int threshold);

    /**
     * Finds products that were recently added or updated in the inventory.
     * 
     * @return A list of recently added or updated product entities.
     */
    List<ProductEntity> findRecent();

    /**
     * Checks if a product exists by its unique identifier.
     * 
     * @param id The unique identifier of the product.
     * @return True if the product exists, false otherwise.
     */
    boolean existsById(Long id);

    /**
     * Checks if a product exists by its name.
     * 
     * @param name The name of the product.
     * @return True if the product exists, false otherwise.
     */
    boolean existsByName(String name);

    /**
     * Finds products with a price within a certain range.
     * 
     * @param minPrice The minimum price.
     * @param maxPrice The maximum price.
     * @return A list of product entities within the given price range.
     */
    List<ProductEntity> findByPriceRange(Double minPrice, Double maxPrice);

    /**
     * Finds products that are currently out of stock.
     * 
     * @return A list of product entities that are out of stock.
     */
    List<ProductEntity> findOutOfStock();

    /**
     * Increments stock quantity for a specific product.
     * 
     * @param id The unique identifier of the product.
     * @param amount The amount to increment the stock by.
     */
    void incrementStock(Long id, int amount);

    /**
     * Decrements stock quantity for a specific product.
     * 
     * @param id The unique identifier of the product.
     * @param amount The amount to decrement the stock by.
     */
    void decrementStock(Long id, int amount);

    /**
     * Finds products by their manufacturer.
     * 
     * @param manufacturer The manufacturer of the products.
     * @return A list of product entities made by the given manufacturer.
     */
    List<ProductEntity> findByManufacturer(String manufacturer);

    /**
     * Finds products that are on sale.
     * 
     * @return A list of product entities that are currently on sale.
     */
    List<ProductEntity> findOnSale();

    /**
     * Updates the price of a specific product.
     * 
     * @param id The unique identifier of the product.
     * @param newPrice The new price to set for the product.
     */
    void updatePrice(Long id, Double newPrice);

    /**
     * Marks a product as discontinued.
     * 
     * @param id The unique identifier of the product.
     */
    void markAsDiscontinued(Long id);

    /**
     * Finds products that are discontinued.
     * 
     * @return A list of product entities that are discontinued.
     */
    List<ProductEntity> findDiscontinued();

    /**
     * Updates the product details.
     * 
     * @param product The product entity with updated details.
     */
    void updateProductDetails(ProductEntity product);

    /**
     * Finds products by a keyword in their description.
     * 
     * @param keyword The keyword to search in product descriptions.
     * @return A list of product entities that contain the keyword in their description.
     */
    List<ProductEntity> findByDescriptionKeyword(String keyword);

    /**
     * Finds products that are new arrivals.
     * 
     * @return A list of product entities that are considered new arrivals.
     */
    List<ProductEntity> findNewArrivals();

    /**
     * Finds the best-selling products.
     * 
     * @return A list of best-selling product entities.
     */
    List<ProductEntity> findBestSellers();

    /**
     * Finds the most viewed products.
     * 
     * @return A list of the most viewed product entities.
     */
    List<ProductEntity> findMostViewed();

    /**
     * Updates the number of views for a specific product.
     * 
     * @param id The unique identifier of the product.
     * @param views The new number of views to set.
     */
    void updateViews(Long id, int views);

    /**
     * Adds a product rating for a specific product.
     * 
     * @param id The unique identifier of the product.
     * @param rating The rating to add.
     */
    void addRating(Long id, Double rating);

    /**
     * Finds the top-rated products.
     * 
     * @return A list of the top-rated product entities.
     */
    List<ProductEntity> findTopRated();

    /**
     * Finds products by their supplier.
     * 
     * @param supplier The supplier of the products.
     * @return A list of product entities supplied by the given supplier.
     */
    List<ProductEntity> findBySupplier(String supplier);

    /**
     * Updates the supplier information for a specific product.
     * 
     * @param id The unique identifier of the product.
     * @param supplier The new supplier information to set.
     */
    void updateSupplier(Long id, String supplier);
}