package application.domain.exceptions;

/** Raised when a seller that is already active is approved again. */
public class SellerAlreadyApprovedException extends DomainException {
  public SellerAlreadyApprovedException(String sellerId) {
    super("Seller is already approved: " + sellerId);
  }
}
