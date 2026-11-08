package application.domain.exceptions;

public class OrderNotDeliverableException extends DomainException {
  public OrderNotDeliverableException(String orderId, String status) {
    super(
        "Only DISPATCHED orders can be marked delivered. Order "
            + orderId
            + " is in status: "
            + status);
  }
}
