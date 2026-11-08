package application.domain.exceptions;

/** Raised when a warehouse is created or renamed without a name. */
public class InvalidWarehouseNameException extends DomainException {
  public InvalidWarehouseNameException() {
    super("Warehouse name is required");
  }
}
