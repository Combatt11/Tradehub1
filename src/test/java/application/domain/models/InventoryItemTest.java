package application.domain.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import application.domain.exceptions.InsufficientStockException;
import application.domain.exceptions.InvalidQuantityException;
import application.domain.exceptions.ProductOutOfStockException;
import application.domain.exceptions.ReservationExceedsReservedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("InventoryItem")
class InventoryItemTest {

  private static final String PRODUCT_ID = "product-1";
  private static final String WAREHOUSE_ID = "warehouse-1";

  private static InventoryItem item(int available) {
    return InventoryItem.create(PRODUCT_ID, WAREHOUSE_ID, available);
  }

  @Nested
  @DisplayName("construction and inbound movements")
  class Construction {

    @Test
    @DisplayName("rejects negative quantities instead of silently clamping them")
    void rejectsNegativeQuantities() {
      assertThatThrownBy(() -> new InventoryItem(null, PRODUCT_ID, WAREHOUSE_ID, -1, 0, 0))
          .isInstanceOf(InvalidQuantityException.class)
          .hasMessage("Inventory quantities cannot be negative");

      assertThatThrownBy(() -> new InventoryItem(null, PRODUCT_ID, WAREHOUSE_ID, 0, -1, 0))
          .isInstanceOf(InvalidQuantityException.class);

      assertThatThrownBy(() -> new InventoryItem(null, PRODUCT_ID, WAREHOUSE_ID, 0, 0, -1))
          .isInstanceOf(InvalidQuantityException.class);
    }

    @Test
    @DisplayName("rejects zero or negative stock additions")
    void rejectsNonPositiveStockAdditions() {
      InventoryItem item = item(1);

      assertThatThrownBy(() -> item.addStock(0))
          .isInstanceOf(InvalidQuantityException.class)
          .hasMessage("Stock amount must be positive");

      assertThatThrownBy(() -> item.addStock(-2)).isInstanceOf(InvalidQuantityException.class);

      item.addStock(4);
      assertThat(item.getQuantityAvailable()).isEqualTo(5);
    }
  }

  @Nested
  @DisplayName("reservations")
  class Reservations {

    @Test
    @DisplayName("distinguishes an out-of-stock product from an insufficient quantity")
    void distinguishesOutOfStockFromInsufficientStock() {
      InventoryItem empty = item(0);

      assertThatThrownBy(() -> empty.reserve(1))
          .isInstanceOf(ProductOutOfStockException.class)
          .hasMessage("Product is out of stock: product-1");

      InventoryItem scarce = item(2);

      assertThatThrownBy(() -> scarce.reserve(3))
          .isInstanceOf(InsufficientStockException.class)
          .hasMessageContaining("Requested: 3, Available: 2");
    }

    @Test
    @DisplayName("rejects non-positive reservation amounts")
    void rejectsNonPositiveReservations() {
      InventoryItem item = item(2);

      assertThatThrownBy(() -> item.reserve(0))
          .isInstanceOf(InvalidQuantityException.class)
          .hasMessage("Reservation amount must be positive");
    }

    @Test
    @DisplayName("releases reserved units back to available stock")
    void releasesReservedUnits() {
      InventoryItem item = item(5);
      item.reserve(3);

      item.releaseReservation(2);

      assertThat(item.getQuantityAvailable()).isEqualTo(4);
      assertThat(item.getQuantityReserved()).isEqualTo(1);
    }

    @Test
    @DisplayName("rejects releasing more units than are reserved")
    void rejectsReleasingMoreThanReserved() {
      InventoryItem item = item(5);
      item.reserve(3);

      assertThatThrownBy(() -> item.releaseReservation(4))
          .isInstanceOf(ReservationExceedsReservedException.class)
          .hasMessage("Cannot move 4 reserved units for product product-1. Reserved: 3");

      assertThatThrownBy(() -> item.releaseReservation(0))
          .isInstanceOf(InvalidQuantityException.class)
          .hasMessage("Release amount must be positive");
    }

    @Test
    @DisplayName("confirms a sale against reserved units")
    void confirmsSale() {
      InventoryItem item = item(5);
      item.reserve(3);

      item.confirmSale(2);

      assertThat(item.getQuantityReserved()).isEqualTo(1);
    }

    @Test
    @DisplayName("rejects confirming more units than are reserved")
    void rejectsConfirmingMoreThanReserved() {
      InventoryItem item = item(5);
      item.reserve(3);

      assertThatThrownBy(() -> item.confirmSale(4))
          .isInstanceOf(ReservationExceedsReservedException.class)
          .hasMessageContaining("Cannot move 4 reserved units");

      assertThatThrownBy(() -> item.confirmSale(0))
          .isInstanceOf(InvalidQuantityException.class)
          .hasMessage("Sale amount must be positive");
    }
  }

  @Nested
  @DisplayName("damaged stock")
  class DamagedStock {

    @Test
    @DisplayName("moves available units to damaged stock")
    void marksUnitsAsDamaged() {
      InventoryItem item = item(5);

      item.markDamaged(2);

      assertThat(item.getQuantityAvailable()).isEqualTo(3);
      assertThat(item.getQuantityDamaged()).isEqualTo(2);
    }

    @Test
    @DisplayName("rejects non-positive or excessive damaged amounts")
    void rejectsInvalidDamagedAmounts() {
      InventoryItem item = item(1);

      assertThatThrownBy(() -> item.markDamaged(0))
          .isInstanceOf(InvalidQuantityException.class)
          .hasMessage("Damaged amount must be positive");

      assertThatThrownBy(() -> item.markDamaged(2))
          .isInstanceOf(InsufficientStockException.class)
          .hasMessageContaining("Requested: 2, Available: 1");
    }
  }
}
