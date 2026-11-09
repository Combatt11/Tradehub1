package application.adapters.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import application.adapters.persistence.jpa.entities.OrderJpaEntity;
import application.adapters.persistence.jpa.mappers.OrderJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaOrderRepository;
import application.domain.entities.Order;
import application.domain.entities.OrderItem;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@Import({OrderJpaMapper.class, OrderJpaAdapter.class})
@DisplayName("OrderJpaAdapter persistence")
class OrderRepositoryJpaTest {

  @Autowired private SpringDataJpaOrderRepository repository;
  @Autowired private OrderJpaAdapter adapter;
  @Autowired private OrderJpaMapper mapper;

  private static OrderJpaEntity entity(String id, String buyerId) {
    OrderJpaEntity entity = new OrderJpaEntity();
    entity.setId(id);
    entity.setBuyerId(buyerId);
    entity.setStatus("CART");
    entity.setTotalAmount(new BigDecimal("20.00"));
    entity.setShippingAddress("Calle 1");
    entity.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
    entity.setUpdatedAt(Instant.parse("2024-01-01T00:00:00Z"));
    entity.setPaymentReference(null);
    entity.setItemsJson("[]");
    return entity;
  }

  private static Order cart(String buyerId) {
    Order cart = Order.createCart(buyerId);
    cart.addItem(new OrderItem("product-1", "Laptop", 2, new BigDecimal("10.00"), "warehouse-1"));
    return cart;
  }

  @Test
  @DisplayName("save and findById round-trip order items and totals")
  void saveAndFindByIdRoundTripsItemsAndTotal() {
    Order saved = adapter.save(cart("buyer-1"));

    Order found = adapter.findById(saved.getId()).orElseThrow();
    assertThat(found.getBuyerId()).isEqualTo("buyer-1");
    assertThat(found.getStatus()).isEqualTo(Order.OrderStatus.CART);
    assertThat(found.getItems()).hasSize(1);
    assertThat(found.getItems().get(0).getProductId()).isEqualTo("product-1");
    assertThat(found.getItems().get(0).getQuantity()).isEqualTo(2);
    assertThat(found.getTotalAmount()).isEqualByComparingTo("20.00");
  }

  @Test
  @DisplayName("findByBuyerId returns only that buyer's orders")
  void findByBuyerIdReturnsOwnedOrders() {
    adapter.save(cart("buyer-1"));
    adapter.save(cart("buyer-2"));

    assertThat(adapter.findByBuyerId("buyer-1")).hasSize(1);
  }

  @Test
  @DisplayName("findActiveCartByBuyerId returns the cart")
  void findActiveCartByBuyerIdReturnsCart() {
    Order saved = adapter.save(cart("buyer-1"));

    assertThat(adapter.findActiveCartByBuyerId("buyer-1"))
        .isPresent()
        .get()
        .extracting(Order::getId)
        .isEqualTo(saved.getId());
  }

  @Test
  @DisplayName("findActiveCartByBuyerId ignores non-cart orders")
  void findActiveCartByBuyerIdIgnoresPaidOrders() {
    Order paid = cart("buyer-1");
    paid.submitForPayment("Calle 1");
    paid.confirmPayment("PAY-1");
    adapter.save(paid);

    assertThat(adapter.findActiveCartByBuyerId("buyer-1")).isEmpty();
  }

  @Test
  @DisplayName("findAll returns every persisted order")
  void findAllReturnsEveryOrder() {
    adapter.save(cart("buyer-1"));
    adapter.save(cart("buyer-2"));

    assertThat(adapter.findAll()).hasSize(2);
  }

  @Test
  @DisplayName("paid order status and payment reference are persisted")
  void paidOrderStateIsPersisted() {
    Order paid = cart("buyer-1");
    paid.submitForPayment("Calle 1");
    paid.confirmPayment("PAY-1");

    Order saved = adapter.save(paid);
    Order found = adapter.findById(saved.getId()).orElseThrow();

    assertThat(found.getStatus()).isEqualTo(Order.OrderStatus.PAID);
    assertThat(found.getShippingAddress()).isEqualTo("Calle 1");
    assertThat(found.getPaymentReference()).isEqualTo("PAY-1");
  }

  @Test
  @DisplayName("malformed items JSON degrades to an empty item list")
  void malformedItemsJsonDegradesToEmptyList() {
    OrderJpaEntity entity = entity("order-1", "buyer-1");
    entity.setItemsJson("{not-json");
    repository.saveAndFlush(entity);

    assertThat(adapter.findById("order-1").orElseThrow().getItems()).isEmpty();
  }

  @Test
  @DisplayName("the not-null constraint on buyerId is enforced by the database")
  void nullBuyerIdIsRejected() {
    OrderJpaEntity entity = entity("order-1", null);

    assertThatThrownBy(() -> repository.saveAndFlush(entity))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("the not-null constraint on totalAmount is enforced by the database")
  void nullTotalAmountIsRejected() {
    OrderJpaEntity entity = entity("order-1", "buyer-1");
    entity.setTotalAmount(null);

    assertThatThrownBy(() -> repository.saveAndFlush(entity))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("the mapper handles null entities defensively")
  void mapperHandlesNullEntities() {
    assertThat(mapper.toDomain(null)).isNull();
    assertThat(mapper.toEntity(null)).isNull();
  }
}
