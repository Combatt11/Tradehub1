package application.domain.models;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import application.domain.exceptions.InvalidOrderItemPriceException;
import application.domain.exceptions.InvalidQuantityException;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OrderItem")
class OrderItemTest {

  @Test
  @DisplayName("rejects non-positive quantities")
  void rejectsNonPositiveQuantity() {
    assertThatThrownBy(
            () -> new OrderItem("product-1", "Laptop", 0, new BigDecimal("10.00"), "wh-1"))
        .isInstanceOf(InvalidQuantityException.class)
        .hasMessage("Order item quantity must be positive");
  }

  @Test
  @DisplayName("rejects null or non-positive unit prices")
  void rejectsInvalidUnitPrice() {
    assertThatThrownBy(() -> new OrderItem("product-1", "Laptop", 1, null, "wh-1"))
        .isInstanceOf(InvalidOrderItemPriceException.class)
        .hasMessageContaining("unit price must be greater than zero");

    assertThatThrownBy(() -> new OrderItem("product-1", "Laptop", 1, BigDecimal.ZERO, "wh-1"))
        .isInstanceOf(InvalidOrderItemPriceException.class);
  }
}
