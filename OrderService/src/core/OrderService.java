package core;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import application.PlaceOrderCommandHandler;
import application.TrackOrderQueryHandler;
import infrastructure.OrderRepositoryImpl;
import infrastructure.InventoryServiceClient;
import domain.OrderEntity;
import domain.OrderStatus;
import valueobjects.OrderID;
import messaging.OrderEventPublisher;
import dtos.OrderDTO;
import dtos.ItemDTO;

public class OrderService {

    private final OrderRepositoryImpl orderRepository;
    private final OrderEventPublisher orderEventPublisher;
    private final PlaceOrderCommandHandler placeOrderHandler;
    private final TrackOrderQueryHandler trackOrderHandler;
    private final InventoryServiceClient inventoryServiceClient;

    public OrderService(OrderRepositoryImpl orderRepository, OrderEventPublisher orderEventPublisher,
                        PlaceOrderCommandHandler placeOrderHandler, TrackOrderQueryHandler trackOrderHandler,
                        InventoryServiceClient inventoryServiceClient) {
        this.orderRepository = orderRepository;
        this.orderEventPublisher = orderEventPublisher;
        this.placeOrderHandler = placeOrderHandler;
        this.trackOrderHandler = trackOrderHandler;
        this.inventoryServiceClient = inventoryServiceClient;
    }

    public OrderDTO placeOrder(OrderDTO orderDTO) {
        if (!validateOrderItems(orderDTO)) {
            throw new IllegalArgumentException("One or more items in the order are not available in stock.");
        }

        OrderEntity orderEntity = convertToEntity(orderDTO);
        orderEntity.setOrderStatus(OrderStatus.PENDING);
        orderEntity.setOrderId(new OrderID(UUID.randomUUID().toString()));
        orderEntity.setCreatedAt(LocalDateTime.now());

        // Fraud detection logic
        if (checkOrderForFraud(orderEntity)) {
            orderEntity.setOrderStatus(OrderStatus.FRAUDULENT);
            orderRepository.saveOrder(orderEntity);
            return convertToDTO(orderEntity);
        }

        OrderEntity savedOrder = orderRepository.saveOrder(orderEntity);

        orderEventPublisher.publishOrderPlacedEvent(savedOrder.getOrderId(), savedOrder.getTotalAmount());

        return convertToDTO(savedOrder);
    }

    public Optional<OrderDTO> trackOrder(String orderId) {
        OrderID orderID = new OrderID(orderId);
        Optional<OrderEntity> orderEntity = orderRepository.findOrderById(orderID);

        if (orderEntity.isPresent()) {
            return Optional.of(convertToDTO(orderEntity.get()));
        }
        return Optional.empty();
    }

    public List<OrderDTO> getAllOrders() {
        List<OrderEntity> orderEntities = orderRepository.findAllOrders();
        return convertToDTOList(orderEntities);
    }

    public void cancelOrder(String orderId) {
        OrderID orderID = new OrderID(orderId);
        Optional<OrderEntity> orderEntity = orderRepository.findOrderById(orderID);

        if (orderEntity.isPresent()) {
            OrderEntity entity = orderEntity.get();
            entity.setOrderStatus(OrderStatus.CANCELED);
            entity.setUpdatedAt(LocalDateTime.now());
            orderRepository.updateOrder(entity);
            orderEventPublisher.publishOrderCanceledEvent(entity.getOrderId());
        }
    }

    public OrderDTO updateOrder(OrderDTO orderDTO) {
        OrderID orderId = new OrderID(orderDTO.getOrderId());
        Optional<OrderEntity> existingOrder = orderRepository.findOrderById(orderId);

        if (existingOrder.isPresent()) {
            OrderEntity orderEntity = existingOrder.get();
            orderEntity.setItems(orderDTO.getItems());
            orderEntity.setTotalAmount(orderDTO.getTotalAmount());
            orderEntity.setUpdatedAt(LocalDateTime.now());
            orderEntity.setOrderStatus(orderDTO.getOrderStatus());

            OrderEntity updatedOrder = orderRepository.updateOrder(orderEntity);
            return convertToDTO(updatedOrder);
        }
        return null;
    }

