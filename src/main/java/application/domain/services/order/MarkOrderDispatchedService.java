package application.domain.services.order;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Order;
import application.domain.ports.out.OrderRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarkOrderDispatchedService {

  private final OrderRepositoryPort orderRepository;

  public MarkOrderDispatchedService(OrderRepositoryPort orderRepository) {
    this.orderRepository = orderRepository;
  }

  @Transactional
  public Order markDispatched(String orderId) {
    Order order = find(orderId);
    order.markDispatched();
    return orderRepository.save(order);
  }

  private Order find(String orderId) {
    return orderRepository
        .findById(orderId)
        .orElseThrow(() -> new UnauthorizedOperationException("Order not found"));
  }
}
