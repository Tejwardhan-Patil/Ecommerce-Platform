package infrastructure.persistence;

import com.ecommerce.inventoryservice.core.entities.StockLevelEntity;
import com.ecommerce.inventoryservice.core.repositories.StockLevelRepository;
import com.ecommerce.inventoryservice.core.valueobjects.ProductID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public class StockLevelRepositoryImpl implements StockLevelRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void saveStockLevel(StockLevelEntity stockLevelEntity) {
        entityManager.persist(stockLevelEntity);
    }

    @Override
    @Transactional
    public void updateStockLevel(StockLevelEntity stockLevelEntity) {
        entityManager.merge(stockLevelEntity);
    }

    @Override
    @Transactional
    public void deleteStockLevel(ProductID productId) {
        StockLevelEntity stockLevelEntity = findByProductId(productId)
            .orElseThrow(() -> new IllegalArgumentException("Stock level not found for product ID: " + productId.getValue()));
        entityManager.remove(stockLevelEntity);
    }

    @Override
    public Optional<StockLevelEntity> findByProductId(ProductID productId) {
        String query = "SELECT s FROM StockLevelEntity s WHERE s.productId = :productId";
        List<StockLevelEntity> result = entityManager.createQuery(query, StockLevelEntity.class)
            .setParameter("productId", productId)
            .getResultList();

        if (result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.get(0));
    }

    @Override
    public List<StockLevelEntity> findAll() {
        String query = "SELECT s FROM StockLevelEntity s";
        return entityManager.createQuery(query, StockLevelEntity.class).getResultList();
    }

    @Override
    @Transactional
    public void increaseStock(ProductID productId, int amount) {
        StockLevelEntity stockLevelEntity = findByProductId(productId)
            .orElseThrow(() -> new IllegalArgumentException("Stock level not found for product ID: " + productId.getValue()));
        
        int newQuantity = stockLevelEntity.getQuantity() + amount;
        stockLevelEntity.setQuantity(newQuantity);
        entityManager.merge(stockLevelEntity);
    }

    @Override
    @Transactional
    public void decreaseStock(ProductID productId, int amount) {
        StockLevelEntity stockLevelEntity = findByProductId(productId)
            .orElseThrow(() -> new IllegalArgumentException("Stock level not found for product ID: " + productId.getValue()));
        
        int newQuantity = stockLevelEntity.getQuantity() - amount;
        if (newQuantity < 0) {
            throw new IllegalStateException("Stock cannot be negative for product ID: " + productId.getValue());
        }
        
        stockLevelEntity.setQuantity(newQuantity);
        entityManager.merge(stockLevelEntity);
    }

    @Override
    public int getStockQuantity(ProductID productId) {
        StockLevelEntity stockLevelEntity = findByProductId(productId)
            .orElseThrow(() -> new IllegalArgumentException("Stock level not found for product ID: " + productId.getValue()));
        return stockLevelEntity.getQuantity();
    }

    @Override
    public List<StockLevelEntity> getStockBelowThreshold(int threshold) {
        String query = "SELECT s FROM StockLevelEntity s WHERE s.quantity < :threshold";
        return entityManager.createQuery(query, StockLevelEntity.class)
            .setParameter("threshold", threshold)
            .getResultList();
    }
}