package infrastructure.persistence;

import com.website.inventoryservice.core.entities.ProductEntity;
import com.website.inventoryservice.core.repositories.ProductRepository;
import com.website.inventoryservice.valueobjects.ProductID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepositoryImpl implements ProductRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public ProductRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public ProductEntity save(ProductEntity product) {
        if (product.getId() == null) {
            entityManager.persist(product);
        } else {
            entityManager.merge(product);
        }
        return product;
    }

    @Override
    public Optional<ProductEntity> findById(ProductID productId) {
        ProductEntity product = entityManager.find(ProductEntity.class, productId);
        return Optional.ofNullable(product);
    }

    @Override
    public List<ProductEntity> findAll() {
        Query query = entityManager.createQuery("SELECT p FROM ProductEntity p");
        return query.getResultList();
    }

    @Override
    public List<ProductEntity> findByName(String name) {
        Query query = entityManager.createQuery("SELECT p FROM ProductEntity p WHERE p.name LIKE :name");
        query.setParameter("name", "%" + name + "%");
        return query.getResultList();
    }

    @Override
    @Transactional
    public void deleteById(ProductID productId) {
        ProductEntity product = entityManager.find(ProductEntity.class, productId);
        if (product != null) {
            entityManager.remove(product);
        }
    }

    @Override
    @Transactional
    public void updateStockLevel(ProductID productId, int newStockLevel) {
        ProductEntity product = entityManager.find(ProductEntity.class, productId);
        if (product != null) {
            product.setStockLevel(newStockLevel);
            entityManager.merge(product);
        }
    }

    @Override
    public List<ProductEntity> findLowStockProducts(int threshold) {
        Query query = entityManager.createQuery("SELECT p FROM ProductEntity p WHERE p.stockLevel < :threshold");
        query.setParameter("threshold", threshold);
        return query.getResultList();
    }

    @Override
    public List<ProductEntity> findProductsByCategory(String category) {
        Query query = entityManager.createQuery("SELECT p FROM ProductEntity p WHERE p.category = :category");
        query.setParameter("category", category);
        return query.getResultList();
    }

    @Override
    public long countProducts() {
        Query query = entityManager.createQuery("SELECT COUNT(p) FROM ProductEntity p");
        return (Long) query.getSingleResult();
    }

    @Override
    @Transactional
    public void updateProductDetails(ProductEntity product) {
        ProductEntity existingProduct = entityManager.find(ProductEntity.class, product.getId());
        if (existingProduct != null) {
            existingProduct.setName(product.getName());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setCategory(product.getCategory());
            existingProduct.setStockLevel(product.getStockLevel());
            entityManager.merge(existingProduct);
        }
    }

    @Override
    public List<ProductEntity> searchProducts(String searchTerm) {
        Query query = entityManager.createQuery("SELECT p FROM ProductEntity p WHERE p.name LIKE :searchTerm OR p.description LIKE :searchTerm");
        query.setParameter("searchTerm", "%" + searchTerm + "%");
        return query.getResultList();
    }

    @Override
    public boolean existsById(ProductID productId) {
        ProductEntity product = entityManager.find(ProductEntity.class, productId);
        return product != null;
    }

    @Override
    @Transactional
    public void bulkUpdateStockLevel(List<ProductID> productIds, int newStockLevel) {
        Query query = entityManager.createQuery("UPDATE ProductEntity p SET p.stockLevel = :newStockLevel WHERE p.id IN :productIds");
        query.setParameter("newStockLevel", newStockLevel);
        query.setParameter("productIds", productIds);
        query.executeUpdate();
    }

    @Override
    public List<ProductEntity> findProductsByPriceRange(double minPrice, double maxPrice) {
        Query query = entityManager.createQuery("SELECT p FROM ProductEntity p WHERE p.price BETWEEN :minPrice AND :maxPrice");
        query.setParameter("minPrice", minPrice);
        query.setParameter("maxPrice", maxPrice);
        return query.getResultList();
    }

    @Override
    public List<ProductEntity> findProductsCreatedAfter(java.util.Date date) {
        Query query = entityManager.createQuery("SELECT p FROM ProductEntity p WHERE p.creationDate > :date");
        query.setParameter("date", date);
        return query.getResultList();
    }

    @Override
    @Transactional
    public void batchDeleteProducts(List<ProductID> productIds) {
        Query query = entityManager.createQuery("DELETE FROM ProductEntity p WHERE p.id IN :productIds");
        query.setParameter("productIds", productIds);
        query.executeUpdate();
    }

    @Override
    public List<ProductEntity> findDiscountedProducts(double discountThreshold) {
        Query query = entityManager.createQuery("SELECT p FROM ProductEntity p WHERE p.discount >= :discountThreshold");
        query.setParameter("discountThreshold", discountThreshold);
        return query.getResultList();
    }

    @Override
    public List<ProductEntity> findProductsByTags(List<String> tags) {
        Query query = entityManager.createQuery("SELECT p FROM ProductEntity p JOIN p.tags t WHERE t.name IN :tags");
        query.setParameter("tags", tags);
        return query.getResultList();
    }

    @Override
    public List<ProductEntity> findFeaturedProducts() {
        Query query = entityManager.createQuery("SELECT p FROM ProductEntity p WHERE p.isFeatured = true");
        return query.getResultList();
    }
}