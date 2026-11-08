package application.domain.exceptions;

public class OrderNotPreparableException extends DomainException {
  public OrderNotPreparableException(String orderId, String status) {
    super("Only PAID orders can be prepared. Order " + orderId + " is in status: " + status);
  }
}
