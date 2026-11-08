package application.domain.exceptions;

public class OrderAlreadyPaidException extends DomainException {
  public OrderAlreadyPaidException(String orderId) {
    super("Order has already been paid: " + orderId);
  }
}
