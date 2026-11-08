package application.domain.services.order;

import application.domain.exceptions.OrderNotFoundException;
import application.domain.models.Order;
import application.domain.ports.in.MarkOrderDeliveredUseCase;
import application.domain.ports.out.OrderRepositoryPort;
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
