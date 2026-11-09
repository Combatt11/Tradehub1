package application.domain.usecases;

import application.domain.entities.Order;
import application.domain.exceptions.OrderNotFoundException;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.usecases.ports.MarkOrderPreparingUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarkOrderPreparingService implements MarkOrderPreparingUseCase {

  private final OrderRepositoryPort orderRepository;

  public MarkOrderPreparingService(OrderRepositoryPort orderRepository) {
    this.orderRepository = orderRepository;
  }

  @Override
  @Transactional
  public Order markPreparing(String orderId) {
    Order order = find(orderId);
    order.markPreparing();
    return orderRepository.save(order);
  }

  private Order find(String orderId) {
    return orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
  }
}
