package application.domain.models;

import application.domain.exceptions.EmptyCartException;
import application.domain.exceptions.InvalidPaymentReferenceException;
import application.domain.exceptions.OrderAlreadyPaidException;
import application.domain.exceptions.OrderItemNotFoundException;
import application.domain.exceptions.OrderNotCancellableException;
import application.domain.exceptions.OrderNotDeliverableException;
import application.domain.exceptions.OrderNotDispatchableException;
import application.domain.exceptions.OrderNotFinalizableException;
import application.domain.exceptions.OrderNotModifiableException;
import application.domain.exceptions.OrderNotPendingException;
import application.domain.exceptions.OrderNotPreparableException;
import application.domain.exceptions.OrderNotSubmittableException;
import application.domain.exceptions.OrderOwnershipException;
import application.domain.exceptions.ShippingAddressRequiredException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Order {
  public enum OrderStatus {
    CART,
    PENDING_PAYMENT,
    PAID,
    PREPARING,
    DISPATCHED,
    DELIVERED,
    FINALIZED,
    CANCELLED
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

  public Order(
      String id,
      String buyerId,
      OrderStatus status,
      List<OrderItem> items,
      BigDecimal totalAmount,
      String shippingAddress,
      Instant createdAt,
      Instant updatedAt,
      String paymentReference) {
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
    return new Order(
        null,
        buyerId,
        OrderStatus.CART,
        new ArrayList<>(),
        BigDecimal.ZERO,
        null,
        Instant.now(),
        Instant.now(),
        null);
  }

  public void addItem(OrderItem item) {
    ensureModifiable();
    this.items.add(item);
    recalculateTotal();
    touch();
  }

  public void removeItem(String productId) {
    ensureModifiable();
    boolean removed = this.items.removeIf(i -> i.getProductId().equals(productId));
    if (!removed) {
      throw new OrderItemNotFoundException(id, productId);
    }
    recalculateTotal();
    touch();
  }

  public void clearItems() {
    ensureModifiable();
    this.items.clear();
    recalculateTotal();
    touch();
  }

  public void confirmPayment(String paymentReference) {
    if (paymentReference == null || paymentReference.isBlank()) {
      throw new InvalidPaymentReferenceException();
    }
    if (status == OrderStatus.PAID
        || status == OrderStatus.PREPARING
        || status == OrderStatus.DISPATCHED
        || status == OrderStatus.DELIVERED
        || status == OrderStatus.FINALIZED) {
      throw new OrderAlreadyPaidException(id);
    }
    if (status != OrderStatus.PENDING_PAYMENT) {
      throw new OrderNotPendingException(id, status.name());
    }
    this.paymentReference = paymentReference;
    this.status = OrderStatus.PAID;
    touch();
  }

  public void markPreparing() {
    if (status != OrderStatus.PAID) {
      throw new OrderNotPreparableException(id, status.name());
    }
    this.status = OrderStatus.PREPARING;
    touch();
  }

  public void markDispatched() {
    if (status != OrderStatus.PREPARING && status != OrderStatus.PAID) {
      throw new OrderNotDispatchableException(id, status.name());
    }
    this.status = OrderStatus.DISPATCHED;
    touch();
  }

  public void markDelivered() {
    if (status != OrderStatus.DISPATCHED) {
      throw new OrderNotDeliverableException(id, status.name());
    }
    this.status = OrderStatus.DELIVERED;
    touch();
  }

  public void finalizeOrder() {
    if (status != OrderStatus.DELIVERED) {
      throw new OrderNotFinalizableException(id, status.name());
    }
    this.status = OrderStatus.FINALIZED;
    touch();
  }

  public void cancel() {
    if (status == OrderStatus.FINALIZED
        || status == OrderStatus.DISPATCHED
        || status == OrderStatus.DELIVERED) {
      throw new OrderNotCancellableException(id, status.name());
    }
    this.status = OrderStatus.CANCELLED;
    touch();
  }

  public void submitForPayment(String shippingAddress) {
    if (status != OrderStatus.CART) {
      throw new OrderNotSubmittableException(id, status.name());
    }
    if (items.isEmpty()) throw new EmptyCartException(id);
    if (shippingAddress == null || shippingAddress.isBlank()) {
      throw new ShippingAddressRequiredException();
    }
    this.shippingAddress = shippingAddress;
    this.status = OrderStatus.PENDING_PAYMENT;
    touch();
  }

  public void ensureOwnedBy(String buyerId) {
    if (!this.buyerId.equals(buyerId)) {
      throw new OrderOwnershipException(id, buyerId);
    }
  }

  private void ensureModifiable() {
    if (status == OrderStatus.FINALIZED || status == OrderStatus.CANCELLED) {
      throw new OrderNotModifiableException(id, status.name());
    }
  }

  private void recalculateTotal() {
    this.totalAmount =
        items.stream().map(OrderItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private void touch() {
    this.updatedAt = Instant.now();
  }

  public String getId() {
    return id;
  }

  public String getBuyerId() {
    return buyerId;
  }

  public OrderStatus getStatus() {
    return status;
  }

  public List<OrderItem> getItems() {
    return List.copyOf(items);
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public String getShippingAddress() {
    return shippingAddress;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public String getPaymentReference() {
    return paymentReference;
  }
}
