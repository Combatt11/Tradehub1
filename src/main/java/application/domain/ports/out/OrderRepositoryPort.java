package application.domain.ports.out;

import application.domain.entities.Order;
import java.util.List;
import java.util.Optional;

public interface OrderRepositoryPort {
  Order save(Order order);

  Optional<Order> findById(String id);

  List<Order> findByBuyerId(String buyerId);

  Optional<Order> findActiveCartByBuyerId(String buyerId);

  List<Order> findAll();
}
