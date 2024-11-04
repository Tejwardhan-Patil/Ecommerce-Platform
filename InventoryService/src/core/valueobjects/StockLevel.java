package core.valueobjects;

import java.util.Objects;

/**
 * StockLevel is a value object that encapsulates the quantity of stock available for a product.
 * It ensures that stock values are valid and handles business logic related to stock management.
 */
public final class StockLevel {

    private final int stockQuantity;
    private final int reservedQuantity;

    /**
     * Constructor to create a StockLevel object.
     * 
     * @param stockQuantity     The total available stock.
     * @param reservedQuantity  The stock that is reserved and cannot be sold.
     */
    public StockLevel(int stockQuantity, int reservedQuantity) {
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        if (reservedQuantity < 0) {
            throw new IllegalArgumentException("Reserved quantity cannot be negative.");
        }
        if (reservedQuantity > stockQuantity) {
            throw new IllegalArgumentException("Reserved quantity cannot exceed stock quantity.");
        }
        this.stockQuantity = stockQuantity;
        this.reservedQuantity = reservedQuantity;
    }

    /**
     * Static factory method to create a StockLevel with only available stock.
     * 
     * @param stockQuantity Total available stock.
     * @return StockLevel object.
     */
    public static StockLevel of(int stockQuantity) {
        return new StockLevel(stockQuantity, 0);
    }

    /**
     * Returns the total stock quantity available.
     * 
     * @return The stock quantity.
     */
    public int getStockQuantity() {
        return stockQuantity;
    }

    /**
     * Returns the reserved stock quantity.
     * 
     * @return The reserved quantity.
     */
    public int getReservedQuantity() {
        return reservedQuantity;
    }

    /**
     * Returns the available stock for purchase (excluding reserved stock).
     * 
     * @return The available stock.
     */
    public int getAvailableStock() {
        return stockQuantity - reservedQuantity;
    }

    /**
     * Checks if the stock is available.
     * 
     * @return true if stock is available, false otherwise.
     */
    public boolean isStockAvailable() {
        return getAvailableStock() > 0;
    }

    /**
     * Updates the stock level by adding to the current stock.
     * 
     * @param additionalStock The quantity of stock to add.
     * @return A new StockLevel object with the updated stock.
     */
    public StockLevel addStock(int additionalStock) {
        if (additionalStock < 0) {
            throw new IllegalArgumentException("Cannot add negative stock.");
        }
        return new StockLevel(stockQuantity + additionalStock, reservedQuantity);
    }

    /**
     * Reduces the stock level.
     * 
     * @param quantity The quantity of stock to reduce.
     * @return A new StockLevel object with the reduced stock.
     */
    public StockLevel reduceStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Cannot reduce negative stock.");
        }
        if (quantity > getAvailableStock()) {
            throw new IllegalArgumentException("Not enough stock available to reduce.");
        }
        return new StockLevel(stockQuantity - quantity, reservedQuantity);
    }

    /**
     * Reserves a specific quantity of stock.
     * 
     * @param quantity The quantity of stock to reserve.
     * @return A new StockLevel object with the reserved stock.
     */
    public StockLevel reserveStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Cannot reserve negative stock.");
        }
        if (quantity > getAvailableStock()) {
            throw new IllegalArgumentException("Not enough available stock to reserve.");
        }
        return new StockLevel(stockQuantity, reservedQuantity + quantity);
    }

    /**
     * Releases a reserved stock quantity.
     * 
     * @param quantity The quantity of reserved stock to release.
     * @return A new StockLevel object with the released reserved stock.
     */
    public StockLevel releaseReservedStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Cannot release negative reserved stock.");
        }
        if (quantity > reservedQuantity) {
            throw new IllegalArgumentException("Cannot release more reserved stock than available.");
        }
        return new StockLevel(stockQuantity, reservedQuantity - quantity);
    }

    /**
     * Checks if the stock level is zero.
     * 
     * @return true if stock level is zero, false otherwise.
     */
    public boolean isOutOfStock() {
        return getAvailableStock() == 0;
    }

    /**
     * Returns a string representation of the StockLevel object.
     */
    @Override
    public String toString() {
        return "StockLevel{" +
                "stockQuantity=" + stockQuantity +
                ", reservedQuantity=" + reservedQuantity +
                ", availableStock=" + getAvailableStock() +
                '}';
    }

    /**
     * Checks if two StockLevel objects are equal based on their stock and reserved quantities.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockLevel that = (StockLevel) o;
        return stockQuantity == that.stockQuantity && reservedQuantity == that.reservedQuantity;
    }

    /**
     * Generates a hash code for the StockLevel object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(stockQuantity, reservedQuantity);
    }
}