package presentation;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class OrderViewModel {
    private UUID orderId;
    private UUID customerId;
    private LocalDateTime orderDate;
    private List<OrderItemViewModel> items;
    private String status;
    private double totalPrice;
    private String shippingAddress;

    public OrderViewModel() {
        this.items = new ArrayList<>();
    }

    public OrderViewModel(UUID orderId, UUID customerId, LocalDateTime orderDate, List<OrderItemViewModel> items, 
                          String status, double totalPrice, String shippingAddress) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.items = items;
        this.status = status;
        this.totalPrice = totalPrice;
        this.shippingAddress = shippingAddress;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public List<OrderItemViewModel> getItems() {
        return items;
    }

    public void setItems(List<OrderItemViewModel> items) {
        this.items = items;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public static class OrderItemViewModel {
        private UUID productId;
        private String productName;
        private int quantity;
        private double price;

        public OrderItemViewModel() {}

        public OrderItemViewModel(UUID productId, String productName, int quantity, double price) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.price = price;
        }

        public UUID getProductId() {
            return productId;
        }

        public void setProductId(UUID productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }
    }

    public static OrderViewModel fromDomain(OrderEntity order) {
        List<OrderItemViewModel> itemViewModels = order.getItems().stream()
            .map(item -> new OrderItemViewModel(item.getProductId(), item.getProductName(), 
                                                item.getQuantity(), item.getPrice()))
            .collect(Collectors.toList());
        return new OrderViewModel(order.getOrderId(), order.getCustomerId(), order.getOrderDate(),
                                  itemViewModels, order.getStatus(), order.calculateTotalPrice(), order.getShippingAddress());
    }

    public OrderEntity toDomain() {
        List<OrderEntity.OrderItem> domainItems = this.items.stream()
            .map(item -> new OrderEntity.OrderItem(item.getProductId(), item.getProductName(),
                                                   item.getQuantity(), item.getPrice()))
            .collect(Collectors.toList());
        return new OrderEntity(this.orderId, this.customerId, this.orderDate, domainItems,
                               this.status, this.shippingAddress);
    }

    public static class OrderEntity {
        private UUID orderId;
        private UUID customerId;
        private LocalDateTime orderDate;
        private List<OrderItem> items;
        private String status;
        private String shippingAddress;

        public OrderEntity(UUID orderId, UUID customerId, LocalDateTime orderDate, List<OrderItem> items, 
                           String status, String shippingAddress) {
            this.orderId = orderId;
            this.customerId = customerId;
            this.orderDate = orderDate;
            this.items = items;
            this.status = status;
            this.shippingAddress = shippingAddress;
        }

        public UUID getOrderId() {
            return orderId;
        }

        public UUID getCustomerId() {
            return customerId;
        }

        public LocalDateTime getOrderDate() {
            return orderDate;
        }

        public List<OrderItem> getItems() {
            return items;
        }

        public String getStatus() {
            return status;
        }

        public String getShippingAddress() {
            return shippingAddress;
        }

        public double calculateTotalPrice() {
            return items.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
        }

        public static class OrderItem {
            private UUID productId;
            private String productName;
            private int quantity;
            private double price;

            public OrderItem(UUID productId, String productName, int quantity, double price) {
                this.productId = productId;
                this.productName = productName;
                this.quantity = quantity;
                this.price = price;
            }

            public UUID getProductId() {
                return productId;
            }

            public String getProductName() {
                return productName;
            }

            public int getQuantity() {
                return quantity;
            }

            public double getPrice() {
                return price;
            }
        }
    }
}