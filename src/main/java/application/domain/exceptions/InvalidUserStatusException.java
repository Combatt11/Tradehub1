package application.domain.exceptions;

/** Raised when a status change is requested without a target status. */
public class InvalidUserStatusException extends DomainException {
  public InvalidUserStatusException() {
    super("User status is required");
  }
}
