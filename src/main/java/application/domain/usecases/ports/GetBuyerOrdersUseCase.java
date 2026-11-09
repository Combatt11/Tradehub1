package application.domain.usecases.ports;

import application.domain.entities.Order;
import java.util.List;

public interface GetBuyerOrdersUseCase {
  List<Order> getOrders(String buyerId);
}
