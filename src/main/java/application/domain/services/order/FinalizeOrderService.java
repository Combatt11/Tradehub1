package application.domain.services.order;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Order;
import application.domain.ports.out.OrderRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinalizeOrderService {

  private final OrderRepositoryPort orderRepository;

  public FinalizeOrderService(OrderRepositoryPort orderRepository) {
    this.orderRepository = orderRepository;
  }

  @Transactional
  public Order finalizeOrder(String orderId) {
    Order order = find(orderId);
    order.finalizeOrder();
    return orderRepository.save(order);
  }

  private Order find(String orderId) {
    return orderRepository
        .findById(orderId)
        .orElseThrow(() -> new UnauthorizedOperationException("Order not found"));
  }
}
