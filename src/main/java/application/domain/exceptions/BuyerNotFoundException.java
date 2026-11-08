package application.domain.exceptions;

public class BuyerNotFoundException extends ResourceNotFoundException {
  public BuyerNotFoundException(String id) {
    super("Buyer not found: " + id);
  }
}
