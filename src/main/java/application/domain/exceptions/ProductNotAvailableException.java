package application.domain.exceptions;

public class ProductNotAvailableException extends DomainException {
  public ProductNotAvailableException(String productId, String status) {
    super("Product " + productId + " is not available for sale. Status: " + status);
  }
}
