package application.domain.exceptions;

/** Raised when a buyer address is missing or blank. */
public class BuyerAddressRequiredException extends DomainException {
  public BuyerAddressRequiredException() {
    super("Buyer address is required");
  }
}
