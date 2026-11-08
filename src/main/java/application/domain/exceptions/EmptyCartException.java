package application.domain.exceptions;

public class EmptyCartException extends DomainException {
  public EmptyCartException(String orderId) {
    super("Cart is empty: " + orderId);
  }
}
