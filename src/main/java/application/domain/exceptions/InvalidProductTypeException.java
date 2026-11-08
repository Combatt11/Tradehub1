package application.domain.exceptions;

public class InvalidProductTypeException extends DomainException {
  public InvalidProductTypeException(String type) {
    super("Invalid product type: " + type + ". Expected PHYSICAL or DIGITAL");
  }
}
