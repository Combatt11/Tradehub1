package application.domain.exceptions;

/** Raised when a buyer that is already active is activated again. */
public class BuyerAlreadyActiveException extends DomainException {
  public BuyerAlreadyActiveException(String buyerId) {
    super("Buyer is already active: " + buyerId);
  }
}
