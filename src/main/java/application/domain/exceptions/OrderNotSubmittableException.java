package application.domain.exceptions;

public class OrderNotSubmittableException extends DomainException {
  public OrderNotSubmittableException(String orderId, String status) {
    super("Only CART orders can be submitted. Order " + orderId + " is in status: " + status);
  }
}
