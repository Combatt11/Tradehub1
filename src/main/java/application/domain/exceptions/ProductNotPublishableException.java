package application.domain.exceptions;

/** Raised when a discontinued product is published again. */
public class ProductNotPublishableException extends DomainException {
  public ProductNotPublishableException(String productId, String status) {
    super("Product " + productId + " cannot be published in status: " + status);
  }
}
