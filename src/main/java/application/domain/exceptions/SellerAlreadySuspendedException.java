package application.domain.exceptions;

/** Raised when a seller that is already suspended is suspended again. */
public class SellerAlreadySuspendedException extends DomainException {
  public SellerAlreadySuspendedException(String sellerId) {
    super("Seller is already suspended: " + sellerId);
  }
}
