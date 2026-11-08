package application.domain.exceptions;

public class OrderNotDispatchableException extends DomainException {
  public OrderNotDispatchableException(String orderId, String status) {
    super(
        "Only PREPARING/PAID orders can be dispatched. Order "
            + orderId
            + " is in status: "
            + status);
  }
}
