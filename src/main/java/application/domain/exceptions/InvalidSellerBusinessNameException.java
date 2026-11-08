package application.domain.exceptions;

/** Raised when a seller is created or renamed without a business name. */
public class InvalidSellerBusinessNameException extends DomainException {
  public InvalidSellerBusinessNameException() {
    super("Seller business name is required");
  }
}
