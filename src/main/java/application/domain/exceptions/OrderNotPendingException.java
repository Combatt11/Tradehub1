package application.domain.exceptions;

/** Raised when payment is confirmed for an order that is not awaiting payment. */
public class OrderNotPendingException extends DomainException {
  public OrderNotPendingException(String orderId, String status) {
    super("Order " + orderId + " is not pending payment. Status: " + status);
  }
}
