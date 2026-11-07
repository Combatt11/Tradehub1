package application.domain.ports.in;
import application.domain.models.Order;
public interface LogisticsOperatorPort {
    Order markPreparing(String orderId);
    Order markDispatched(String orderId);
    Order markDelivered(String orderId);
}
