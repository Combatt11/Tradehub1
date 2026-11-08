package application.domain.exceptions;

/**
 * Raised when an authenticated actor is not allowed to perform an operation. Mapped to HTTP 403.
 */
public class UnauthorizedOperationException extends DomainException {
  public UnauthorizedOperationException(String message) {
    super(message);
  }
}
