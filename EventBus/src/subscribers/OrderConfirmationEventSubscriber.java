package subscribers;

import com.website.orderservice.application.events.OrderConfirmationEvent;
import com.website.orderservice.core.OrderEntity;
import com.website.orderservice.infrastructure.OrderRepository;
import com.website.notificationservice.application.commands.SendEmailCommand;
import com.website.notificationservice.application.commands.SendSmsCommand;
import com.website.notificationservice.core.NotificationService;
import com.website.shippingservice.application.commands.ShipOrderCommand;
import com.website.shippingservice.core.ShippingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderConfirmationEventSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(OrderConfirmationEventSubscriber.class);

    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private final ShippingService shippingService;

    @Autowired
    public OrderConfirmationEventSubscriber(OrderRepository orderRepository,
                                            NotificationService notificationService,
                                            ShippingService shippingService) {
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
        this.shippingService = shippingService;
    }

    @EventListener
    @Transactional
    public void handleOrderConfirmationEvent(OrderConfirmationEvent event) {
        logger.info("Received OrderConfirmationEvent for orderId: {}", event.getOrderId());

        OrderEntity order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + event.getOrderId()));

        if (order.isConfirmed()) {
            logger.warn("Order with id: {} is already confirmed.", event.getOrderId());
            return;
        }

        confirmOrder(order);
        sendOrderConfirmationNotifications(order);
        initiateShipping(order);
    }

    private void confirmOrder(OrderEntity order) {
        logger.info("Confirming order with id: {}", order.getId());
        order.confirm();
        orderRepository.save(order);
    }

    private void sendOrderConfirmationNotifications(OrderEntity order) {
        logger.info("Sending order confirmation notifications for orderId: {}", order.getId());

        SendEmailCommand emailCommand = new SendEmailCommand(order.getCustomerEmail(),
                "Order Confirmation",
                generateOrderConfirmationEmail(order));

        SendSmsCommand smsCommand = new SendSmsCommand(order.getCustomerPhone(),
                "Your order has been confirmed. Order ID: " + order.getId());

        notificationService.sendEmail(emailCommand);
        notificationService.sendSms(smsCommand);
    }

    private String generateOrderConfirmationEmail(OrderEntity order) {
        return "Dear Customer,\n\n" +
                "Your order with ID " + order.getId() + " has been successfully confirmed.\n" +
                "Details:\n" +
                "Product Name: " + order.getProductName() + "\n" +
                "Quantity: " + order.getQuantity() + "\n" +
                "Total Price: $" + order.getTotalPrice() + "\n\n" +
                "Thank you for shopping with us!";
    }

    private void initiateShipping(OrderEntity order) {
        logger.info("Initiating shipping for orderId: {}", order.getId());

        ShipOrderCommand shipOrderCommand = new ShipOrderCommand(order.getId(), order.getShippingAddress());

        shippingService.shipOrder(shipOrderCommand);
    }
}