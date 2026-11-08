package application.domain.services.order;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Order;
import application.domain.ports.out.OrderRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfirmPaymentService {

  private final OrderRepositoryPort orderRepository;

  public ConfirmPaymentService(OrderRepositoryPort orderRepository) {
    this.orderRepository = orderRepository;
  }

  @Transactional
  public Order confirmPayment(String orderId, String buyerId, String paymentReference) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new UnauthorizedOperationException("Order not found"));
    if (!order.getBuyerId().equals(buyerId))
      throw new UnauthorizedOperationException("Order does not belong to this buyer");
    order.confirmPayment(paymentReference);
    return orderRepository.save(order);
  }
}
