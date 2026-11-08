package application.domain.exceptions;

/** Raised when an already published product is published again. */
public class ProductAlreadyPublishedException extends DomainException {
  public ProductAlreadyPublishedException(String productId) {
    super("Product is already published: " + productId);
  }
}
