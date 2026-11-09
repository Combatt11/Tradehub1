package application.domain.usecases.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import application.domain.entities.Order;
import application.domain.exceptions.OrderNotCancellableException;
import application.domain.exceptions.OrderNotFoundException;
import application.domain.exceptions.OrderOwnershipException;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.usecases.CancelOrderService;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelOrderService")
class CancelOrderServiceTest {

  private static final String ORDER_ID = "order-1";
  private static final String BUYER_ID = "buyer-1";

  @Mock private OrderRepositoryPort orderRepository;

  @InjectMocks private CancelOrderService service;

  @Test
  @DisplayName("cancels an order owned by the buyer")
  void cancelsOwnedOrder() {
    Order order = Order.createCart(BUYER_ID);
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
    when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

    Order result = service.cancelOrder(ORDER_ID, BUYER_ID);

    assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.CANCELLED);
    verify(orderRepository).save(order);
  }

  @Test
  @DisplayName("throws when the order does not exist")
  void throwsWhenOrderNotFound() {
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.cancelOrder(ORDER_ID, BUYER_ID))
        .isInstanceOf(OrderNotFoundException.class)
        .hasMessage("Order not found: order-1");

    verify(orderRepository, never()).save(any());
  }

  @Test
  @DisplayName("throws when the order belongs to another buyer")
  void throwsWhenOrderBelongsToAnotherBuyer() {
    Order order = Order.createCart("other-buyer");
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> service.cancelOrder(ORDER_ID, BUYER_ID))
        .isInstanceOf(OrderOwnershipException.class)
        .hasMessageContaining("does not belong to buyer: buyer-1");

    verify(orderRepository, never()).save(any());
  }

  @Test
  @DisplayName("throws when the order is already dispatched")
  void throwsWhenOrderIsNotCancellable() {
    Order order = Order.createCart(BUYER_ID);
    order.confirmPayment("PAY-1");
    order.markDispatched();
    when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> service.cancelOrder(ORDER_ID, BUYER_ID))
        .isInstanceOf(OrderNotCancellableException.class)
        .hasMessageContaining("cannot be cancelled in status: DISPATCHED");

    verify(orderRepository, never()).save(any());
  }
}
