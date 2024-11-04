package presentation;

import com.paymentservice.application.ProcessPaymentCommandHandler;
import com.paymentservice.application.PaymentStatusQueryHandler;
import com.paymentservice.core.PaymentEntity;
import com.paymentservice.presentation.viewmodel.PaymentViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final ProcessPaymentCommandHandler processPaymentCommandHandler;
    private final PaymentStatusQueryHandler paymentStatusQueryHandler;

    @Autowired
    public PaymentController(ProcessPaymentCommandHandler processPaymentCommandHandler,
                             PaymentStatusQueryHandler paymentStatusQueryHandler) {
        this.processPaymentCommandHandler = processPaymentCommandHandler;
        this.paymentStatusQueryHandler = paymentStatusQueryHandler;
    }

    @PostMapping("/process")
    public ResponseEntity<PaymentViewModel> processPayment(@Validated @RequestBody PaymentViewModel paymentRequest) {
        try {
            PaymentEntity paymentEntity = processPaymentCommandHandler.handle(paymentRequest.toCommand());
            PaymentViewModel response = PaymentViewModel.fromEntity(paymentEntity);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/status/{paymentId}")
    public ResponseEntity<PaymentViewModel> getPaymentStatus(@PathVariable UUID paymentId) {
        try {
            PaymentEntity paymentEntity = paymentStatusQueryHandler.handle(paymentId);
            if (paymentEntity != null) {
                PaymentViewModel response = PaymentViewModel.fromEntity(paymentEntity);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/cancel/{paymentId}")
    public ResponseEntity<PaymentViewModel> cancelPayment(@PathVariable UUID paymentId) {
        try {
            PaymentEntity paymentEntity = processPaymentCommandHandler.cancelPayment(paymentId);
            if (paymentEntity != null) {
                PaymentViewModel response = PaymentViewModel.fromEntity(paymentEntity);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/refund/{paymentId}")
    public ResponseEntity<PaymentViewModel> refundPayment(@PathVariable UUID paymentId) {
        try {
            PaymentEntity paymentEntity = processPaymentCommandHandler.refundPayment(paymentId);
            if (paymentEntity != null) {
                PaymentViewModel response = PaymentViewModel.fromEntity(paymentEntity);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/history/{customerId}")
    public ResponseEntity<List<PaymentViewModel>> getPaymentHistory(@PathVariable UUID customerId) {
        try {
            List<PaymentEntity> paymentHistory = paymentStatusQueryHandler.getPaymentHistory(customerId);
            if (!paymentHistory.isEmpty()) {
                List<PaymentViewModel> response = paymentHistory.stream()
                        .map(PaymentViewModel::fromEntity)
                        .collect(Collectors.toList());
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleExceptions(Exception ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}