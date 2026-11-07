package application.domain.exceptions;
public class InsufficientStockException extends DomainException {
    public InsufficientStockException(String productId, int requested, int available) {
        super(String.format("Insufficient stock for product %s. Requested: %d, Available: %d", productId, requested, available));
    }
}
