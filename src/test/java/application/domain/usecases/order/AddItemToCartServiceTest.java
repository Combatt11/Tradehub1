package application.domain.usecases.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import application.domain.entities.Buyer;
import application.domain.entities.InventoryItem;
import application.domain.entities.Order;
import application.domain.entities.Product;
import application.domain.exceptions.BuyerCannotPurchaseException;
import application.domain.exceptions.BuyerNotFoundException;
import application.domain.exceptions.InsufficientStockException;
import application.domain.exceptions.ProductNotAvailableException;
import application.domain.exceptions.ProductNotFoundException;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.usecases.AddItemToCartService;
import application.domain.usecases.ports.GetCartUseCase;
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
@DisplayName("AddItemToCartService")
class AddItemToCartServiceTest {

  private static final String BUYER_ID = "buyer-1";
  private static final String PRODUCT_ID = "product-1";
  private static final String WAREHOUSE_ID = "warehouse-1";

  @Mock private OrderRepositoryPort orderRepository;
  @Mock private ProductRepositoryPort productRepository;
  @Mock private InventoryRepositoryPort inventoryRepository;
  @Mock private BuyerRepositoryPort buyerRepository;
  @Mock private GetCartUseCase getCartUseCase;

  @InjectMocks private AddItemToCartService service;

  private Order cart;

  @BeforeEach
  void setUp() {
    cart = Order.createCart(BUYER_ID);
  }

  private static Buyer activeBuyer() {
    return Buyer.create("user-1", "Calle 1");
  }

  private static Product publishedPhysicalProduct() {
    return new Product(
        PRODUCT_ID,
        "seller-1",
        "Laptop",
        "A laptop",
        Product.ProductType.PHYSICAL,
        new BigDecimal("100.00"),
        Product.ProductStatus.PUBLISHED,
        null,
        "SKU-1");
  }

  private static Product publishedDigitalProduct() {
    return new Product(
        PRODUCT_ID,
        "seller-1",
        "Ebook",
        "An ebook",
        Product.ProductType.DIGITAL,
        new BigDecimal("10.00"),
        Product.ProductStatus.PUBLISHED,
        null,
        "SKU-2");
  }

  @Nested
  @DisplayName("happy paths")
  class HappyPaths {

    @Test
    @DisplayName("reserves stock and adds a physical product to the cart")
    void addsPhysicalProductAndReservesStock() {
      InventoryItem inventory = InventoryItem.create(PRODUCT_ID, WAREHOUSE_ID, 10);
      when(buyerRepository.findById(BUYER_ID)).thenReturn(Optional.of(activeBuyer()));
      when(productRepository.findById(PRODUCT_ID))
          .thenReturn(Optional.of(publishedPhysicalProduct()));
      when(inventoryRepository.findByProductAndWarehouse(PRODUCT_ID, WAREHOUSE_ID))
          .thenReturn(Optional.of(inventory));
      when(getCartUseCase.getOrCreateCart(BUYER_ID)).thenReturn(cart);
      when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

      Order result = service.addItem(BUYER_ID, PRODUCT_ID, 3, WAREHOUSE_ID);

      assertThat(result.getItems()).hasSize(1);
      assertThat(result.getItems().get(0).getQuantity()).isEqualTo(3);
      assertThat(result.getTotalAmount()).isEqualByComparingTo("300.00");
      assertThat(inventory.getQuantityAvailable()).isEqualTo(7);
      assertThat(inventory.getQuantityReserved()).isEqualTo(3);
      verify(inventoryRepository).save(inventory);
    }

    @Test
    @DisplayName("adds a digital product without touching inventory")
    void addsDigitalProductWithoutInventory() {
      when(buyerRepository.findById(BUYER_ID)).thenReturn(Optional.of(activeBuyer()));
      when(productRepository.findById(PRODUCT_ID))
          .thenReturn(Optional.of(publishedDigitalProduct()));
      when(getCartUseCase.getOrCreateCart(BUYER_ID)).thenReturn(cart);
      when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

      Order result = service.addItem(BUYER_ID, PRODUCT_ID, 2, null);

      assertThat(result.getItems()).hasSize(1);
      assertThat(result.getTotalAmount()).isEqualByComparingTo("20.00");
      verifyNoInteractions(inventoryRepository);
    }
  }

  @Nested
  @DisplayName("buyer validation")
  class BuyerValidation {

