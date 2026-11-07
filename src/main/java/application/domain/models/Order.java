package application.domain.models;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Order {
    public enum OrderStatus {
        CART, PENDING_PAYMENT, PAID, PREPARING, DISPATCHED, DELIVERED, FINALIZED, CANCELLED
    }

    private final String id;
    private final String buyerId;
    private OrderStatus status;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private Instant createdAt;
    private Instant updatedAt;
    private String paymentReference;

    public Order(String id, String buyerId, OrderStatus status, List<OrderItem> items,
                 BigDecimal totalAmount, String shippingAddress, Instant createdAt,
                 Instant updatedAt, String paymentReference) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.buyerId = Objects.requireNonNull(buyerId);
        this.status = status != null ? status : OrderStatus.CART;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        this.shippingAddress = shippingAddress;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
        this.paymentReference = paymentReference;
    }

    public static Order createCart(String buyerId) {
        return new Order(null, buyerId, OrderStatus.CART, new ArrayList<>(),
                BigDecimal.ZERO, null, Instant.now(), Instant.now(), null);
    }

    public void addItem(OrderItem item) {
        ensureModifiable();
        this.items.add(item);
        recalculateTotal();
        touch();
    }

    public void removeItem(String productId) {
        ensureModifiable();
        this.items.removeIf(i -> i.getProductId().equals(productId));
        recalculateTotal();
        touch();
    }

    public void confirmPayment(String paymentReference) {
        if (status != OrderStatus.PENDING_PAYMENT && status != OrderStatus.CART) {
            throw new IllegalStateException("Order cannot accept payment in status: " + status);
        }
        this.paymentReference = paymentReference;
        this.status = OrderStatus.PAID;
        touch();
    }

    public void markPreparing() {
        if (status != OrderStatus.PAID) throw new IllegalStateException("Only PAID orders can be prepared");
        this.status = OrderStatus.PREPARING;
        touch();
    }

    public void markDispatched() {
        if (status != OrderStatus.PREPARING && status != OrderStatus.PAID) {
            throw new IllegalStateException("Only PREPARING/PAID orders can be dispatched");
        }
        this.status = OrderStatus.DISPATCHED;
        touch();
    }

    public void markDelivered() {
        if (status != OrderStatus.DISPATCHED) {
            throw new IllegalStateException("Only DISPATCHED orders can be marked delivered");
        }
        this.status = OrderStatus.DELIVERED;
        touch();
    }

    public void finalizeOrder() {
        if (status != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Only DELIVERED orders can be finalized");
        }
        this.status = OrderStatus.FINALIZED;
        touch();
    }

    public void cancel() {
        if (status == OrderStatus.FINALIZED || status == OrderStatus.DISPATCHED || status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel order in status: " + status);
        }
        this.status = OrderStatus.CANCELLED;
        touch();
    }

    public void submitForPayment(String shippingAddress) {
        if (status != OrderStatus.CART) throw new IllegalStateException("Only CART can be submitted");
        if (items.isEmpty()) throw new IllegalStateException("Cart is empty");
        this.shippingAddress = Objects.requireNonNull(shippingAddress);
        this.status = OrderStatus.PENDING_PAYMENT;
        touch();
    }

    private void ensureModifiable() {
        if (status == OrderStatus.FINALIZED || status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order cannot be modified in status: " + status);
        }
    }

    private void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void touch() { this.updatedAt = Instant.now(); }

    public String getId() { return id; }
    public String getBuyerId() { return buyerId; }
    public OrderStatus getStatus() { return status; }
    public List<OrderItem> getItems() { return List.copyOf(items); }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getShippingAddress() { return shippingAddress; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getPaymentReference() { return paymentReference; }
}
