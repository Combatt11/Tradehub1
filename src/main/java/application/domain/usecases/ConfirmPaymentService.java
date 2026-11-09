package application.domain.usecases;

import application.domain.entities.Order;
import application.domain.exceptions.OrderNotFoundException;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.usecases.ports.ConfirmPaymentUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfirmPaymentService implements ConfirmPaymentUseCase {

  private final OrderRepositoryPort orderRepository;

  public ConfirmPaymentService(OrderRepositoryPort orderRepository) {
    this.orderRepository = orderRepository;
  }

  @Override
  @Transactional
  public Order confirmPayment(String orderId, String buyerId, String paymentReference) {
    Order order =
        orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
    order.ensureOwnedBy(buyerId);
    order.confirmPayment(paymentReference);
    return orderRepository.save(order);
  }
}
