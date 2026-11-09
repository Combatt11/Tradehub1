package application.domain.usecases;

import application.domain.entities.Order;
import application.domain.exceptions.OrderNotFoundException;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.usecases.ports.FinalizeOrderUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinalizeOrderService implements FinalizeOrderUseCase {

  private final OrderRepositoryPort orderRepository;

  public FinalizeOrderService(OrderRepositoryPort orderRepository) {
    this.orderRepository = orderRepository;
  }

  @Override
  @Transactional
  public Order finalizeOrder(String orderId) {
    Order order = find(orderId);
    order.finalizeOrder();
    return orderRepository.save(order);
  }

  private Order find(String orderId) {
    return orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
  }
}
