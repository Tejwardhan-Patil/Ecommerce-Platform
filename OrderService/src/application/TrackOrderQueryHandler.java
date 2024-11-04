package application;

import com.ecommerce.order.core.OrderEntity;
import com.ecommerce.order.core.OrderStatus;
import com.ecommerce.order.core.OrderService;
import com.ecommerce.order.infrastructure.OrderRepository;
import com.ecommerce.order.presentation.OrderViewModel;
import java.util.Optional;
import java.util.logging.Logger;

public class TrackOrderQueryHandler {

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final Logger logger = Logger.getLogger(TrackOrderQueryHandler.class.getName());

    public TrackOrderQueryHandler(OrderRepository orderRepository, OrderService orderService) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    public OrderViewModel handle(TrackOrderQuery query) {
        logger.info("Handling TrackOrderQuery for Order ID: " + query.getOrderId());

        Optional<OrderEntity> orderEntity = orderRepository.findById(query.getOrderId());

        if (orderEntity.isEmpty()) {
            logger.warning("Order not found for ID: " + query.getOrderId());
            throw new OrderNotFoundException("Order with ID " + query.getOrderId() + " not found.");
        }

        OrderEntity order = orderEntity.get();
        return convertToViewModel(order);
    }

    private OrderViewModel convertToViewModel(OrderEntity order) {
        logger.info("Converting OrderEntity to OrderViewModel for Order ID: " + order.getOrderId());

        OrderViewModel viewModel = new OrderViewModel();
        viewModel.setOrderId(order.getOrderId());
        viewModel.setCustomerId(order.getCustomerId());
        viewModel.setOrderStatus(order.getOrderStatus().name());
        viewModel.setTotalAmount(order.getTotalAmount());
        viewModel.setItems(order.getItems());

        if (order.getOrderStatus() == OrderStatus.DELIVERED) {
            viewModel.setDeliveryDate(order.getDeliveryDate());
        }

        logger.info("OrderViewModel successfully created for Order ID: " + order.getOrderId());
        return viewModel;
    }
}

class TrackOrderQuery {
    private final String orderId;

    public TrackOrderQuery(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }
}

class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}