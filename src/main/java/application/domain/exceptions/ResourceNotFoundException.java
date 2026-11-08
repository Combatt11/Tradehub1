package application.domain.exceptions;

/** Base type for domain resources that cannot be found. Mapped to HTTP 404. */
public class ResourceNotFoundException extends DomainException {
  public ResourceNotFoundException(String message) {
    super(message);
  }
}
