package infrastructure;

import com.website.orderservice.core.OrderEntity;
import com.website.orderservice.core.OrderRepository;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepositoryImpl implements OrderRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public OrderEntity save(OrderEntity order) {
        if (order.getId() == null) {
            entityManager.persist(order);
            return order;
        } else {
            return entityManager.merge(order);
        }
    }

    @Override
    public Optional<OrderEntity> findById(Long id) {
        return Optional.ofNullable(entityManager.find(OrderEntity.class, id));
    }

    @Override
    public List<OrderEntity> findAll() {
        String query = "SELECT o FROM OrderEntity o";
        return entityManager.createQuery(query, OrderEntity.class).getResultList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        OrderEntity order = entityManager.find(OrderEntity.class, id);
        if (order != null) {
            entityManager.remove(order);
        }
    }

    @Override
    @Transactional
    public void updateStatus(Long orderId, String status) {
        OrderEntity order = entityManager.find(OrderEntity.class, orderId);
        if (order != null) {
            order.setStatus(status);
            entityManager.merge(order);
        }
    }

    @Override
    public List<OrderEntity> findByCustomerId(Long customerId) {
        String query = "SELECT o FROM OrderEntity o WHERE o.customerId = :customerId";
        return entityManager.createQuery(query, OrderEntity.class)
                            .setParameter("customerId", customerId)
                            .getResultList();
    }

    @Override
    public List<OrderEntity> findByStatus(String status) {
        String query = "SELECT o FROM OrderEntity o WHERE o.status = :status";
        return entityManager.createQuery(query, OrderEntity.class)
                            .setParameter("status", status)
                            .getResultList();
    }

    @Override
    @Transactional
    public void deleteOrdersByCustomerId(Long customerId) {
        String query = "DELETE FROM OrderEntity o WHERE o.customerId = :customerId";
        entityManager.createQuery(query)
                     .setParameter("customerId", customerId)
                     .executeUpdate();
    }

    @Override
    public List<OrderEntity> findOrdersByDateRange(String startDate, String endDate) {
        String query = "SELECT o FROM OrderEntity o WHERE o.orderDate BETWEEN :startDate AND :endDate";
        return entityManager.createQuery(query, OrderEntity.class)
                            .setParameter("startDate", startDate)
                            .setParameter("endDate", endDate)
                            .getResultList();
    }

    @Override
    public List<OrderEntity> findPendingOrders() {
        String query = "SELECT o FROM OrderEntity o WHERE o.status = 'PENDING'";
        return entityManager.createQuery(query, OrderEntity.class).getResultList();
    }

    @Override
    public List<OrderEntity> findCompletedOrders() {
        String query = "SELECT o FROM OrderEntity o WHERE o.status = 'COMPLETED'";
        return entityManager.createQuery(query, OrderEntity.class).getResultList();
    }

    @Override
    public List<OrderEntity> findCanceledOrders() {
        String query = "SELECT o FROM OrderEntity o WHERE o.status = 'CANCELED'";
        return entityManager.createQuery(query, OrderEntity.class).getResultList();
    }

    @Override
    @Transactional
    public void markOrderAsShipped(Long orderId) {
        OrderEntity order = entityManager.find(OrderEntity.class, orderId);
        if (order != null && "PROCESSING".equals(order.getStatus())) {
            order.setStatus("SHIPPED");
            entityManager.merge(order);
        }
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        OrderEntity order = entityManager.find(OrderEntity.class, orderId);
        if (order != null && !"SHIPPED".equals(order.getStatus())) {
            order.setStatus("CANCELED");
            entityManager.merge(order);
        }
    }

    @Override
    public List<OrderEntity> findOrdersWithHighTotal(double total) {
        String query = "SELECT o FROM OrderEntity o WHERE o.totalAmount > :total";
        return entityManager.createQuery(query, OrderEntity.class)
                            .setParameter("total", total)
                            .getResultList();
    }

    @Override
    public List<OrderEntity> findOrdersByProductId(Long productId) {
        String query = "SELECT o FROM OrderEntity o JOIN o.orderItems i WHERE i.productId = :productId";
        return entityManager.createQuery(query, OrderEntity.class)
                            .setParameter("productId", productId)
                            .getResultList();
    }

    @Override
    public List<OrderEntity> findOrdersByPaymentMethod(String paymentMethod) {
        String query = "SELECT o FROM OrderEntity o WHERE o.paymentMethod = :paymentMethod";
        return entityManager.createQuery(query, OrderEntity.class)
                            .setParameter("paymentMethod", paymentMethod)
                            .getResultList();
    }

    @Override
    public long countOrdersByCustomerId(Long customerId) {
        String query = "SELECT COUNT(o) FROM OrderEntity o WHERE o.customerId = :customerId";
        return entityManager.createQuery(query, Long.class)
                            .setParameter("customerId", customerId)
                            .getSingleResult();
    }

    @Override
    public long countTotalOrders() {
        String query = "SELECT COUNT(o) FROM OrderEntity o";
        return entityManager.createQuery(query, Long.class).getSingleResult();
    }

    @Override
    public long countOrdersByStatus(String status) {
        String query = "SELECT COUNT(o) FROM OrderEntity o WHERE o.status = :status";
        return entityManager.createQuery(query, Long.class)
                            .setParameter("status", status)
                            .getSingleResult();
    }

    @Override
    @Transactional
    public void deleteAllOrders() {
        String query = "DELETE FROM OrderEntity";
        entityManager.createQuery(query).executeUpdate();
    }

    @Override
    @Transactional
    public void updatePaymentMethod(Long orderId, String paymentMethod) {
        OrderEntity order = entityManager.find(OrderEntity.class, orderId);
        if (order != null) {
            order.setPaymentMethod(paymentMethod);
            entityManager.merge(order);
        }
    }

    @Override
    public boolean existsById(Long id) {
        String query = "SELECT COUNT(o) FROM OrderEntity o WHERE o.id = :id";
        Long count = entityManager.createQuery(query, Long.class)
                                  .setParameter("id", id)
                                  .getSingleResult();
        return count > 0;
    }
}