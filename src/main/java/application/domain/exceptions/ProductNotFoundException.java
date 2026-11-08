package application.domain.exceptions;

public class ProductNotFoundException extends ResourceNotFoundException {
  public ProductNotFoundException(String id) {
    super("Product not found: " + id);
  }
}
