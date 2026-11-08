package application.domain.exceptions;

public class OrderNotFinalizableException extends DomainException {
  public OrderNotFinalizableException(String orderId, String status) {
    super("Only DELIVERED orders can be finalized. Order " + orderId + " is in status: " + status);
  }
}
