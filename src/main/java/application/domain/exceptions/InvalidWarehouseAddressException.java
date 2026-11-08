package application.domain.exceptions;

/** Raised when a warehouse is created or renamed without an address. */
public class InvalidWarehouseAddressException extends DomainException {
  public InvalidWarehouseAddressException() {
    super("Warehouse address is required");
  }
}
