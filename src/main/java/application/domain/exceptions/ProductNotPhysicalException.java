package application.domain.exceptions;

public class ProductNotPhysicalException extends DomainException {
  public ProductNotPhysicalException(String productId) {
    super("Digital products do not require inventory: " + productId);
  }
}
