package application.domain.services.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import application.domain.exceptions.OrderAlreadyPaidException;
import application.domain.exceptions.OrderNotFoundException;
import application.domain.exceptions.OrderOwnershipException;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.services.ConfirmPaymentService;
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
@DisplayName("ConfirmPaymentService")
class ConfirmPaymentServiceTest {

  private static final String ORDER_ID = "order-1";
  private static final String BUYER_ID = "buyer-1";

  @Mock private OrderRepositoryPort orderRepository;

  @InjectMocks private ConfirmPaymentService service;

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
          .isInstanceOf(OrderNotFoundException.class)
          .hasMessage("Order not found: order-1");

      verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("throws when the order belongs to another buyer")
    void throwsWhenOrderBelongsToAnotherBuyer() {
      givenOrderInStatus(Order.OrderStatus.PENDING_PAYMENT);

      assertThatThrownBy(() -> service.confirmPayment(ORDER_ID, "other-buyer", "PAY-123"))
          .isInstanceOf(OrderOwnershipException.class)
          .hasMessageContaining("does not belong to buyer: other-buyer");

      verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("throws when the order is already finalized")
    void throwsWhenOrderAlreadyFinalized() {
      givenOrderInStatus(Order.OrderStatus.FINALIZED);

      assertThatThrownBy(() -> service.confirmPayment(ORDER_ID, BUYER_ID, "PAY-123"))
          .isInstanceOf(OrderAlreadyPaidException.class)
          .hasMessageContaining("has already been paid");

      verify(orderRepository, never()).save(any());
    }
  }
}
