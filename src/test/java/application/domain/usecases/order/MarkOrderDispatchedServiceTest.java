package application.domain.usecases.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import application.domain.entities.Order;
import application.domain.entities.OrderItem;
import application.domain.exceptions.OrderNotDispatchableException;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.usecases.MarkOrderDispatchedService;
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
@DisplayName("MarkOrderDispatchedService")
class MarkOrderDispatchedServiceTest {

  private static final String ORDER_ID = "order-1";

  @Mock private OrderRepositoryPort orderRepository;

  @InjectMocks private MarkOrderDispatchedService service;

  private Order order;

  @BeforeEach
  void setUp() {
    order = Order.createCart("buyer-1");
    order.addItem(new OrderItem("product-1", "Laptop", 1, new BigDecimal("100.00"), "wh-1"));
    order.confirmPayment("PAY-1");
  }

  private void givenOrderInStatus(Order.OrderStatus status) {
    if (status == Order.OrderStatus.PREPARING) {
      order.markPreparing();
    }
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
    when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
  }

  @Test
  @DisplayName("moves a PREPARING order to DISPATCHED")
  void marksDispatchedFromPreparing() {
    givenOrderInStatus(Order.OrderStatus.PREPARING);

    Order result = service.markDispatched(ORDER_ID);

    assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.DISPATCHED);
    verify(orderRepository).save(order);
  }

  @Test
  @DisplayName("moves a PAID order directly to DISPATCHED")
  void marksDispatchedFromPaid() {
    givenOrderInStatus(Order.OrderStatus.PAID);

    Order result = service.markDispatched(ORDER_ID);

    assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.DISPATCHED);
  }

  @Test
  @DisplayName("throws when the order is not payable or preparing")
  void throwsWhenNotReadyToDispatch() {
    Order cart = Order.createCart("buyer-1");
    cart.addItem(new OrderItem("product-1", "Laptop", 1, new BigDecimal("100.00"), "wh-1"));
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(cart));

    assertThatThrownBy(() -> service.markDispatched(ORDER_ID))
        .isInstanceOf(OrderNotDispatchableException.class)
        .hasMessageContaining("Only PREPARING/PAID orders can be dispatched");

    verify(orderRepository, never()).save(any());
  }
}
