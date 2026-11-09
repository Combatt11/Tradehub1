package application.domain.usecases;

import application.domain.entities.Order;
import application.domain.exceptions.OrderNotFoundException;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.usecases.ports.CancelOrderUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelOrderService implements CancelOrderUseCase {

  private final OrderRepositoryPort orderRepository;

  public CancelOrderService(OrderRepositoryPort orderRepository) {
    this.orderRepository = orderRepository;
  }

  @Override
  @Transactional
  public Order cancelOrder(String orderId, String buyerId) {
    Order order =
        orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
    order.ensureOwnedBy(buyerId);
    order.cancel();
    return orderRepository.save(order);
  }
}
