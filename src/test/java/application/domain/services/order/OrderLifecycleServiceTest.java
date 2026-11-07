package application.domain.services.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.ports.out.OrderRepositoryPort;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderLifecycleService")
class OrderLifecycleServiceTest {

  private static final String ORDER_ID = "order-1";
  private static final String BUYER_ID = "buyer-1";

  @Mock private OrderRepositoryPort orderRepository;

  @InjectMocks private OrderLifecycleService service;

  private Order order;

  @BeforeEach
  void setUp() {
    order = Order.createCart(BUYER_ID);
    order.addItem(new OrderItem("product-1", "Laptop", 1, new BigDecimal("100.00"), "wh-1"));
  }

  private void givenOrderInStatus(Order.OrderStatus status) {
    switch (status) {
      case CART -> {}
      case PENDING_PAYMENT -> order.submitForPayment("Calle 1");
      case PAID -> order.confirmPayment("PAY-1");
      case PREPARING -> {
        order.confirmPayment("PAY-1");
        order.markPreparing();
      }
      case DISPATCHED -> {
        order.confirmPayment("PAY-1");
        order.markDispatched();
      }
      case DELIVERED -> {
        order.confirmPayment("PAY-1");
        order.markDispatched();
        order.markDelivered();
      }
      case FINALIZED -> {
        order.confirmPayment("PAY-1");
        order.markDispatched();
        order.markDelivered();
        order.finalizeOrder();
      }
      case CANCELLED -> order.cancel();
    }
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
    lenient().when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
  }

  @Nested
  @DisplayName("confirmPayment")
  class ConfirmPayment {

    @Test
    @DisplayName("marks a pending order as PAID and stores the payment reference")
    void confirmsPayment() {
      givenOrderInStatus(Order.OrderStatus.PENDING_PAYMENT);

      Order result = service.confirmPayment(ORDER_ID, BUYER_ID, "PAY-123");

      assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.PAID);
      assertThat(result.getPaymentReference()).isEqualTo("PAY-123");
      verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("throws when the order does not exist")
    void throwsWhenOrderNotFound() {
      when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.confirmPayment(ORDER_ID, BUYER_ID, "PAY-123"))
          .isInstanceOf(UnauthorizedOperationException.class)
          .hasMessage("Order not found");

      verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("throws when the order belongs to another buyer")
    void throwsWhenOrderBelongsToAnotherBuyer() {
      givenOrderInStatus(Order.OrderStatus.PENDING_PAYMENT);

      assertThatThrownBy(() -> service.confirmPayment(ORDER_ID, "other-buyer", "PAY-123"))
          .isInstanceOf(UnauthorizedOperationException.class)
          .hasMessage("Order does not belong to this buyer");

      verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("throws when the order is already finalized")
    void throwsWhenOrderAlreadyFinalized() {
      givenOrderInStatus(Order.OrderStatus.FINALIZED);

      assertThatThrownBy(() -> service.confirmPayment(ORDER_ID, BUYER_ID, "PAY-123"))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("cannot accept payment");

      verify(orderRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("markPreparing")
  class MarkPreparing {

    @Test
    @DisplayName("moves a PAID order to PREPARING")
    void marksPreparing() {
      givenOrderInStatus(Order.OrderStatus.PAID);

      Order result = service.markPreparing(ORDER_ID);

      assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.PREPARING);
      verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("throws when the order is not PAID")
    void throwsWhenNotPaid() {
      givenOrderInStatus(Order.OrderStatus.CART);

      assertThatThrownBy(() -> service.markPreparing(ORDER_ID))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("Only PAID orders can be prepared");

      verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("throws when the order does not exist")
    void throwsWhenOrderNotFound() {
      when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.markPreparing(ORDER_ID))
          .isInstanceOf(UnauthorizedOperationException.class)
          .hasMessage("Order not found");
    }
  }

  @Nested
  @DisplayName("markDispatched")
  class MarkDispatched {

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
    @DisplayName("throws when the order is still in CART")
    void throwsWhenInCart() {
      givenOrderInStatus(Order.OrderStatus.CART);

      assertThatThrownBy(() -> service.markDispatched(ORDER_ID))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("Only PREPARING/PAID orders can be dispatched");

      verify(orderRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("markDelivered")
  class MarkDelivered {

    @Test
    @DisplayName("moves a DISPATCHED order to DELIVERED")
    void marksDelivered() {
      givenOrderInStatus(Order.OrderStatus.DISPATCHED);

      Order result = service.markDelivered(ORDER_ID);

      assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.DELIVERED);
      verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("throws when the order was not dispatched")
    void throwsWhenNotDispatched() {
      givenOrderInStatus(Order.OrderStatus.PAID);

      assertThatThrownBy(() -> service.markDelivered(ORDER_ID))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("Only DISPATCHED orders can be marked delivered");

      verify(orderRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("finalizeOrder")
  class FinalizeOrder {

    @Test
    @DisplayName("moves a DELIVERED order to FINALIZED")
    void finalizesOrder() {
      givenOrderInStatus(Order.OrderStatus.DELIVERED);

      Order result = service.finalizeOrder(ORDER_ID);

      assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.FINALIZED);
      verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("throws when the order was not delivered")
    void throwsWhenNotDelivered() {
      givenOrderInStatus(Order.OrderStatus.DISPATCHED);

      assertThatThrownBy(() -> service.finalizeOrder(ORDER_ID))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("Only DELIVERED orders can be finalized");

      verify(orderRepository, never()).save(any());
    }
  }
}
