package application.domain.exceptions;

/** Raised when an email/password pair cannot authenticate a user. Mapped to HTTP 401. */
public class InvalidCredentialsException extends DomainException {
  public InvalidCredentialsException() {
    super("Invalid email or password");
  }
}
