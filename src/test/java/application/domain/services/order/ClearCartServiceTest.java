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
@DisplayName("ClearCartService")
class ClearCartServiceTest {

  private static final String BUYER_ID = "buyer-1";

  @Mock private OrderRepositoryPort orderRepository;

  @InjectMocks private ClearCartService service;

  @Test
  @DisplayName("removes every item from the active cart and resets the total")
  void clearsCartItems() {
    Order cart = Order.createCart(BUYER_ID);
    cart.addItem(new OrderItem("product-1", "Laptop", 2, new BigDecimal("50.00"), "wh-1"));
    when(orderRepository.findActiveCartByBuyerId(BUYER_ID)).thenReturn(Optional.of(cart));
    when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

    Order result = service.clearCart(BUYER_ID);

    assertThat(result.getItems()).isEmpty();
    assertThat(result.getTotalAmount()).isEqualByComparingTo("0.00");
    assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.CART);
    verify(orderRepository).save(cart);
  }

  @Test
  @DisplayName("throws when there is no active cart")
  void throwsWhenNoActiveCart() {
    when(orderRepository.findActiveCartByBuyerId(BUYER_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.clearCart(BUYER_ID))
        .isInstanceOf(UnauthorizedOperationException.class)
        .hasMessage("No active cart found");

    verify(orderRepository, never()).save(any());
  }
}
