package application.domain.exceptions;

/** Raised when a seller warehouse is registered without a seller. */
public class SellerRequiredForWarehouseException extends DomainException {
  public SellerRequiredForWarehouseException() {
    super("A seller is required to create a seller warehouse");
  }
}
