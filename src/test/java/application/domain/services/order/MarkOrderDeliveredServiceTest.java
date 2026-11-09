package application.domain.services.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import application.domain.exceptions.OrderNotDeliverableException;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.MarkOrderDeliveredService;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("MarkOrderDeliveredService")
class MarkOrderDeliveredServiceTest {

  private static final String ORDER_ID = "order-1";

  @Mock private OrderRepositoryPort orderRepository;

  @InjectMocks private MarkOrderDeliveredService service;

  private Order order;

  @BeforeEach
  void setUp() {
    order = Order.createCart("buyer-1");
    order.addItem(new OrderItem("product-1", "Laptop", 1, new BigDecimal("100.00"), "wh-1"));
    order.confirmPayment("PAY-1");
  }

  @Test
  @DisplayName("moves a DISPATCHED order to DELIVERED")
  void marksDelivered() {
    order.markDispatched();
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
    when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

    Order result = service.markDelivered(ORDER_ID);

    assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.DELIVERED);
    verify(orderRepository).save(order);
  }

  @Test
  @DisplayName("throws when the order was not dispatched")
  void throwsWhenNotDispatched() {
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> service.markDelivered(ORDER_ID))
        .isInstanceOf(OrderNotDeliverableException.class)
        .hasMessageContaining("Only DISPATCHED orders can be marked delivered");

    verify(orderRepository, never()).save(any());
  }
}
