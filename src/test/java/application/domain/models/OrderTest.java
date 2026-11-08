package application.domain.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import application.domain.exceptions.OrderItemNotFoundException;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Order")
class OrderTest {

  private static Order cartWithItem() {
    Order cart = Order.createCart("buyer-1");
    cart.addItem(new OrderItem("product-1", "Laptop", 1, new BigDecimal("100.00"), "wh-1"));
    return cart;
  }

  @Test
  @DisplayName("throws when removing a product that is not part of the order")
  void rejectsRemovingMissingItem() {
    Order cart = cartWithItem();

    assertThatThrownBy(() -> cart.removeItem("product-2"))
        .isInstanceOf(OrderItemNotFoundException.class)
        .hasMessageContaining("Product product-2 is not part of order");
  }

  @Test
  @DisplayName("removes an item and recalculates the total")
  void removesExistingItem() {
    Order cart = cartWithItem();

    cart.removeItem("product-1");

    assertThat(cart.getItems()).isEmpty();
    assertThat(cart.getTotalAmount()).isEqualByComparingTo("0.00");
  }
}
