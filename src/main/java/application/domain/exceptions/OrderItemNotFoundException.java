package application.domain.exceptions;

/** Raised when an order operation references a product that is not part of the order. */
public class OrderItemNotFoundException extends ResourceNotFoundException {
  public OrderItemNotFoundException(String orderId, String productId) {
    super("Product " + productId + " is not part of order: " + orderId);
  }
}
