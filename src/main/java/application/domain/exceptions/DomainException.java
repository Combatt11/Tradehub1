package application.domain.exceptions;

/** Base type for every expected business error raised by the domain. */
public class DomainException extends RuntimeException {
  public DomainException(String message) {
    super(message);
  }
}
