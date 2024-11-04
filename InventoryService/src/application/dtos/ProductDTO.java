package application.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Data Transfer Object for Product
 */
public class ProductDTO {

    private String productId;
    private String name;
    private String description;
    private BigDecimal price;
    private int stockQuantity;
    private LocalDate dateAdded;
    private String category;
    private String brand;
    private String supplier;
    private boolean active;
    private BigDecimal discount;

    public ProductDTO() {
        // Default constructor
    }

    public ProductDTO(String productId, String name, String description, BigDecimal price, 
                      int stockQuantity, LocalDate dateAdded, String category, 
                      String brand, String supplier, boolean active, BigDecimal discount) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.dateAdded = dateAdded;
        this.category = category;
        this.brand = brand;
        this.supplier = supplier;
        this.active = active;
        this.discount = discount;
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

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public LocalDate getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(LocalDate dateAdded) {
        this.dateAdded = dateAdded;
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

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    // Override equals and hashCode to ensure DTO comparison consistency
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductDTO that = (ProductDTO) o;
        return stockQuantity == that.stockQuantity &&
                active == that.active &&
                Objects.equals(productId, that.productId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(price, that.price) &&
                Objects.equals(dateAdded, that.dateAdded) &&
                Objects.equals(category, that.category) &&
                Objects.equals(brand, that.brand) &&
                Objects.equals(supplier, that.supplier) &&
                Objects.equals(discount, that.discount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, name, description, price, stockQuantity, dateAdded, category, brand, supplier, active, discount);
    }

    @Override
    public String toString() {
        return "ProductDTO{" +
                "productId='" + productId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", stockQuantity=" + stockQuantity +
                ", dateAdded=" + dateAdded +
                ", category='" + category + '\'' +
                ", brand='" + brand + '\'' +
                ", supplier='" + supplier + '\'' +
                ", active=" + active +
                ", discount=" + discount +
                '}';
    }
}