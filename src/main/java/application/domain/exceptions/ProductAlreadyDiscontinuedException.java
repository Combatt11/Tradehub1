package application.domain.exceptions;

/** Raised when an already discontinued product is discontinued again. */
public class ProductAlreadyDiscontinuedException extends DomainException {
  public ProductAlreadyDiscontinuedException(String productId) {
    super("Product is already discontinued: " + productId);
  }
}