    @Test
    @DisplayName("throws when the buyer does not exist")
    void throwsWhenBuyerNotFound() {
      when(buyerRepository.findById(BUYER_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.addItem(BUYER_ID, PRODUCT_ID, 1, WAREHOUSE_ID))
          .isInstanceOf(BuyerNotFoundException.class)
          .hasMessage("Buyer not found: buyer-1");

      verifyNoInteractions(productRepository, inventoryRepository, orderRepository, getCartUseCase);
    }

    @Test
    @DisplayName("throws when the buyer is suspended")
    void throwsWhenBuyerCannotPurchase() {
      Buyer suspended = Buyer.create("user-1", "Calle 1");
      suspended.suspend();
      when(buyerRepository.findById(BUYER_ID)).thenReturn(Optional.of(suspended));

      assertThatThrownBy(() -> service.addItem(BUYER_ID, PRODUCT_ID, 1, WAREHOUSE_ID))
          .isInstanceOf(BuyerCannotPurchaseException.class)
          .hasMessageContaining("cannot purchase in status: SUSPENDED");

      verifyNoInteractions(productRepository, inventoryRepository, orderRepository, getCartUseCase);
    }
  }

  @Nested
  @DisplayName("product validation")
  class ProductValidation {

    @Test
    @DisplayName("throws when the product does not exist")
    void throwsWhenProductNotFound() {
      when(buyerRepository.findById(BUYER_ID)).thenReturn(Optional.of(activeBuyer()));
      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.addItem(BUYER_ID, PRODUCT_ID, 1, WAREHOUSE_ID))
          .isInstanceOf(ProductNotFoundException.class)
          .hasMessage("Product not found: product-1");

      verifyNoInteractions(inventoryRepository, orderRepository, getCartUseCase);
    }

    @Test
    @DisplayName("throws when the product is not published")
    void throwsWhenProductNotAvailableForSale() {
      Product draft =
          new Product(
              PRODUCT_ID,
              "seller-1",
              "Laptop",
              "A laptop",
              Product.ProductType.PHYSICAL,
              new BigDecimal("100.00"),
              Product.ProductStatus.DRAFT,
              null,
              "SKU-1");
      when(buyerRepository.findById(BUYER_ID)).thenReturn(Optional.of(activeBuyer()));
      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(draft));

      assertThatThrownBy(() -> service.addItem(BUYER_ID, PRODUCT_ID, 1, WAREHOUSE_ID))
          .isInstanceOf(ProductNotAvailableException.class)
          .hasMessageContaining("is not available for sale. Status: DRAFT");

      verifyNoInteractions(inventoryRepository, orderRepository, getCartUseCase);
    }
  }

  @Nested
  @DisplayName("stock validation")
  class StockValidation {

    @Test
    @DisplayName("throws InsufficientStockException when there is no inventory record")
    void throwsWhenInventoryRecordMissing() {
      when(buyerRepository.findById(BUYER_ID)).thenReturn(Optional.of(activeBuyer()));
      when(productRepository.findById(PRODUCT_ID))
          .thenReturn(Optional.of(publishedPhysicalProduct()));
      when(inventoryRepository.findByProductAndWarehouse(PRODUCT_ID, WAREHOUSE_ID))
          .thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.addItem(BUYER_ID, PRODUCT_ID, 2, WAREHOUSE_ID))
          .isInstanceOf(InsufficientStockException.class)
          .hasMessageContaining("Insufficient stock for product product-1")
          .hasMessageContaining("Requested: 2, Available: 0");

      verify(inventoryRepository, never()).save(any());
      verify(orderRepository, never()).save(any());
      verifyNoInteractions(getCartUseCase);
    }

    @Test
    @DisplayName("throws InsufficientStockException when requested quantity exceeds available")
    void throwsWhenNotEnoughStock() {
      InventoryItem inventory = InventoryItem.create(PRODUCT_ID, WAREHOUSE_ID, 1);
      when(buyerRepository.findById(BUYER_ID)).thenReturn(Optional.of(activeBuyer()));
      when(productRepository.findById(PRODUCT_ID))
          .thenReturn(Optional.of(publishedPhysicalProduct()));
      when(inventoryRepository.findByProductAndWarehouse(PRODUCT_ID, WAREHOUSE_ID))
          .thenReturn(Optional.of(inventory));

      assertThatThrownBy(() -> service.addItem(BUYER_ID, PRODUCT_ID, 5, WAREHOUSE_ID))
          .isInstanceOf(InsufficientStockException.class)
          .hasMessageContaining("Requested: 5, Available: 1");

      assertThat(inventory.getQuantityAvailable()).isEqualTo(1);
      assertThat(inventory.getQuantityReserved()).isZero();
      verify(inventoryRepository, never()).save(any());
      verify(orderRepository, never()).save(any());
      verifyNoInteractions(getCartUseCase);
    }
  }
}
