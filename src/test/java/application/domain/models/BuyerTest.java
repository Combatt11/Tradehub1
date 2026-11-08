package application.domain.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import application.domain.exceptions.BuyerAddressRequiredException;
import application.domain.exceptions.BuyerAlreadyActiveException;
import application.domain.exceptions.BuyerAlreadySuspendedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Buyer")
class BuyerTest {

  @Test
  @DisplayName("guards activate and suspend against repeating the current status")
  void guardsStatusTransitions() {
    Buyer buyer = Buyer.create("user-1", "Calle 1");
    assertThat(buyer.getCommercialStatus()).isEqualTo(Buyer.CommercialStatus.ACTIVE);

    assertThatThrownBy(buyer::activate)
        .isInstanceOf(BuyerAlreadyActiveException.class)
        .hasMessageContaining("Buyer is already active");

    buyer.suspend();
    assertThat(buyer.getCommercialStatus()).isEqualTo(Buyer.CommercialStatus.SUSPENDED);
    assertThatThrownBy(buyer::suspend)
        .isInstanceOf(BuyerAlreadySuspendedException.class)
        .hasMessageContaining("Buyer is already suspended");

    buyer.activate();
    assertThat(buyer.getCommercialStatus()).isEqualTo(Buyer.CommercialStatus.ACTIVE);
  }

  @Test
  @DisplayName("requires a non-blank primary and additional address")
  void requiresAddresses() {
    assertThatThrownBy(() -> Buyer.create("user-1", " "))
        .isInstanceOf(BuyerAddressRequiredException.class)
        .hasMessage("Buyer address is required");

    Buyer buyer = Buyer.create("user-1", "Calle 1");
    assertThatThrownBy(() -> buyer.addAddress(null))
        .isInstanceOf(BuyerAddressRequiredException.class);
    assertThatThrownBy(() -> buyer.setPrimaryAddress(""))
        .isInstanceOf(BuyerAddressRequiredException.class);

    buyer.addAddress("Carrera 2");
    assertThat(buyer.getAdditionalAddresses()).containsExactly("Carrera 2");
  }
}
