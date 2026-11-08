package application.domain.services.order;

import application.domain.exceptions.CartNotFoundException;
import application.domain.models.Order;
import application.domain.ports.in.SubmitOrderFromCartUseCase;
import application.domain.ports.out.OrderRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubmitOrderFromCartService implements SubmitOrderFromCartUseCase {

  private final OrderRepositoryPort orderRepository;

  public SubmitOrderFromCartService(OrderRepositoryPort orderRepository) {
    this.orderRepository = orderRepository;
  }

  @Override
  @Transactional
  public Order submitOrder(String buyerId, String shippingAddress) {
    Order cart =
        orderRepository
            .findActiveCartByBuyerId(buyerId)
            .orElseThrow(() -> new CartNotFoundException(buyerId));
    cart.submitForPayment(shippingAddress);
    return orderRepository.save(cart);
  }
}
