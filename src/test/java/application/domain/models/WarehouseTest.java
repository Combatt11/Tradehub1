package application.domain.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import application.domain.exceptions.InvalidWarehouseAddressException;
import application.domain.exceptions.InvalidWarehouseNameException;
import application.domain.exceptions.SellerRequiredForWarehouseException;
import application.domain.exceptions.WarehouseAlreadyActiveException;
import application.domain.exceptions.WarehouseAlreadyInactiveException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Warehouse")
class WarehouseTest {

  @Test
  @DisplayName("guards activate and deactivate against repeating the current status")
  void guardsStatusTransitions() {
    Warehouse warehouse = Warehouse.createMarketplace("Main", "Calle 1");
    assertThat(warehouse.getStatus()).isEqualTo(Warehouse.WarehouseStatus.ACTIVE);

    assertThatThrownBy(warehouse::activate)
        .isInstanceOf(WarehouseAlreadyActiveException.class)
        .hasMessageContaining("Warehouse is already active");

    warehouse.deactivate();
    assertThat(warehouse.getStatus()).isEqualTo(Warehouse.WarehouseStatus.INACTIVE);
    assertThatThrownBy(warehouse::deactivate)
        .isInstanceOf(WarehouseAlreadyInactiveException.class)
        .hasMessageContaining("Warehouse is already inactive");

    warehouse.activate();
    assertThat(warehouse.getStatus()).isEqualTo(Warehouse.WarehouseStatus.ACTIVE);
  }

  @Test
  @DisplayName("requires a name and an address")
  void requiresNameAndAddress() {
    assertThatThrownBy(() -> Warehouse.createMarketplace(" ", "Calle 1"))
        .isInstanceOf(InvalidWarehouseNameException.class)
        .hasMessage("Warehouse name is required");

    assertThatThrownBy(() -> Warehouse.createMarketplace("Main", null))
        .isInstanceOf(InvalidWarehouseAddressException.class)
        .hasMessage("Warehouse address is required");

    Warehouse warehouse = Warehouse.createMarketplace("Main", "Calle 1");
    assertThatThrownBy(() -> warehouse.setName(" "))
        .isInstanceOf(InvalidWarehouseNameException.class);
    assertThatThrownBy(() -> warehouse.setAddress(null))
        .isInstanceOf(InvalidWarehouseAddressException.class);
  }

  @Test
  @DisplayName("requires a seller for seller warehouses")
  void requiresSellerForSellerWarehouse() {
    assertThatThrownBy(() -> Warehouse.createForSeller("Main", "Calle 1", null))
        .isInstanceOf(SellerRequiredForWarehouseException.class)
        .hasMessage("A seller is required to create a seller warehouse");

    assertThatThrownBy(() -> Warehouse.createForSeller("Main", "Calle 1", " "))
        .isInstanceOf(SellerRequiredForWarehouseException.class);
  }
}
