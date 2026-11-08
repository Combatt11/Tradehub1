package application.domain.ports.in;

import application.domain.models.Order;
import java.util.List;

public interface GetBuyerOrdersUseCase {
  List<Order> getOrders(String buyerId);
}
