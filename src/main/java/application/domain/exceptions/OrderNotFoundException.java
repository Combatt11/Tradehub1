package application.domain.exceptions;

public class OrderNotFoundException extends ResourceNotFoundException {
  public OrderNotFoundException(String id) {
    super("Order not found: " + id);
  }
}
