package application.domain.services.order;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Order;
import application.domain.ports.out.OrderRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubmitOrderFromCartService {

  private final OrderRepositoryPort orderRepository;

  public SubmitOrderFromCartService(OrderRepositoryPort orderRepository) {
    this.orderRepository = orderRepository;
  }

  @Transactional
  public Order submitOrder(String buyerId, String shippingAddress) {
    Order cart =
        orderRepository
            .findActiveCartByBuyerId(buyerId)
            .orElseThrow(() -> new UnauthorizedOperationException("No active cart found"));
    cart.submitForPayment(shippingAddress);
    return orderRepository.save(cart);
  }
}
