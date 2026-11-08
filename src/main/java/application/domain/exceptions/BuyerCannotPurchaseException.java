package application.domain.exceptions;

public class BuyerCannotPurchaseException extends UnauthorizedOperationException {
  public BuyerCannotPurchaseException(String buyerId, String status) {
    super("Buyer " + buyerId + " cannot purchase in status: " + status);
  }
}
