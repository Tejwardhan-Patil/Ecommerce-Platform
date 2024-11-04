package application.dtos;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

public class StockLevelDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "Product ID cannot be null")
    @Size(min = 1, max = 50, message = "Product ID must be between 1 and 50 characters")
    private String productId;

    @NotNull(message = "Warehouse ID cannot be null")
    @Size(min = 1, max = 50, message = "Warehouse ID must be between 1 and 50 characters")
    private String warehouseId;

    @NotNull(message = "Stock Quantity cannot be null")
    @Positive(message = "Stock Quantity must be greater than zero")
    private BigDecimal stockQuantity;

    @NotNull(message = "Minimum Stock Level cannot be null")
    @Positive(message = "Minimum Stock Level must be greater than zero")
    private BigDecimal minStockLevel;

    @NotNull(message = "Reorder Level cannot be null")
    @Positive(message = "Reorder Level must be greater than zero")
    private BigDecimal reorderLevel;

    @NotNull(message = "Reorder Quantity cannot be null")
    @Positive(message = "Reorder Quantity must be greater than zero")
    private BigDecimal reorderQuantity;

    @NotNull(message = "Last Restocked Date cannot be null")
    private LocalDateTime lastRestockedDate;

    @NotNull(message = "Last Updated Date cannot be null")
    private LocalDateTime lastUpdatedDate;

    public StockLevelDTO() {
        // Default constructor
    }

    public StockLevelDTO(String productId, String warehouseId, BigDecimal stockQuantity, BigDecimal minStockLevel,
                         BigDecimal reorderLevel, BigDecimal reorderQuantity, LocalDateTime lastRestockedDate,
                         LocalDateTime lastUpdatedDate) {
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.stockQuantity = stockQuantity;
        this.minStockLevel = minStockLevel;
        this.reorderLevel = reorderLevel;
        this.reorderQuantity = reorderQuantity;
        this.lastRestockedDate = lastRestockedDate;
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
    }

    public BigDecimal getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(BigDecimal stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public BigDecimal getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(BigDecimal minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    public BigDecimal getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(BigDecimal reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public BigDecimal getReorderQuantity() {
        return reorderQuantity;
    }

    public void setReorderQuantity(BigDecimal reorderQuantity) {
        this.reorderQuantity = reorderQuantity;
    }

    public LocalDateTime getLastRestockedDate() {
        return lastRestockedDate;
    }

    public void setLastRestockedDate(LocalDateTime lastRestockedDate) {
        this.lastRestockedDate = lastRestockedDate;
    }

    public LocalDateTime getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(LocalDateTime lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockLevelDTO that = (StockLevelDTO) o;
        return productId.equals(that.productId) &&
               warehouseId.equals(that.warehouseId) &&
               stockQuantity.equals(that.stockQuantity) &&
               minStockLevel.equals(that.minStockLevel) &&
               reorderLevel.equals(that.reorderLevel) &&
               reorderQuantity.equals(that.reorderQuantity) &&
               lastRestockedDate.equals(that.lastRestockedDate) &&
               lastUpdatedDate.equals(that.lastUpdatedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, warehouseId, stockQuantity, minStockLevel, reorderLevel, reorderQuantity, lastRestockedDate, lastUpdatedDate);
    }

    @Override
    public String toString() {
        return "StockLevelDTO{" +
               "productId='" + productId + '\'' +
               ", warehouseId='" + warehouseId + '\'' +
               ", stockQuantity=" + stockQuantity +
               ", minStockLevel=" + minStockLevel +
               ", reorderLevel=" + reorderLevel +
               ", reorderQuantity=" + reorderQuantity +
               ", lastRestockedDate=" + lastRestockedDate +
               ", lastUpdatedDate=" + lastUpdatedDate +
               '}';
    }
}