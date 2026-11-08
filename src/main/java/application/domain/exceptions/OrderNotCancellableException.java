package application.domain.exceptions;

public class OrderNotCancellableException extends DomainException {
  public OrderNotCancellableException(String orderId, String status) {
    super("Order " + orderId + " cannot be cancelled in status: " + status);
  }
}
