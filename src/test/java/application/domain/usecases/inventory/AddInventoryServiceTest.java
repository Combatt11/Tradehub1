package application.domain.usecases.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import application.domain.entities.InventoryItem;
import application.domain.entities.Product;
import application.domain.entities.Warehouse;
import application.domain.exceptions.ProductNotFoundException;
import application.domain.exceptions.ProductNotPhysicalException;
import application.domain.exceptions.ProductOwnershipException;
import application.domain.exceptions.WarehouseNotActiveException;
import application.domain.exceptions.WarehouseNotFoundException;
import application.domain.ports.out.InventoryRepositoryPort;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import application.domain.usecases.AddInventoryService;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddInventoryService")
class AddInventoryServiceTest {

  private static final String SELLER_ID = "seller-1";
  private static final String PRODUCT_ID = "product-1";
  private static final String WAREHOUSE_ID = "warehouse-1";

  @Mock private InventoryRepositoryPort inventoryRepository;
  @Mock private ProductRepositoryPort productRepository;
  @Mock private WarehouseRepositoryPort warehouseRepository;

  @InjectMocks private AddInventoryService service;

  private static Product physicalProduct(String sellerId) {
    return new Product(
        PRODUCT_ID,
        sellerId,
        "Laptop",
        "A laptop",
        Product.ProductType.PHYSICAL,
        new BigDecimal("1200.00"),
        Product.ProductStatus.PUBLISHED,
        null,
        "SKU-1");
  }

  private static Product digitalProduct(String sellerId) {
    return new Product(
        PRODUCT_ID,
        sellerId,
        "Ebook",
        "An ebook",
        Product.ProductType.DIGITAL,
        new BigDecimal("9.99"),
        Product.ProductStatus.PUBLISHED,
        null,
        "SKU-2");
  }

  private static Warehouse activeWarehouse() {
    return Warehouse.createMarketplace("Main", "Calle 1");
  }

  @Nested
  @DisplayName("happy paths")
  class HappyPaths {

    @Test
    @DisplayName("creates a new inventory item when none exists for product+warehouse")
    void createsNewInventoryItem() {
      when(productRepository.findById(PRODUCT_ID))
          .thenReturn(Optional.of(physicalProduct(SELLER_ID)));
      when(warehouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.of(activeWarehouse()));
      when(inventoryRepository.findByProductAndWarehouse(PRODUCT_ID, WAREHOUSE_ID))
          .thenReturn(Optional.empty());
      when(inventoryRepository.save(any(InventoryItem.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      InventoryItem result = service.execute(PRODUCT_ID, WAREHOUSE_ID, 10, SELLER_ID);

      assertThat(result.getProductId()).isEqualTo(PRODUCT_ID);
      assertThat(result.getWarehouseId()).isEqualTo(WAREHOUSE_ID);
      assertThat(result.getQuantityAvailable()).isEqualTo(10);
      assertThat(result.getQuantityReserved()).isZero();
      assertThat(result.getId()).isNotBlank();
    }

    @Test
    @DisplayName("increments stock on an existing inventory item")
    void incrementsExistingStock() {
      InventoryItem existing = InventoryItem.create(PRODUCT_ID, WAREHOUSE_ID, 5);
      when(productRepository.findById(PRODUCT_ID))
          .thenReturn(Optional.of(physicalProduct(SELLER_ID)));
      when(warehouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.of(activeWarehouse()));
      when(inventoryRepository.findByProductAndWarehouse(PRODUCT_ID, WAREHOUSE_ID))
          .thenReturn(Optional.of(existing));
      when(inventoryRepository.save(any(InventoryItem.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      InventoryItem result = service.execute(PRODUCT_ID, WAREHOUSE_ID, 7, SELLER_ID);

      assertThat(result).isSameAs(existing);
      assertThat(result.getQuantityAvailable()).isEqualTo(12);

      ArgumentCaptor<InventoryItem> captor = ArgumentCaptor.forClass(InventoryItem.class);
      verify(inventoryRepository).save(captor.capture());
      assertThat(captor.getValue().getQuantityAvailable()).isEqualTo(12);
    }
  }

  @Nested
  @DisplayName("error paths")
  class ErrorPaths {

    @Test
    @DisplayName("throws when the product does not exist")
    void throwsWhenProductNotFound() {
      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.execute(PRODUCT_ID, WAREHOUSE_ID, 1, SELLER_ID))
          .isInstanceOf(ProductNotFoundException.class)
          .hasMessage("Product not found: product-1");

      verifyNoInteractions(inventoryRepository, warehouseRepository);
    }

    @Test
    @DisplayName("throws when the product belongs to another seller")
    void throwsWhenProductBelongsToAnotherSeller() {
      when(productRepository.findById(PRODUCT_ID))
          .thenReturn(Optional.of(physicalProduct("other-seller")));

      assertThatThrownBy(() -> service.execute(PRODUCT_ID, WAREHOUSE_ID, 1, SELLER_ID))
          .isInstanceOf(ProductOwnershipException.class)
          .hasMessage("Product product-1 does not belong to seller: seller-1");

      verifyNoInteractions(inventoryRepository, warehouseRepository);
    }

    @Test
    @DisplayName("throws when the product is digital")
    void throwsWhenProductIsDigital() {
      when(productRepository.findById(PRODUCT_ID))
          .thenReturn(Optional.of(digitalProduct(SELLER_ID)));

      assertThatThrownBy(() -> service.execute(PRODUCT_ID, WAREHOUSE_ID, 1, SELLER_ID))
          .isInstanceOf(ProductNotPhysicalException.class)
          .hasMessage("Digital products do not require inventory: product-1");

      verifyNoInteractions(inventoryRepository, warehouseRepository);
    }

    @Test
    @DisplayName("throws when the warehouse does not exist")
    void throwsWhenWarehouseNotFound() {
      when(productRepository.findById(PRODUCT_ID))
          .thenReturn(Optional.of(physicalProduct(SELLER_ID)));
      when(warehouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.execute(PRODUCT_ID, WAREHOUSE_ID, 1, SELLER_ID))
          .isInstanceOf(WarehouseNotFoundException.class)
          .hasMessage("Warehouse not found: warehouse-1");

      verifyNoInteractions(inventoryRepository);
    }

    @Test
    @DisplayName("throws when the warehouse is not active")
    void throwsWhenWarehouseIsInactive() {
      Warehouse inactive = Warehouse.createMarketplace("Main", "Calle 1");
      inactive.deactivate();
      when(productRepository.findById(PRODUCT_ID))
          .thenReturn(Optional.of(physicalProduct(SELLER_ID)));
      when(warehouseRepository.findById(WAREHOUSE_ID)).thenReturn(Optional.of(inactive));

      assertThatThrownBy(() -> service.execute(PRODUCT_ID, WAREHOUSE_ID, 1, SELLER_ID))
          .isInstanceOf(WarehouseNotActiveException.class)
          .hasMessageContaining("is not active. Status: INACTIVE");

      verify(inventoryRepository, never()).save(any());
    }
  }
}
