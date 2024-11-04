package core.repositories;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import core.entities.StockLevelEntity;
import core.valueobjects.ProductID;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StockLevelRepository {

    private final Map<ProductID, StockLevelEntity> stockLevelCache = new ConcurrentHashMap<>();
    private final DataSource dataSource;

    public StockLevelRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        initializeCache();
    }

    private void initializeCache() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT product_id, stock_quantity FROM stock_levels")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ProductID productId = new ProductID(rs.getString("product_id"));
                StockLevelEntity stockLevel = new StockLevelEntity(productId, rs.getInt("stock_quantity"));
                stockLevelCache.put(productId, stockLevel);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing stock level cache", e);
        }
    }

    public Optional<StockLevelEntity> findStockByProductId(ProductID productId) {
        if (stockLevelCache.containsKey(productId)) {
            return Optional.of(stockLevelCache.get(productId));
        } else {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT stock_quantity FROM stock_levels WHERE product_id = ?")) {
                stmt.setString(1, productId.getValue());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    StockLevelEntity stockLevel = new StockLevelEntity(productId, rs.getInt("stock_quantity"));
                    stockLevelCache.put(productId, stockLevel);
                    return Optional.of(stockLevel);
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error fetching stock level for product ID: " + productId, e);
            }
        }
        return Optional.empty();
    }

    public List<StockLevelEntity> getAllStockLevels() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT product_id, stock_quantity FROM stock_levels")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ProductID productId = new ProductID(rs.getString("product_id"));
                StockLevelEntity stockLevel = new StockLevelEntity(productId, rs.getInt("stock_quantity"));
                stockLevelCache.put(productId, stockLevel);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all stock levels", e);
        }
        return List.copyOf(stockLevelCache.values());
    }

    public void updateStockLevel(ProductID productId, int newQuantity) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE stock_levels SET stock_quantity = ? WHERE product_id = ?")) {
            stmt.setInt(1, newQuantity);
            stmt.setString(2, productId.getValue());
            stmt.executeUpdate();
            stockLevelCache.put(productId, new StockLevelEntity(productId, newQuantity));
        } catch (SQLException e) {
            throw new RuntimeException("Error updating stock level for product ID: " + productId, e);
        }
    }

    public void createStockLevel(ProductID productId, int initialQuantity) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO stock_levels (product_id, stock_quantity) VALUES (?, ?)")) {
            stmt.setString(1, productId.getValue());
            stmt.setInt(2, initialQuantity);
            stmt.executeUpdate();
            stockLevelCache.put(productId, new StockLevelEntity(productId, initialQuantity));
        } catch (SQLException e) {
            throw new RuntimeException("Error creating stock level for product ID: " + productId, e);
        }
    }

    public void deleteStockLevel(ProductID productId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM stock_levels WHERE product_id = ?")) {
            stmt.setString(1, productId.getValue());
            stmt.executeUpdate();
            stockLevelCache.remove(productId);
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting stock level for product ID: " + productId, e);
        }
    }

    public int getTotalStockCount() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT SUM(stock_quantity) FROM stock_levels")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching total stock count", e);
        }
        return 0;
    }

    public List<ProductID> getOutOfStockProducts() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT product_id FROM stock_levels WHERE stock_quantity = 0")) {
            ResultSet rs = stmt.executeQuery();
            List<ProductID> outOfStockProducts = new ArrayList<>();
            while (rs.next()) {
                outOfStockProducts.add(new ProductID(rs.getString("product_id")));
            }
            return outOfStockProducts;
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching out-of-stock products", e);
        }
    }

    public void clearCache() {
        stockLevelCache.clear();
        initializeCache();
    }

    public int getStockQuantityByProduct(ProductID productId) {
        return findStockByProductId(productId)
                .map(StockLevelEntity::getStockQuantity)
                .orElseThrow(() -> new RuntimeException("Stock level not found for product: " + productId));
    }

    public boolean hasSufficientStock(ProductID productId, int requiredQuantity) {
        return getStockQuantityByProduct(productId) >= requiredQuantity;
    }
}