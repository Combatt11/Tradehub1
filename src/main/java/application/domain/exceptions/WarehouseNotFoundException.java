package application.domain.exceptions;

public class WarehouseNotFoundException extends ResourceNotFoundException {
  public WarehouseNotFoundException(String id) {
    super("Warehouse not found: " + id);
  }
}
