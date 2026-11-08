package application.domain.exceptions;

/** Raised when a requested quantity is zero, negative, or otherwise invalid. */
public class InvalidQuantityException extends DomainException {
  public InvalidQuantityException(String message) {
    super(message);
  }
}
