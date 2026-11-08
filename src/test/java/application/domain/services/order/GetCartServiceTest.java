package application.domain.services.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import application.domain.models.Order;
import application.domain.ports.out.OrderRepositoryPort;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCartService")
class GetCartServiceTest {

  private static final String BUYER_ID = "buyer-1";

  @Mock private OrderRepositoryPort orderRepository;

  @InjectMocks private GetCartService service;

  @Test
  @DisplayName("returns the existing active cart")
  void returnsExistingCart() {
    Order existing = Order.createCart(BUYER_ID);
    when(orderRepository.findActiveCartByBuyerId(BUYER_ID)).thenReturn(Optional.of(existing));

    Order result = service.getOrCreateCart(BUYER_ID);

    assertThat(result).isSameAs(existing);
    verify(orderRepository, never()).save(any());
  }

  @Test
  @DisplayName("creates and persists a new cart when none exists")
  void createsNewCart() {
    when(orderRepository.findActiveCartByBuyerId(BUYER_ID)).thenReturn(Optional.empty());
    when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

    Order result = service.getOrCreateCart(BUYER_ID);

    assertThat(result.getBuyerId()).isEqualTo(BUYER_ID);
    assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.CART);
    assertThat(result.getItems()).isEmpty();
    verify(orderRepository).save(any(Order.class));
  }
}
