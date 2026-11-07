package application.domain.services.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import application.domain.exceptions.InsufficientStockException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Buyer;
import application.domain.models.InventoryItem;
import application.domain.models.Order;
import application.domain.models.Product;
import application.domain.ports.out.BuyerRepositoryPort;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.OrderRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService")
class CartServiceTest {

    private static final String BUYER_ID = "buyer-1";
    private static final String PRODUCT_ID = "product-1";
    private static final String WAREHOUSE_ID = "warehouse-1";

    @Mock
    private OrderRepositoryPort orderRepository;
    @Mock
    private ProductRepositoryPort productRepository;
    @Mock
    private InventoryRepositoryPort inventoryRepository;
    @Mock
    private BuyerRepositoryPort buyerRepository;

    @InjectMocks
    private CartService service;

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
    @DisplayName("getOrCreateCart")
    class GetOrCreateCart {

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

    @Nested
    @DisplayName("addToCart")
    class AddToCart {

        @Test
        @DisplayName("reserves stock and adds a physical product to the cart")
        void addsPhysicalProductAndReservesStock() {
            InventoryItem inventory = InventoryItem.create(PRODUCT_ID, WAREHOUSE_ID, 10);
            when(buyerRepository.findById(BUYER_ID)).thenReturn(Optional.of(activeBuyer()));
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(publishedPhysicalProduct()));
            when(inventoryRepository.findByProductAndWarehouse(PRODUCT_ID, WAREHOUSE_ID))
                    .thenReturn(Optional.of(inventory));
            when(orderRepository.findActiveCartByBuyerId(BUYER_ID)).thenReturn(Optional.empty());
            when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

            Order result = service.addToCart(BUYER_ID, PRODUCT_ID, 3, WAREHOUSE_ID);

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
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(publishedDigitalProduct()));
            when(orderRepository.findActiveCartByBuyerId(BUYER_ID)).thenReturn(Optional.empty());
            when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

            Order result = service.addToCart(BUYER_ID, PRODUCT_ID, 2, null);

            assertThat(result.getItems()).hasSize(1);
            assertThat(result.getTotalAmount()).isEqualByComparingTo("20.00");
            verifyNoInteractions(inventoryRepository);
        }

        @Test
        @DisplayName("throws when the buyer does not exist")
        void throwsWhenBuyerNotFound() {
            when(buyerRepository.findById(BUYER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.addToCart(BUYER_ID, PRODUCT_ID, 1, WAREHOUSE_ID))
                    .isInstanceOf(UnauthorizedOperationException.class)
                    .hasMessage("Buyer not found");

            verifyNoInteractions(productRepository, inventoryRepository, orderRepository);
        }

        @Test
        @DisplayName("throws when the buyer is suspended")
        void throwsWhenBuyerCannotPurchase() {
            Buyer suspended = Buyer.create("user-1", "Calle 1");
            suspended.suspend();
            when(buyerRepository.findById(BUYER_ID)).thenReturn(Optional.of(suspended));

            assertThatThrownBy(() -> service.addToCart(BUYER_ID, PRODUCT_ID, 1, WAREHOUSE_ID))
                    .isInstanceOf(UnauthorizedOperationException.class)
                    .hasMessage("Buyer cannot purchase");

            verifyNoInteractions(productRepository, inventoryRepository, orderRepository);
        }

        @Test
        @DisplayName("throws when the product does not exist")
        void throwsWhenProductNotFound() {
            when(buyerRepository.findById(BUYER_ID)).thenReturn(Optional.of(activeBuyer()));
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.addToCart(BUYER_ID, PRODUCT_ID, 1, WAREHOUSE_ID))
                    .isInstanceOf(UnauthorizedOperationException.class)
                    .hasMessage("Product not found");

            verifyNoInteractions(inventoryRepository, orderRepository);
        }

        @Test
        @DisplayName("throws when the product is not published")
        void throwsWhenProductNotAvailableForSale() {
            Product draft
                    = new Product(
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

            assertThatThrownBy(() -> service.addToCart(BUYER_ID, PRODUCT_ID, 1, WAREHOUSE_ID))
                    .isInstanceOf(UnauthorizedOperationException.class)
                    .hasMessage("Product is not available for sale");

            verifyNoInteractions(inventoryRepository, orderRepository);
        }

        @Test
        @DisplayName("throws InsufficientStockException when there is no inventory record")
        void throwsWhenInventoryRecordMissing() {
            when(buyerRepository.findById(BUYER_ID)).thenReturn(Optional.of(activeBuyer()));
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(publishedPhysicalProduct()));
            when(inventoryRepository.findByProductAndWarehouse(PRODUCT_ID, WAREHOUSE_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.addToCart(BUYER_ID, PRODUCT_ID, 2, WAREHOUSE_ID))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("Insufficient stock for product product-1")
                    .hasMessageContaining("Requested: 2, Available: 0");

            verify(inventoryRepository, never()).save(any());
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws InsufficientStockException when requested quantity exceeds available")
        void throwsWhenNotEnoughStock() {
            InventoryItem inventory = InventoryItem.create(PRODUCT_ID, WAREHOUSE_ID, 1);
            when(buyerRepository.findById(BUYER_ID)).thenReturn(Optional.of(activeBuyer()));
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(publishedPhysicalProduct()));
            when(inventoryRepository.findByProductAndWarehouse(PRODUCT_ID, WAREHOUSE_ID))
                    .thenReturn(Optional.of(inventory));

            assertThatThrownBy(() -> service.addToCart(BUYER_ID, PRODUCT_ID, 5, WAREHOUSE_ID))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("Requested: 5, Available: 1");

            assertThat(inventory.getQuantityAvailable()).isEqualTo(1);
            assertThat(inventory.getQuantityReserved()).isZero();
            verify(inventoryRepository, never()).save(any());
            verify(orderRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("submitOrder")
    class SubmitOrder {

        @Test
        @DisplayName("submits a non-empty cart for payment")
        void submitsCart() {
            Order cart = Order.createCart(BUYER_ID);
            cart.addItem(
                    new application.domain.models.OrderItem(
                            PRODUCT_ID, "Laptop", 1, new BigDecimal("100.00"), WAREHOUSE_ID));
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
}