    private OrderEntity convertToEntity(OrderDTO orderDTO) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderId(new OrderID(orderDTO.getOrderId()));
        orderEntity.setCustomerId(orderDTO.getCustomerId());
        orderEntity.setItems(orderDTO.getItems());
        orderEntity.setTotalAmount(orderDTO.getTotalAmount());
        orderEntity.setOrderStatus(orderDTO.getOrderStatus());
        orderEntity.setCreatedAt(orderDTO.getCreatedAt());
        return orderEntity;
    }

    private OrderDTO convertToDTO(OrderEntity orderEntity) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderId(orderEntity.getOrderId().getValue());
        orderDTO.setCustomerId(orderEntity.getCustomerId());
        orderDTO.setItems(orderEntity.getItems());
        orderDTO.setTotalAmount(orderEntity.getTotalAmount());
        orderDTO.setOrderStatus(orderEntity.getOrderStatus());
        orderDTO.setCreatedAt(orderEntity.getCreatedAt());
        orderDTO.setUpdatedAt(orderEntity.getUpdatedAt());
        return orderDTO;
    }

    private List<OrderDTO> convertToDTOList(List<OrderEntity> orderEntities) {
        return orderEntities.stream()
                .map(orderEntity -> convertToDTO(orderEntity)) 
                .collect(Collectors.toList()); 
    }
    
    public boolean isOrderDeliverable(String orderId) {
        OrderID orderID = new OrderID(orderId);
        Optional<OrderEntity> orderEntity = orderRepository.findOrderById(orderID);

        return orderEntity.isPresent() && orderEntity.get().getOrderStatus() == OrderStatus.SHIPPED;
    }

    public boolean validateOrderItems(OrderDTO orderDTO) {
        for (ItemDTO item : orderDTO.getItems()) {
            boolean isAvailable = inventoryServiceClient.checkStockAvailability(item.getProductId(), item.getQuantity());
            if (!isAvailable) {
                return false; // Item not available in stock
            }
        }
        return true; // All items are available
    }
    
    public void confirmOrderDelivery(String orderId) {
        OrderID orderID = new OrderID(orderId);
        Optional<OrderEntity> orderEntity = orderRepository.findOrderById(orderID);

        if (orderEntity.isPresent()) {
            OrderEntity entity = orderEntity.get();
            entity.setOrderStatus(OrderStatus.DELIVERED);
            entity.setUpdatedAt(LocalDateTime.now());
            orderRepository.updateOrder(entity);
            orderEventPublisher.publishOrderDeliveredEvent(entity.getOrderId());
        }
    }
    
    public boolean checkOrderForFraud(OrderEntity orderEntity) {
        double totalAmount = orderEntity.getTotalAmount();
        int itemCount = orderEntity.getItems().size();
        String customerId = orderEntity.getCustomerId();
    
        // Check if the total order amount exceeds the threshold for fraud detection
        if (totalAmount > 10000) {
            logFraudDetection("High order amount detected: " + totalAmount, orderEntity);
            return true; // Fraud detected due to high order amount
        }
    
        // Check if the number of items in the order exceeds a suspicious limit
        if (itemCount > 10) {
            logFraudDetection("Unusually high number of items detected: " + itemCount, orderEntity);
            return true; // Fraud detected due to unusually high number of items
        }
    
        // Check for multiple high-value orders from the same customer in a short time span
        List<OrderEntity> recentOrders = orderRepository.findRecentOrdersByCustomerId(customerId, LocalDateTime.now().minusHours(1));
        long highValueOrderCount = recentOrders.stream()
                                            .filter(order -> order.getTotalAmount().compareTo(new BigDecimal("5000")) > 0)
                                            .count();

        if (highValueOrderCount >= 3) {
            logFraudDetection("Multiple high-value orders detected for customer: " + customerId, orderEntity);
            return true; // Fraud detected due to multiple high-value orders in a short time frame
        }
    
        // Check if the order is being shipped to a flagged address
        String shippingAddress = orderEntity.getShippingAddress();
        if (isFlaggedAddress(shippingAddress)) {
            logFraudDetection("Order being shipped to flagged address: " + shippingAddress, orderEntity);
            return true; // Fraud detected due to flagged shipping address
        }
    
        // Check if the customer has been flagged for past fraudulent activity
        if (isFlaggedCustomer(customerId)) {
            logFraudDetection("Customer has been flagged for fraudulent activity: " + customerId, orderEntity);
            return true; // Fraud detected due to flagged customer
        }
    
        // No fraud detected
        return false;
    }
    
    private boolean isFlaggedAddress(String shippingAddress) {
        // Check against a list of known fraudulent addresses
        List<String> flaggedAddresses = List.of("123 Fraud St", "456 Fake Ave");
        return flaggedAddresses.contains(shippingAddress);
    }
    
    private boolean isFlaggedCustomer(String customerId) {
        // Check if the customer is on a blacklist or has been flagged for past fraudulent activities
        return fraudDetectionService.isCustomerFlagged(customerId);
    }
    
    private void logFraudDetection(String reason, OrderEntity orderEntity) {
        // Log the fraud detection reason and details for further analysis and reporting
        System.out.println("Fraud detected: " + reason + " for order ID: " + orderEntity.getOrderId());
    }
    
}