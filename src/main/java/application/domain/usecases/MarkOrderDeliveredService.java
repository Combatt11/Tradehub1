package application.domain.usecases;

import application.domain.entities.Order;
import application.domain.exceptions.OrderNotFoundException;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.usecases.ports.MarkOrderDeliveredUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarkOrderDeliveredService implements MarkOrderDeliveredUseCase {

  private final OrderRepositoryPort orderRepository;

  public MarkOrderDeliveredService(OrderRepositoryPort orderRepository) {
    this.orderRepository = orderRepository;
  }

  @Override
  @Transactional
  public Order markDelivered(String orderId) {
    Order order = find(orderId);
    order.markDelivered();
    return orderRepository.save(order);
  }

  private Order find(String orderId) {
    return orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
  }
}
