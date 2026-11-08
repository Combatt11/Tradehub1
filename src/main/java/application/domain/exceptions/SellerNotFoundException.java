package application.domain.exceptions;

public class SellerNotFoundException extends ResourceNotFoundException {
  public SellerNotFoundException(String id) {
    super("Seller not found: " + id);
  }
}
