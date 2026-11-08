package application.domain.exceptions;

/** Raised when an order line carries a non-positive unit price. */
public class InvalidOrderItemPriceException extends DomainException {
  public InvalidOrderItemPriceException(String price) {
    super("Order item unit price must be greater than zero: " + price);
  }
}
