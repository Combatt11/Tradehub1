package application.domain.usecases;

import application.domain.entities.Order;
import application.domain.exceptions.CartNotFoundException;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.usecases.ports.ClearCartUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClearCartService implements ClearCartUseCase {

  private final OrderRepositoryPort orderRepository;

  public ClearCartService(OrderRepositoryPort orderRepository) {
    this.orderRepository = orderRepository;
  }

  @Override
  @Transactional
  public Order clearCart(String buyerId) {
    Order cart =
        orderRepository
            .findActiveCartByBuyerId(buyerId)
            .orElseThrow(() -> new CartNotFoundException(buyerId));
    cart.clearItems();
    return orderRepository.save(cart);
  }
}
