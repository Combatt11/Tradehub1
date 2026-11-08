package application.domain.exceptions;

/** Raised when a registration attempts to reuse an existing email. Mapped to HTTP 409. */
public class DuplicateEmailException extends DomainException {
  public DuplicateEmailException(String email) {
    super("Email already registered: " + email);
  }
}
