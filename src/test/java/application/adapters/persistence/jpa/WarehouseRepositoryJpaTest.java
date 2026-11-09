package application.adapters.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import application.adapters.persistence.jpa.entities.WarehouseJpaEntity;
import application.adapters.persistence.jpa.mappers.WarehouseJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaWarehouseRepository;
import application.domain.models.Warehouse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@Import({WarehouseJpaMapper.class, WarehouseJpaAdapter.class})
@DisplayName("WarehouseJpaAdapter persistence")
class WarehouseRepositoryJpaTest {

  @Autowired private SpringDataJpaWarehouseRepository repository;
  @Autowired private WarehouseJpaAdapter adapter;
  @Autowired private WarehouseJpaMapper mapper;

  private static WarehouseJpaEntity entity(String id, String name, String sellerId) {
    WarehouseJpaEntity entity = new WarehouseJpaEntity();
    entity.setId(id);
    entity.setName(name);
    entity.setAddress("Address");
    entity.setType(sellerId == null ? "MARKETPLACE" : "SELLER");
    entity.setStatus("ACTIVE");
    entity.setSellerId(sellerId);
    entity.setOperatorUserId("operator-1");
    return entity;
  }

  @Test
  @DisplayName("save and findById round-trip every mapped field")
  void saveAndFindByIdRoundTripsAllFields() {
    Warehouse warehouse = Warehouse.createForSeller("Seller Warehouse", "Calle 1", "seller-1");

    Warehouse saved = adapter.save(warehouse);
    Warehouse found = adapter.findById(saved.getId()).orElseThrow();

    assertThat(found.getName()).isEqualTo("Seller Warehouse");
    assertThat(found.getAddress()).isEqualTo("Calle 1");
    assertThat(found.getType()).isEqualTo(Warehouse.WarehouseType.SELLER);
    assertThat(found.getStatus()).isEqualTo(Warehouse.WarehouseStatus.ACTIVE);
    assertThat(found.getSellerId()).isEqualTo("seller-1");
    assertThat(found.getOperatorUserId()).isNull();
  }

  @Test
  @DisplayName("a marketplace warehouse is persisted with a null seller")
  void marketplaceWarehouseHasNoSeller() {
    Warehouse saved = adapter.save(Warehouse.createMarketplace("Central", "Main Ave"));

    Warehouse found = adapter.findById(saved.getId()).orElseThrow();
    assertThat(found.getType()).isEqualTo(Warehouse.WarehouseType.MARKETPLACE);
    assertThat(found.getSellerId()).isNull();
  }

  @Test
  @DisplayName("findBySellerId returns only that seller's warehouses")
  void findBySellerIdReturnsOwnedWarehouses() {
    adapter.save(Warehouse.createForSeller("Seller A", "Calle 1", "seller-1"));
    adapter.save(Warehouse.createForSeller("Seller B", "Calle 2", "seller-2"));
    adapter.save(Warehouse.createMarketplace("Central", "Main Ave"));

    assertThat(adapter.findBySellerId("seller-1"))
        .extracting(Warehouse::getName)
        .containsExactly("Seller A");
  }

  @Test
  @DisplayName("findBySellerId returns empty for an unknown seller")
  void findBySellerIdReturnsEmptyWhenMissing() {
    assertThat(adapter.findBySellerId("nobody")).isEmpty();
  }

  @Test
  @DisplayName("findAll returns every persisted warehouse")
  void findAllReturnsEveryWarehouse() {
    adapter.save(Warehouse.createForSeller("Seller A", "Calle 1", "seller-1"));
    adapter.save(Warehouse.createMarketplace("Central", "Main Ave"));

    assertThat(adapter.findAll()).hasSize(2);
  }

  @Test
  @DisplayName("an assigned operator is persisted")
  void assignedOperatorIsPersisted() {
    Warehouse warehouse =
        adapter.save(Warehouse.createForSeller("Seller A", "Calle 1", "seller-1"));
    warehouse.assignOperator("operator-1");

    adapter.save(warehouse);

    assertThat(adapter.findById(warehouse.getId()).orElseThrow().getOperatorUserId())
        .isEqualTo("operator-1");
  }

  @Test
  @DisplayName("the not-null constraint on name is enforced by the database")
  void nullNameIsRejected() {
    WarehouseJpaEntity entity = entity("warehouse-1", null, "seller-1");

    assertThatThrownBy(() -> repository.saveAndFlush(entity))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("the not-null constraint on type is enforced by the database")
  void nullTypeIsRejected() {
    WarehouseJpaEntity entity = entity("warehouse-1", "Warehouse", "seller-1");
    entity.setType(null);

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
