package application.domain.exceptions;
public class OrderNotModifiableException extends DomainException {
    public OrderNotModifiableException(String orderId, String status) {
        super("Order " + orderId + " cannot be modified in status: " + status);
    }
}
