package presentation.viewmodels;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * ViewModel representing the Product details for presentation layer.
 */
public class ProductViewModel {

    private String productId;
    private String name;
    private String description;
    private BigDecimal price;
    private int availableStock;
    private List<String> categories;
    private boolean isActive;

    public ProductViewModel(String productId, String name, String description, BigDecimal price, int availableStock, List<String> categories, boolean isActive) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.availableStock = availableStock;
        this.categories = categories;
        this.isActive = isActive;
    }

    // Getters and Setters

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(int availableStock) {
        this.availableStock = availableStock;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductViewModel that = (ProductViewModel) o;
        return availableStock == that.availableStock &&
                isActive == that.isActive &&
                Objects.equals(productId, that.productId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(price, that.price) &&
                Objects.equals(categories, that.categories);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, name, description, price, availableStock, categories, isActive);
    }

    @Override
    public String toString() {
        return "ProductViewModel{" +
                "productId='" + productId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", availableStock=" + availableStock +
                ", categories=" + categories +
                ", isActive=" + isActive +
                '}';
    }

    // Utility methods

    /**
     * Update the stock level of the product.
     * @param newStockLevel New stock level to be updated.
     */
    public void updateStock(int newStockLevel) {
        this.availableStock = newStockLevel;
    }

    /**
     * Check if the product is in stock.
     * @return true if product is available, otherwise false.
     */
    public boolean isInStock() {
        return this.availableStock > 0;
    }

    /**
     * Check if the product belongs to a specific category.
     * @param category Category to check.
     * @return true if product belongs to the category, otherwise false.
     */
    public boolean belongsToCategory(String category) {
        return this.categories.contains(category);
    }

    /**
     * Toggle product availability.
     */
    public void toggleAvailability() {
        this.isActive = !this.isActive;
    }

    /**
     * Apply a discount to the product price.
     * @param discountRate Discount rate in percentage (10 for 10% discount).
     */
    public void applyDiscount(double discountRate) {
        BigDecimal discountAmount = this.price.multiply(BigDecimal.valueOf(discountRate / 100));
        this.price = this.price.subtract(discountAmount);
    }

    /**
     * Revert the discount and restore the original price.
     * @param originalPrice The original price of the product before the discount.
     */
    public void revertDiscount(BigDecimal originalPrice) {
        this.price = originalPrice;
    }

    /**
     * Mark the product as featured.
     * This method is used to highlight a product for special promotions.
     */
    public void markAsFeatured() {
        logger.info("Marking the product as featured...");

        try {
            // Set the featured flag
            setFeaturedFlag(true);

            // Update metadata (such as promotion timestamp)
            updateFeaturedMetadata();

            // Notify other services (notify the promotion team, update search index)
            notifyServices();

            logger.info("Product marked as featured successfully.");
        } catch (Exception e) {
            logger.error("Failed to mark the product as featured: {}", e.getMessage());
        }
    }

    /**
     * Set the featured flag for the product.
     * @param isFeatured boolean value indicating whether the product is featured.
     */
    private void setFeaturedFlag(boolean isFeatured) {
        this.isFeatured = isFeatured;
        logger.info("Featured flag set to: {}", isFeatured);
    }

    /**
     * Update any necessary metadata related to featuring the product.
     */
    private void updateFeaturedMetadata() {
        // Simulate updating metadata, setting the date and time when the product was featured
        this.featuredDate = LocalDateTime.now();
        logger.info("Updated product metadata: Featured date set to {}", featuredDate);
    }

    /**
     * Validate if the product has all the required details.
     * @return true if valid, otherwise false.
     */
    public boolean validate() {
        return this.productId != null && !this.productId.isEmpty() &&
               this.name != null && !this.name.isEmpty() &&
               this.price != null && this.price.compareTo(BigDecimal.ZERO) > 0 &&
               this.availableStock >= 0;
    }
}