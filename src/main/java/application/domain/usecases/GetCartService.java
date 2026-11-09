package application.domain.usecases;

import application.domain.entities.Order;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.usecases.ports.GetCartUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetCartService implements GetCartUseCase {

  private final OrderRepositoryPort orderRepository;

  public GetCartService(OrderRepositoryPort orderRepository) {
    this.orderRepository = orderRepository;
  }

  @Override
  @Transactional
  public Order getOrCreateCart(String buyerId) {
    return orderRepository
        .findActiveCartByBuyerId(buyerId)
        .orElseGet(() -> orderRepository.save(Order.createCart(buyerId)));
  }
}
