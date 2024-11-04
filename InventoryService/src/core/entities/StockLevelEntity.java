package com.website.inventoryservice.core.entities;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "stock_levels")
public class StockLevelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    public StockLevelEntity() {
    }

    public StockLevelEntity(Long productId, Integer availableQuantity, Integer reservedQuantity, LocalDateTime lastUpdated, Integer version) {
        this.productId = productId;
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = reservedQuantity;
        this.lastUpdated = lastUpdated;
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public Integer getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockLevelEntity that = (StockLevelEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(productId, that.productId) &&
                Objects.equals(availableQuantity, that.availableQuantity) &&
                Objects.equals(reservedQuantity, that.reservedQuantity) &&
                Objects.equals(lastUpdated, that.lastUpdated) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, productId, availableQuantity, reservedQuantity, lastUpdated, version);
    }

    @Override
    public String toString() {
        return "StockLevelEntity{" +
                "id=" + id +
                ", productId=" + productId +
                ", availableQuantity=" + availableQuantity +
                ", reservedQuantity=" + reservedQuantity +
                ", lastUpdated=" + lastUpdated +
                ", version=" + version +
                '}';
    }

    public void updateStock(int quantityChange) {
        this.availableQuantity += quantityChange;
        this.lastUpdated = LocalDateTime.now();
    }

    public boolean canReserveStock(int quantityToReserve) {
        return availableQuantity - reservedQuantity >= quantityToReserve;
    }

    public void reserveStock(int quantity) {
        if (canReserveStock(quantity)) {
            reservedQuantity += quantity;
            lastUpdated = LocalDateTime.now();
        } else {
            throw new IllegalStateException("Not enough stock available to reserve");
        }
    }

    public void releaseReservedStock(int quantity) {
        if (reservedQuantity >= quantity) {
            reservedQuantity -= quantity;
            lastUpdated = LocalDateTime.now();
        } else {
            throw new IllegalStateException("Cannot release more reserved stock than is held");
        }
    }

    public void fulfillReservedStock(int quantity) {
        if (reservedQuantity >= quantity) {
            reservedQuantity -= quantity;
            availableQuantity -= quantity;
            lastUpdated = LocalDateTime.now();
        } else {
            throw new IllegalStateException("Cannot fulfill more reserved stock than is held");
        }
    }
    
    public void restock(int quantity) {
        this.availableQuantity += quantity;
        this.lastUpdated = LocalDateTime.now();
    }
    
    public void adjustStock(int quantityChange, boolean isRestock) {
        if (isRestock) {
            restock(quantityChange);
        } else {
            updateStock(quantityChange);
        }
    }

    public boolean isStockAvailable(int quantity) {
        return this.availableQuantity >= quantity;
    }

    public boolean isProductOutOfStock() {
        return this.availableQuantity == 0;
    }
    
    public static StockLevelEntity createNewStockLevel(Long productId, int initialStock) {
        return new StockLevelEntity(productId, initialStock, 0, LocalDateTime.now(), 0);
    }

    public void markAsOutOfStock() {
        this.availableQuantity = 0;
        this.lastUpdated = LocalDateTime.now();
    }

    public static StockLevelEntity adjustExistingStock(StockLevelEntity stockLevelEntity, int adjustment) {
        stockLevelEntity.adjustStock(adjustment, adjustment >= 0);
        stockLevelEntity.setLastUpdated(LocalDateTime.now());
        return stockLevelEntity;
    }
}