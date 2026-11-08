package application.domain.services.order;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Order;
import application.domain.ports.out.OrderRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClearCartService {

  private final OrderRepositoryPort orderRepository;

  public ClearCartService(OrderRepositoryPort orderRepository) {
    this.orderRepository = orderRepository;
  }

  @Transactional
  public Order clearCart(String buyerId) {
    Order cart =
        orderRepository
            .findActiveCartByBuyerId(buyerId)
            .orElseThrow(() -> new UnauthorizedOperationException("No active cart found"));
    cart.clearItems();
    return orderRepository.save(cart);
  }
}
