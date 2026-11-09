package application.domain.services;

import application.domain.models.Order;
import application.domain.ports.in.GetCartUseCase;
import application.domain.ports.out.OrderRepositoryPort;
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
