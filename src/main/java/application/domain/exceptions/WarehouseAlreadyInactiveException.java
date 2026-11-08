package application.domain.exceptions;

/** Raised when a warehouse that is already inactive is deactivated again. */
public class WarehouseAlreadyInactiveException extends DomainException {
  public WarehouseAlreadyInactiveException(String warehouseId) {
    super("Warehouse is already inactive: " + warehouseId);
  }
}
