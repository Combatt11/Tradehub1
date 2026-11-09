package application.domain.services;

import application.domain.models.Order;
import application.domain.ports.in.GetBuyerOrdersUseCase;
import application.domain.ports.out.OrderRepositoryPort;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetBuyerOrdersService implements GetBuyerOrdersUseCase {

  private final OrderRepositoryPort orderRepository;

  public GetBuyerOrdersService(OrderRepositoryPort orderRepository) {
    this.orderRepository = orderRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Order> getOrders(String buyerId) {
    return orderRepository.findByBuyerId(buyerId);
  }
}
