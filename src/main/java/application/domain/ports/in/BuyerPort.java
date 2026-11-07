package application.domain.ports.in;
import application.domain.models.Order;
import application.domain.models.Product;
import java.util.List;
public interface BuyerPort {
    List<Product> browseCatalog();
    Order getOrCreateCart(String buyerId);
    Order addToCart(String buyerId, String productId, int quantity, String warehouseId);
    Order submitOrder(String buyerId, String shippingAddress);
    Order confirmPayment(String orderId, String buyerId, String paymentReference);
    List<Order> myOrders(String buyerId);
}
