package application.domain.exceptions;

/** Raised when a warehouse that is already active is activated again. */
public class WarehouseAlreadyActiveException extends DomainException {
  public WarehouseAlreadyActiveException(String warehouseId) {
    super("Warehouse is already active: " + warehouseId);
  }
}
