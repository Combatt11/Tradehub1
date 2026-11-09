package application.adapters.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import application.adapters.persistence.jpa.entities.BuyerJpaEntity;
import application.adapters.persistence.jpa.mappers.BuyerJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaBuyerRepository;
import application.domain.models.Buyer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@Import({BuyerJpaMapper.class, BuyerJpaAdapter.class})
@DisplayName("BuyerJpaAdapter persistence")
class BuyerRepositoryJpaTest {

  @Autowired private SpringDataJpaBuyerRepository repository;
  @Autowired private BuyerJpaAdapter adapter;
  @Autowired private BuyerJpaMapper mapper;

  private static BuyerJpaEntity entity(String id, String userId, String primaryAddress) {
    BuyerJpaEntity entity = new BuyerJpaEntity();
    entity.setId(id);
    entity.setUserId(userId);
    entity.setPrimaryAddress(primaryAddress);
    entity.setCommercialStatus("ACTIVE");
    entity.setAdditionalAddressesJson("[]");
    return entity;
  }

  @Test
  @DisplayName("save and findById round-trip addresses and commercial status")
  void saveAndFindByIdRoundTripsAllFields() {
    Buyer buyer = Buyer.create("user-1", "Calle 1");
    buyer.addAddress("Calle 2");

    Buyer saved = adapter.save(buyer);
    Buyer found = adapter.findById(saved.getId()).orElseThrow();

    assertThat(found.getUserId()).isEqualTo("user-1");
    assertThat(found.getPrimaryAddress()).isEqualTo("Calle 1");
    assertThat(found.getAdditionalAddresses()).containsExactly("Calle 2");
    assertThat(found.getCommercialStatus()).isEqualTo(Buyer.CommercialStatus.ACTIVE);
  }

  @Test
  @DisplayName("findByUserId returns the buyer profile")
  void findByUserIdReturnsBuyer() {
    adapter.save(Buyer.create("user-1", "Calle 1"));

    assertThat(adapter.findByUserId("user-1"))
        .isPresent()
        .get()
        .extracting(Buyer::getPrimaryAddress)
        .isEqualTo("Calle 1");
  }

  @Test
  @DisplayName("findByUserId returns empty for an unknown user")
  void findByUserIdReturnsEmptyWhenMissing() {
    assertThat(adapter.findByUserId("nobody")).isEmpty();
  }

  @Test
  @DisplayName("a suspended commercial status is persisted")
  void suspendedStatusIsPersisted() {
    Buyer buyer = Buyer.create("user-1", "Calle 1");
    buyer.suspend();

    Buyer saved = adapter.save(buyer);

    assertThat(adapter.findById(saved.getId()).orElseThrow().getCommercialStatus())
        .isEqualTo(Buyer.CommercialStatus.SUSPENDED);
    assertThat(adapter.findById(saved.getId()).orElseThrow().canPurchase()).isFalse();
  }

  @Test
  @DisplayName("findAll returns every persisted buyer")
  void findAllReturnsEveryBuyer() {
    adapter.save(Buyer.create("user-1", "Calle 1"));
    adapter.save(Buyer.create("user-2", "Calle 2"));

    assertThat(adapter.findAll()).hasSize(2);
  }

  @Test
  @DisplayName("malformed additional-address JSON degrades to an empty list")
  void malformedAddressJsonDegradesToEmptyList() {
    BuyerJpaEntity entity = entity("buyer-1", "user-1", "Calle 1");
    entity.setAdditionalAddressesJson("{not-json");
    repository.saveAndFlush(entity);

    Buyer found = adapter.findByUserId("user-1").orElseThrow();
    assertThat(found.getAdditionalAddresses()).isEmpty();
  }

  @Test
  @DisplayName("the unique userId constraint is enforced by the database")
  void duplicateUserIdIsRejected() {
    repository.saveAndFlush(entity("buyer-1", "user-1", "Calle 1"));

    assertThatThrownBy(() -> repository.saveAndFlush(entity("buyer-2", "user-1", "Calle 2")))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("the not-null constraint on primaryAddress is enforced by the database")
  void nullPrimaryAddressIsRejected() {
    BuyerJpaEntity entity = entity("buyer-1", "user-1", null);

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
