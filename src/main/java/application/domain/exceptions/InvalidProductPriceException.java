package application.domain.exceptions;

public class InvalidProductPriceException extends DomainException {
  public InvalidProductPriceException(String price) {
    super("Product price must be greater than zero: " + price);
  }
}
