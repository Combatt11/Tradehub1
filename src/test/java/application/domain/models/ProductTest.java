package application.domain.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import application.domain.exceptions.ProductAlreadyDiscontinuedException;
import application.domain.exceptions.ProductAlreadyPublishedException;
import application.domain.exceptions.ProductAlreadySuspendedException;
import application.domain.exceptions.ProductNotPublishableException;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Product")
class ProductTest {

  private static Product product() {
    return Product.create(
        "seller-1",
        "Laptop",
        "A laptop",
        Product.ProductType.PHYSICAL,
        new BigDecimal("100.00"),
        "SKU-1");
  }

  @Test
  @DisplayName("guards publish, suspend and discontinue against repeating the current status")
  void guardsStatusTransitions() {
    Product product = product();
    assertThat(product.getStatus()).isEqualTo(Product.ProductStatus.DRAFT);

    product.publish();
    assertThat(product.getStatus()).isEqualTo(Product.ProductStatus.PUBLISHED);
    assertThatThrownBy(product::publish)
        .isInstanceOf(ProductAlreadyPublishedException.class)
        .hasMessageContaining("Product is already published");

    product.suspend();
    assertThat(product.getStatus()).isEqualTo(Product.ProductStatus.SUSPENDED);
    assertThatThrownBy(product::suspend)
        .isInstanceOf(ProductAlreadySuspendedException.class)
        .hasMessageContaining("Product is already suspended");

    product.discontinue();
    assertThat(product.getStatus()).isEqualTo(Product.ProductStatus.DISCONTINUED);
    assertThatThrownBy(product::discontinue)
        .isInstanceOf(ProductAlreadyDiscontinuedException.class)
        .hasMessageContaining("Product is already discontinued");
  }

  @Test
  @DisplayName("does not allow publishing a discontinued product")
  void rejectsPublishingDiscontinuedProduct() {
    Product product = product();
    product.discontinue();

    assertThatThrownBy(product::publish)
        .isInstanceOf(ProductNotPublishableException.class)
        .hasMessageContaining("cannot be published in status: DISCONTINUED");
  }
}
