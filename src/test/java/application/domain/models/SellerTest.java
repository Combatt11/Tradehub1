package application.domain.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import application.domain.exceptions.InvalidSellerBusinessNameException;
import application.domain.exceptions.SellerAlreadyApprovedException;
import application.domain.exceptions.SellerAlreadySuspendedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Seller")
class SellerTest {

  private static Seller seller() {
    return Seller.create("user-1", "Acme", "TAX-1", "3001234567");
  }

  @Test
  @DisplayName("guards approve and suspend against repeating the current status")
  void guardsStatusTransitions() {
    Seller seller = seller();
    assertThat(seller.getStatus()).isEqualTo(Seller.SellerStatus.PENDING_APPROVAL);

    seller.approve();
    assertThat(seller.getStatus()).isEqualTo(Seller.SellerStatus.ACTIVE);
    assertThatThrownBy(seller::approve)
        .isInstanceOf(SellerAlreadyApprovedException.class)
        .hasMessageContaining("Seller is already approved");

    seller.suspend();
    assertThat(seller.getStatus()).isEqualTo(Seller.SellerStatus.SUSPENDED);
    assertThatThrownBy(seller::suspend)
        .isInstanceOf(SellerAlreadySuspendedException.class)
        .hasMessageContaining("Seller is already suspended");
  }

  @Test
  @DisplayName("rejects a blank business name")
  void rejectsBlankBusinessName() {
    assertThatThrownBy(() -> new Seller(null, "user-1", " ", null, null, null))
        .isInstanceOf(InvalidSellerBusinessNameException.class)
        .hasMessage("Seller business name is required");

    Seller seller = seller();
    assertThatThrownBy(() -> seller.setBusinessName(null))
        .isInstanceOf(InvalidSellerBusinessNameException.class);
  }
}
