package application.adapters.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import application.adapters.persistence.jpa.entities.InventoryItemJpaEntity;
import application.adapters.persistence.jpa.mappers.InventoryJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaInventoryRepository;
import application.domain.models.InventoryItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@Import({InventoryJpaMapper.class, InventoryJpaAdapter.class})
@DisplayName("InventoryJpaAdapter persistence")
class InventoryRepositoryJpaTest {

  @Autowired private SpringDataJpaInventoryRepository repository;
  @Autowired private InventoryJpaAdapter adapter;
  @Autowired private InventoryJpaMapper mapper;

  private static InventoryItemJpaEntity entity(
      String id, String productId, String warehouseId, int available) {
    InventoryItemJpaEntity entity = new InventoryItemJpaEntity();
    entity.setId(id);
    entity.setProductId(productId);
    entity.setWarehouseId(warehouseId);
    entity.setQuantityAvailable(available);
    entity.setQuantityReserved(0);
    entity.setQuantityDamaged(0);
    return entity;
  }

  @Test
  @DisplayName("save and findById round-trip every mapped quantity")
  void saveAndFindByIdRoundTripsAllFields() {
    InventoryItem saved =
        adapter.save(new InventoryItem(null, "product-1", "warehouse-1", 10, 2, 1));

    InventoryItem found = adapter.findById(saved.getId()).orElseThrow();
    assertThat(found.getProductId()).isEqualTo("product-1");
    assertThat(found.getWarehouseId()).isEqualTo("warehouse-1");
    assertThat(found.getQuantityAvailable()).isEqualTo(10);
    assertThat(found.getQuantityReserved()).isEqualTo(2);
    assertThat(found.getQuantityDamaged()).isEqualTo(1);
  }

  @Test
  @DisplayName("findByProductAndWarehouse returns the matching row")
  void findByProductAndWarehouseReturnsMatch() {
    adapter.save(InventoryItem.create("product-1", "warehouse-1", 10));

    assertThat(adapter.findByProductAndWarehouse("product-1", "warehouse-1"))
        .isPresent()
        .get()
        .extracting(InventoryItem::getQuantityAvailable)
        .isEqualTo(10);
  }

  @Test
  @DisplayName("findByProductAndWarehouse returns empty for an unknown pair")
  void findByProductAndWarehouseReturnsEmptyWhenMissing() {
    assertThat(adapter.findByProductAndWarehouse("product-1", "warehouse-1")).isEmpty();
  }

  @Test
  @DisplayName("findByProductId returns every warehouse holding the product")
  void findByProductIdReturnsEveryWarehouse() {
    adapter.save(InventoryItem.create("product-1", "warehouse-1", 10));
    adapter.save(InventoryItem.create("product-1", "warehouse-2", 20));
    adapter.save(InventoryItem.create("product-2", "warehouse-1", 30));

    assertThat(adapter.findByProductId("product-1")).hasSize(2);
  }

  @Test
  @DisplayName("findByWarehouseId returns every product stored there")
  void findByWarehouseIdReturnsEveryProduct() {
    adapter.save(InventoryItem.create("product-1", "warehouse-1", 10));
    adapter.save(InventoryItem.create("product-2", "warehouse-1", 20));
    adapter.save(InventoryItem.create("product-1", "warehouse-2", 30));

    assertThat(adapter.findByWarehouseId("warehouse-1")).hasSize(2);
  }

  @Test
  @DisplayName("stock mutations are persisted")
  void stockMutationsArePersisted() {
    InventoryItem item = adapter.save(InventoryItem.create("product-1", "warehouse-1", 10));
    item.reserve(3);

    adapter.save(item);

    InventoryItem found = adapter.findById(item.getId()).orElseThrow();
    assertThat(found.getQuantityAvailable()).isEqualTo(7);
    assertThat(found.getQuantityReserved()).isEqualTo(3);
  }

  @Test
  @DisplayName("the product/warehouse unique constraint is enforced by the database")
  void duplicateProductWarehousePairIsRejected() {
    repository.saveAndFlush(entity("inventory-1", "product-1", "warehouse-1", 10));

    assertThatThrownBy(
            () -> repository.saveAndFlush(entity("inventory-2", "product-1", "warehouse-1", 5)))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("the not-null constraint on productId is enforced by the database")
  void nullProductIdIsRejected() {
    InventoryItemJpaEntity entity = entity("inventory-1", null, "warehouse-1", 10);

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
