package application;

import com.ecommerce.orderservice.core.OrderEntity;
import com.ecommerce.orderservice.core.OrderService;
import com.ecommerce.orderservice.infrastructure.OrderRepositoryImpl;
import com.ecommerce.inventoryservice.application.CheckStockQueryHandler;
import com.ecommerce.paymentservice.application.ProcessPaymentCommandHandler;
import com.ecommerce.sharedkernel.messaging.EventBus;
import com.ecommerce.sharedkernel.events.OrderPlacedEvent;
import com.ecommerce.sharedkernel.valueobjects.CustomerID;
import com.ecommerce.sharedkernel.valueobjects.OrderID;
import com.ecommerce.sharedkernel.valueobjects.PaymentID;
import com.ecommerce.sharedkernel.exceptions.OrderPlacementException;
import java.time.LocalDateTime;

public class PlaceOrderCommandHandler {

    private final OrderService orderService;
    private final OrderRepositoryImpl orderRepository;
    private final CheckStockQueryHandler checkStockHandler;
    private final ProcessPaymentCommandHandler processPaymentHandler;
    private final EventBus eventBus;

    public PlaceOrderCommandHandler(OrderService orderService,
                                    OrderRepositoryImpl orderRepository,
                                    CheckStockQueryHandler checkStockHandler,
                                    ProcessPaymentCommandHandler processPaymentHandler,
                                    EventBus eventBus) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.checkStockHandler = checkStockHandler;
        this.processPaymentHandler = processPaymentHandler;
        this.eventBus = eventBus;
    }

    public void handle(PlaceOrderCommand command) throws OrderPlacementException {
        // Validate the command inputs
        validateCommand(command);

        // Check stock availability
        boolean isStockAvailable = checkStockHandler.handle(command.getProductID(), command.getQuantity());
        if (!isStockAvailable) {
            throw new OrderPlacementException("Stock is insufficient for the requested quantity.");
        }

        // Initiate payment
        PaymentID paymentID = processPayment(command);

        // Create Order
        OrderEntity order = createOrder(command, paymentID);

        // Save order in repository
        orderRepository.save(order);

        // Publish event that the order was placed successfully
        OrderPlacedEvent event = new OrderPlacedEvent(order.getOrderID(), order.getCustomerID(), LocalDateTime.now());
        eventBus.publish(event);

        // Log successful order placement
        System.out.println("Order placed successfully: " + order.getOrderID());
    }

    private void validateCommand(PlaceOrderCommand command) throws OrderPlacementException {
        if (command.getCustomerID() == null || command.getProductID() == null || command.getQuantity() <= 0) {
            throw new OrderPlacementException("Invalid order command. Ensure all fields are correctly populated.");
        }
    }

    private PaymentID processPayment(PlaceOrderCommand command) throws OrderPlacementException {
        try {
            return processPaymentHandler.handle(command.getCustomerID(), command.getTotalAmount());
        } catch (Exception e) {
            throw new OrderPlacementException("Payment processing failed: " + e.getMessage());
        }
    }

    private OrderEntity createOrder(PlaceOrderCommand command, PaymentID paymentID) {
        OrderID orderID = OrderID.generate();
        return new OrderEntity(orderID, command.getCustomerID(), command.getProductID(),
                command.getQuantity(), command.getTotalAmount(), paymentID, LocalDateTime.now());
    }
}