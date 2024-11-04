package infrastructure;

import PaymentService.src.core.PaymentEntity;
import PaymentService.src.core.PaymentStatus;
import PaymentService.src.core.repositories.PaymentRepository;
import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

public class PaymentRepositoryImpl implements PaymentRepository {

    private final DataSource dataSource;

    public PaymentRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public PaymentEntity save(PaymentEntity payment) {
        String query = "INSERT INTO payments (id, amount, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, payment.getId());
            preparedStatement.setDouble(2, payment.getAmount());
            preparedStatement.setString(3, payment.getStatus().name());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(payment.getCreatedAt()));
            preparedStatement.setTimestamp(5, Timestamp.valueOf(payment.getUpdatedAt()));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving payment", e);
        }
        return payment;
    }

    @Override
    public Optional<PaymentEntity> findById(String id) {
        String query = "SELECT * FROM payments WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                PaymentEntity payment = mapResultSetToPaymentEntity(resultSet);
                return Optional.of(payment);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding payment by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<PaymentEntity> findAll() {
        List<PaymentEntity> payments = new ArrayList<>();
        String query = "SELECT * FROM payments";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                PaymentEntity payment = mapResultSetToPaymentEntity(resultSet);
                payments.add(payment);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all payments", e);
        }
        return payments;
    }

    @Override
    public void update(PaymentEntity payment) {
        String query = "UPDATE payments SET amount = ?, status = ?, updated_at = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setDouble(1, payment.getAmount());
            preparedStatement.setString(2, payment.getStatus().name());
            preparedStatement.setTimestamp(3, Timestamp.valueOf(payment.getUpdatedAt()));
            preparedStatement.setString(4, payment.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating payment", e);
        }
    }

    @Override
    public void deleteById(String id) {
        String query = "DELETE FROM payments WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting payment by id", e);
        }
    }

    @Override
    public List<PaymentEntity> findByStatus(PaymentStatus status) {
        List<PaymentEntity> payments = new ArrayList<>();
        String query = "SELECT * FROM payments WHERE status = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, status.name());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                PaymentEntity payment = mapResultSetToPaymentEntity(resultSet);
                payments.add(payment);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching payments by status", e);
        }
        return payments;
    }

    private PaymentEntity mapResultSetToPaymentEntity(ResultSet resultSet) throws SQLException {
        String id = resultSet.getString("id");
        double amount = resultSet.getDouble("amount");
        PaymentStatus status = PaymentStatus.valueOf(resultSet.getString("status"));
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        Timestamp updatedAt = resultSet.getTimestamp("updated_at");

        PaymentEntity payment = new PaymentEntity(id, amount, status, createdAt.toLocalDateTime(), updatedAt.toLocalDateTime());
        return payment;
    }

    @Override
    public void processPayment(String paymentId, double amount) {
        Optional<PaymentEntity> optionalPayment = findById(paymentId);
        if (optionalPayment.isPresent()) {
            PaymentEntity payment = optionalPayment.get();
            payment.setAmount(amount);
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setUpdatedAt(java.time.LocalDateTime.now());
            update(payment);
        } else {
            throw new RuntimeException("Payment not found for processing");
        }
    }

    @Override
    public void refundPayment(String paymentId) {
        Optional<PaymentEntity> optionalPayment = findById(paymentId);
        if (optionalPayment.isPresent()) {
            PaymentEntity payment = optionalPayment.get();
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setUpdatedAt(java.time.LocalDateTime.now());
            update(payment);
        } else {
            throw new RuntimeException("Payment not found for refund");
        }
    }

    @Override
    public boolean existsById(String id) {
        String query = "SELECT 1 FROM payments WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException("Error checking if payment exists", e);
        }
    }
}