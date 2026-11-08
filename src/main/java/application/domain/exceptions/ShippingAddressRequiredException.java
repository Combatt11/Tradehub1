package application.domain.exceptions;

public class ShippingAddressRequiredException extends DomainException {
  public ShippingAddressRequiredException() {
    super("Shipping address is required");
  }
}
