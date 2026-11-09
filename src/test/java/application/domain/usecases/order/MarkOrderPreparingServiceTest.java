package application.domain.usecases.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import application.domain.entities.Order;
import application.domain.entities.OrderItem;
import application.domain.exceptions.OrderNotFoundException;
import application.domain.exceptions.OrderNotPreparableException;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.usecases.MarkOrderPreparingService;
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
@DisplayName("MarkOrderPreparingService")
class MarkOrderPreparingServiceTest {

  private static final String ORDER_ID = "order-1";

  @Mock private OrderRepositoryPort orderRepository;

  @InjectMocks private MarkOrderPreparingService service;

  private Order order;

  @BeforeEach
  void setUp() {
    order = Order.createCart("buyer-1");
    order.addItem(new OrderItem("product-1", "Laptop", 1, new BigDecimal("100.00"), "wh-1"));
  }

  private void givenOrderInStatus(Order.OrderStatus status) {
    if (status == Order.OrderStatus.PAID) {
      order.confirmPayment("PAY-1");
    }
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
  }

  @Test
  @DisplayName("moves a PAID order to PREPARING")
  void marksPreparing() {
    givenOrderInStatus(Order.OrderStatus.PAID);
    when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

    Order result = service.markPreparing(ORDER_ID);

    assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.PREPARING);
    verify(orderRepository).save(order);
  }

  @Test
  @DisplayName("throws when the order is not PAID")
  void throwsWhenNotPaid() {
    givenOrderInStatus(Order.OrderStatus.CART);

    assertThatThrownBy(() -> service.markPreparing(ORDER_ID))
        .isInstanceOf(OrderNotPreparableException.class)
        .hasMessageContaining("Only PAID orders can be prepared");

    verify(orderRepository, never()).save(any());
  }

  @Test
  @DisplayName("throws when the order does not exist")
  void throwsWhenOrderNotFound() {
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.markPreparing(ORDER_ID))
        .isInstanceOf(OrderNotFoundException.class)
        .hasMessage("Order not found: order-1");

    verify(orderRepository, never()).save(any());
  }
}
