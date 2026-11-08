package application.domain.exceptions;

/** Raised when a product has no available units in the requested warehouse. */
public class ProductOutOfStockException extends DomainException {
  public ProductOutOfStockException(String productId) {
    super("Product is out of stock: " + productId);
  }
}
