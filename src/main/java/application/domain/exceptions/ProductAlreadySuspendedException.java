package application.domain.exceptions;

/** Raised when an already suspended product is suspended again. */
public class ProductAlreadySuspendedException extends DomainException {
  public ProductAlreadySuspendedException(String productId) {
    super("Product is already suspended: " + productId);
  }
}
