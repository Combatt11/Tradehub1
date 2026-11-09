package application.adapters.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import application.adapters.persistence.jpa.entities.SellerJpaEntity;
import application.adapters.persistence.jpa.mappers.SellerJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaSellerRepository;
import application.domain.models.Seller;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@Import({SellerJpaMapper.class, SellerJpaAdapter.class})
@DisplayName("SellerJpaAdapter persistence")
class SellerRepositoryJpaTest {

  @Autowired private SpringDataJpaSellerRepository repository;
  @Autowired private SellerJpaAdapter adapter;
  @Autowired private SellerJpaMapper mapper;

  private static SellerJpaEntity entity(String id, String userId, String businessName) {
    SellerJpaEntity entity = new SellerJpaEntity();
    entity.setId(id);
    entity.setUserId(userId);
    entity.setBusinessName(businessName);
    entity.setTaxId("TAX-1");
    entity.setStatus("PENDING_APPROVAL");
    entity.setContactPhone("+555");
    return entity;
  }

  @Test
  @DisplayName("save and findById round-trip every mapped field")
  void saveAndFindByIdRoundTripsAllFields() {
    Seller saved = adapter.save(Seller.create("user-1", "Acme", "TAX-1", "+555"));

    Seller found = adapter.findById(saved.getId()).orElseThrow();
    assertThat(found.getUserId()).isEqualTo("user-1");
    assertThat(found.getBusinessName()).isEqualTo("Acme");
    assertThat(found.getTaxId()).isEqualTo("TAX-1");
    assertThat(found.getStatus()).isEqualTo(Seller.SellerStatus.PENDING_APPROVAL);
    assertThat(found.getContactPhone()).isEqualTo("+555");
  }

  @Test
  @DisplayName("findByUserId returns the seller profile")
  void findByUserIdReturnsSeller() {
    adapter.save(Seller.create("user-1", "Acme", "TAX-1", "+555"));

    assertThat(adapter.findByUserId("user-1"))
        .isPresent()
        .get()
        .extracting(Seller::getBusinessName)
        .isEqualTo("Acme");
  }

  @Test
  @DisplayName("findByUserId returns empty for an unknown user")
  void findByUserIdReturnsEmptyWhenMissing() {
    assertThat(adapter.findByUserId("nobody")).isEmpty();
  }

  @Test
  @DisplayName("approving a seller is persisted across save/find cycles")
  void approvingSellerIsPersisted() {
    Seller seller = adapter.save(Seller.create("user-1", "Acme", "TAX-1", "+555"));
    seller.approve();

    adapter.save(seller);

    assertThat(adapter.findById(seller.getId()).orElseThrow().getStatus())
        .isEqualTo(Seller.SellerStatus.ACTIVE);
  }

  @Test
  @DisplayName("findAll returns every persisted seller")
  void findAllReturnsEverySeller() {
    adapter.save(Seller.create("user-1", "Acme", "TAX-1", "+555"));
    adapter.save(Seller.create("user-2", "Beta", "TAX-2", "+556"));

    assertThat(adapter.findAll()).hasSize(2);
  }

  @Test
  @DisplayName("the unique userId constraint is enforced by the database")
  void duplicateUserIdIsRejected() {
    repository.saveAndFlush(entity("seller-1", "user-1", "Acme"));

    assertThatThrownBy(() -> repository.saveAndFlush(entity("seller-2", "user-1", "Beta")))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("the not-null constraint on businessName is enforced by the database")
  void nullBusinessNameIsRejected() {
    SellerJpaEntity entity = entity("seller-1", "user-1", null);

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
