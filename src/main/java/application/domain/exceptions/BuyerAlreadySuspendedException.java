package application.domain.exceptions;

/** Raised when a buyer that is already suspended is suspended again. */
public class BuyerAlreadySuspendedException extends DomainException {
  public BuyerAlreadySuspendedException(String buyerId) {
    super("Buyer is already suspended: " + buyerId);
  }
}
