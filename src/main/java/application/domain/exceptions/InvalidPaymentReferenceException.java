package application.domain.exceptions;

/** Raised when a payment confirmation arrives without a reference. */
public class InvalidPaymentReferenceException extends DomainException {
  public InvalidPaymentReferenceException() {
    super("Payment reference is required");
  }
}
