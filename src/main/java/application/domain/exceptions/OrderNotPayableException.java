package application.domain.exceptions;

public class OrderNotPayableException extends DomainException {
  public OrderNotPayableException(String orderId, String status) {
    super("Order " + orderId + " cannot accept payment in status: " + status);
  }
}
