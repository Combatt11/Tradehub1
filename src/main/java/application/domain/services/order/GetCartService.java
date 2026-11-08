package application.domain.services.order;

import application.domain.models.Order;
import application.domain.ports.out.OrderRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetCartService {

  private final OrderRepositoryPort orderRepository;

  public GetCartService(OrderRepositoryPort orderRepository) {
    this.orderRepository = orderRepository;
  }

  @Transactional
  public Order getOrCreateCart(String buyerId) {
    return orderRepository
        .findActiveCartByBuyerId(buyerId)
        .orElseGet(() -> orderRepository.save(Order.createCart(buyerId)));
  }
}
