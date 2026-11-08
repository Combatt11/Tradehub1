package application.domain.services.warehouse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import application.domain.exceptions.SellerNotApprovedException;
import application.domain.exceptions.SellerNotFoundException;
import application.domain.exceptions.SellerRequiredForWarehouseException;
import application.domain.models.Seller;
import application.domain.models.Warehouse;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.ports.out.WarehouseRepositoryPort;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterSellerWarehouseService")
class RegisterSellerWarehouseServiceTest {

  private static final String SELLER_ID = "seller-1";

  @Mock private WarehouseRepositoryPort warehouseRepository;
  @Mock private SellerRepositoryPort sellerRepository;

  @InjectMocks private RegisterSellerWarehouseService service;

  private static Seller seller() {
    return new Seller(
        SELLER_ID, "user-1", "Acme", "TAX-1", Seller.SellerStatus.PENDING_APPROVAL, "3001234567");
  }

  @Test
  @DisplayName("requires a seller id")
  void requiresSellerId() {
    assertThatThrownBy(() -> service.registerSellerWarehouse("Main", "Calle 1", " "))
        .isInstanceOf(SellerRequiredForWarehouseException.class)
        .hasMessage("A seller is required to create a seller warehouse");

    verifyNoInteractions(warehouseRepository, sellerRepository);
  }

  @Test
  @DisplayName("throws when the seller does not exist")
  void throwsWhenSellerNotFound() {
    when(sellerRepository.findById(SELLER_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.registerSellerWarehouse("Main", "Calle 1", SELLER_ID))
        .isInstanceOf(SellerNotFoundException.class)
        .hasMessage("Seller not found: seller-1");

    verify(warehouseRepository, never()).save(any());
  }

  @Test
  @DisplayName("throws when the seller is not active")
  void throwsWhenSellerIsNotActive() {
    when(sellerRepository.findById(SELLER_ID)).thenReturn(Optional.of(seller()));

    assertThatThrownBy(() -> service.registerSellerWarehouse("Main", "Calle 1", SELLER_ID))
        .isInstanceOf(SellerNotApprovedException.class)
        .hasMessageContaining("is not approved to sell. Status: PENDING_APPROVAL");

    verify(warehouseRepository, never()).save(any());
  }

  @Test
  @DisplayName("registers an active seller warehouse")
  void registersSellerWarehouse() {
    Seller seller = seller();
    seller.approve();
    when(sellerRepository.findById(SELLER_ID)).thenReturn(Optional.of(seller));
    when(warehouseRepository.save(any(Warehouse.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Warehouse warehouse = service.registerSellerWarehouse("Main", "Calle 1", SELLER_ID);

    assertThat(warehouse.getType()).isEqualTo(Warehouse.WarehouseType.SELLER);
    assertThat(warehouse.getSellerId()).isEqualTo(SELLER_ID);
    assertThat(warehouse.getStatus()).isEqualTo(Warehouse.WarehouseStatus.ACTIVE);
    assertThat(warehouse.getName()).isEqualTo("Main");
    verify(warehouseRepository).save(warehouse);
  }
}
