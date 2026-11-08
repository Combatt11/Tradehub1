package application.domain.exceptions;

public class WarehouseNotActiveException extends DomainException {
  public WarehouseNotActiveException(String warehouseId, String status) {
    super("Warehouse " + warehouseId + " is not active. Status: " + status);
  }
}
