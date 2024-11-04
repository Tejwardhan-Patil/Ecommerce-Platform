package core;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class OrderEntity {
    
    private String orderId;
    private String customerId;
    private OrderStatus status;
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private String deliveryAddress;
    private PaymentInfo paymentInfo;

    public OrderEntity(String customerId, List<OrderItem> items, String deliveryAddress, PaymentInfo paymentInfo) {
        this.orderId = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.status = OrderStatus.PENDING;
        this.orderDate = LocalDateTime.now();
        this.items = new ArrayList<>(items);
        this.deliveryAddress = deliveryAddress;
        this.paymentInfo = paymentInfo;
        calculateTotalAmount();
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public PaymentInfo getPaymentInfo() {
        return paymentInfo;
    }

    public void setPaymentInfo(PaymentInfo paymentInfo) {
        this.paymentInfo = paymentInfo;
    }

    private void calculateTotalAmount() {
        this.totalAmount = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addItem(OrderItem item) {
        items.add(item);
        calculateTotalAmount();
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        calculateTotalAmount();
    }

    public boolean isDelivered() {
        return this.status == OrderStatus.DELIVERED;
    }

    public void markAsDelivered() {
        this.status = OrderStatus.DELIVERED;
        this.deliveryDate = LocalDateTime.now();
    }

    public void markAsCancelled() {
        this.status = OrderStatus.CANCELLED;
    }

    public boolean isPaymentCompleted() {
        return this.paymentInfo.isPaymentCompleted();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderEntity that = (OrderEntity) o;
        return Objects.equals(orderId, that.orderId) &&
                Objects.equals(customerId, that.customerId) &&
                status == that.status &&
                Objects.equals(orderDate, that.orderDate) &&
                Objects.equals(deliveryDate, that.deliveryDate) &&
                Objects.equals(items, that.items) &&
                Objects.equals(totalAmount, that.totalAmount) &&
                Objects.equals(deliveryAddress, that.deliveryAddress) &&
                Objects.equals(paymentInfo, that.paymentInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, customerId, status, orderDate, deliveryDate, items, totalAmount, deliveryAddress, paymentInfo);
    }

    @Override
    public String toString() {
        return "OrderEntity{" +
                "orderId='" + orderId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", status=" + status +
                ", orderDate=" + orderDate +
                ", deliveryDate=" + deliveryDate +
                ", items=" + items +
                ", totalAmount=" + totalAmount +
                ", deliveryAddress='" + deliveryAddress + '\'' +
                ", paymentInfo=" + paymentInfo +
                '}';
    }

    public enum OrderStatus {
        PENDING,
        SHIPPED,
        DELIVERED,
        CANCELLED
    }

    public static class OrderItem {

        private String productId;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;

        public OrderItem(String productId, String productName, int quantity, BigDecimal unitPrice) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public String getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public BigDecimal getTotalPrice() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OrderItem orderItem = (OrderItem) o;
            return quantity == orderItem.quantity &&
                    Objects.equals(productId, orderItem.productId) &&
                    Objects.equals(productName, orderItem.productName) &&
                    Objects.equals(unitPrice, orderItem.unitPrice);
        }

        @Override
        public int hashCode() {
            return Objects.hash(productId, productName, quantity, unitPrice);
        }

        @Override
        public String toString() {
            return "OrderItem{" +
                    "productId='" + productId + '\'' +
                    ", productName='" + productName + '\'' +
                    ", quantity=" + quantity +
                    ", unitPrice=" + unitPrice +
                    '}';
        }
    }

    public static class PaymentInfo {

        private String paymentId;
        private BigDecimal amountPaid;
        private boolean paymentCompleted;

        public PaymentInfo(String paymentId, BigDecimal amountPaid, boolean paymentCompleted) {
            this.paymentId = paymentId;
            this.amountPaid = amountPaid;
            this.paymentCompleted = paymentCompleted;
        }

        public String getPaymentId() {
            return paymentId;
        }

        public BigDecimal getAmountPaid() {
            return amountPaid;
        }

        public boolean isPaymentCompleted() {
            return paymentCompleted;
        }

        public void markPaymentCompleted() {
            this.paymentCompleted = true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PaymentInfo that = (PaymentInfo) o;
            return paymentCompleted == that.paymentCompleted &&
                    Objects.equals(paymentId, that.paymentId) &&
                    Objects.equals(amountPaid, that.amountPaid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(paymentId, amountPaid, paymentCompleted);
        }

        @Override
        public String toString() {
            return "PaymentInfo{" +
                    "paymentId='" + paymentId + '\'' +
                    ", amountPaid=" + amountPaid +
                    ", paymentCompleted=" + paymentCompleted +
                    '}';
        }
    }
}