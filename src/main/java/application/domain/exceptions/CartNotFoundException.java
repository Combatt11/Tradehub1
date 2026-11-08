package application.domain.exceptions;

public class CartNotFoundException extends ResourceNotFoundException {
  public CartNotFoundException(String buyerId) {
    super("No active cart found for buyer: " + buyerId);
  }
}
