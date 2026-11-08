package application.domain.exceptions;

/** Raised when a physical product is added to a cart without a warehouse. */
public class WarehouseRequiredException extends DomainException {
  public WarehouseRequiredException(String productId) {
    super("A warehouse is required for physical product: " + productId);
  }
}
