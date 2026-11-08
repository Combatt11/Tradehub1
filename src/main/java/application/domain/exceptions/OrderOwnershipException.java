package application.domain.exceptions;

public class OrderOwnershipException extends UnauthorizedOperationException {
  public OrderOwnershipException(String orderId, String buyerId) {
    super("Order " + orderId + " does not belong to buyer: " + buyerId);
  }
}
