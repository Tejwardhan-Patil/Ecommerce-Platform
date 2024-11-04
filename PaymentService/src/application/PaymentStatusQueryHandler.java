package application;

import com.website.paymentservice.core.PaymentEntity;
import com.website.paymentservice.core.PaymentService;
import com.website.paymentservice.infrastructure.PaymentRepositoryImpl;
import com.website.paymentservice.presentation.PaymentViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

public class PaymentStatusQueryHandler {

    private final PaymentService paymentService;
    private final PaymentRepositoryImpl paymentRepository;
    private static final Logger logger = LoggerFactory.getLogger(PaymentStatusQueryHandler.class);

    public PaymentStatusQueryHandler(PaymentService paymentService, PaymentRepositoryImpl paymentRepository) {
        this.paymentService = paymentService;
        this.paymentRepository = paymentRepository;
    }

    public PaymentViewModel handle(UUID paymentId) {
        logger.info("Handling payment status query for paymentId: {}", paymentId);

        Optional<PaymentEntity> paymentEntityOptional = paymentRepository.findById(paymentId);

        if (paymentEntityOptional.isEmpty()) {
            logger.warn("Payment not found for id: {}", paymentId);
            throw new PaymentNotFoundException("Payment not found for id: " + paymentId);
        }

        PaymentEntity paymentEntity = paymentEntityOptional.get();
        logger.info("Payment found, checking status for id: {}", paymentId);

        return new PaymentViewModel(paymentEntity.getPaymentId(),
                                    paymentEntity.getAmount(),
                                    paymentEntity.getStatus(),
                                    paymentEntity.getTransactionDate());
    }
    
    private class PaymentNotFoundException extends RuntimeException {
        public PaymentNotFoundException(String message) {
            super(message);
        }
    }
}