package application.domain.services.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.ports.out.OrderRepositoryPort;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubmitOrderFromCartService")
class SubmitOrderFromCartServiceTest {

  private static final String BUYER_ID = "buyer-1";

  @Mock private OrderRepositoryPort orderRepository;

  @InjectMocks private SubmitOrderFromCartService service;

  @Test
  @DisplayName("submits a non-empty cart for payment")
  void submitsCart() {
    Order cart = Order.createCart(BUYER_ID);
    cart.addItem(new OrderItem("product-1", "Laptop", 1, new BigDecimal("100.00"), "wh-1"));
    when(orderRepository.findActiveCartByBuyerId(BUYER_ID)).thenReturn(Optional.of(cart));
    when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

    Order result = service.submitOrder(BUYER_ID, "Calle 99");

    assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.PENDING_PAYMENT);
    assertThat(result.getShippingAddress()).isEqualTo("Calle 99");
    verify(orderRepository).save(cart);
  }

  @Test
  @DisplayName("throws when there is no active cart")
  void throwsWhenNoActiveCart() {
    when(orderRepository.findActiveCartByBuyerId(BUYER_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.submitOrder(BUYER_ID, "Calle 99"))
        .isInstanceOf(UnauthorizedOperationException.class)
        .hasMessage("No active cart found");

    verify(orderRepository, never()).save(any());
  }

  @Test
  @DisplayName("throws when the cart is empty")
  void throwsWhenCartIsEmpty() {
    Order emptyCart = Order.createCart(BUYER_ID);
    when(orderRepository.findActiveCartByBuyerId(BUYER_ID)).thenReturn(Optional.of(emptyCart));

    assertThatThrownBy(() -> service.submitOrder(BUYER_ID, "Calle 99"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Cart is empty");

    verify(orderRepository, never()).save(any());
  }
}
