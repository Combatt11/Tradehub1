package application.domain.exceptions;

public class InvalidProductNameException extends DomainException {
  public InvalidProductNameException() {
    super("Product name is required");
  }
}
