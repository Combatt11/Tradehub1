package application.adapters.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import application.adapters.persistence.jpa.entities.UserJpaEntity;
import application.adapters.persistence.jpa.mappers.UserJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaUserRepository;
import application.domain.entities.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@Import({UserJpaMapper.class, UserJpaAdapter.class})
@DisplayName("UserJpaAdapter persistence")
class UserRepositoryJpaTest {

  @Autowired private SpringDataJpaUserRepository repository;
  @Autowired private UserJpaAdapter adapter;
  @Autowired private UserJpaMapper mapper;

  private static UserJpaEntity entity(String id, String email, String identityDocument) {
    UserJpaEntity entity = new UserJpaEntity();
    entity.setId(id);
    entity.setFullName("Test User");
    entity.setEmail(email);
    entity.setPasswordHash("$2a$10$hash");
    entity.setRole("BUYER");
    entity.setStatus("ACTIVE");
    entity.setIdentityDocument(identityDocument);
    return entity;
  }

  private static User user(String fullName, String email, String identityDocument) {
    return User.create(fullName, email, "hash", User.Role.BUYER, identityDocument);
  }

  @Test
  @DisplayName("save and findById round-trip every mapped field")
  void saveAndFindByIdRoundTripsAllFields() {
    User saved = adapter.save(user("Alice", "alice@test.com", "DOC-1"));

    assertThat(saved.getId()).isNotBlank();
    User found = adapter.findById(saved.getId()).orElseThrow();
    assertThat(found.getFullName()).isEqualTo("Alice");
    assertThat(found.getEmail()).isEqualTo("alice@test.com");
    assertThat(found.getPasswordHash()).isEqualTo("hash");
    assertThat(found.getRole()).isEqualTo(User.Role.BUYER);
    assertThat(found.getStatus()).isEqualTo(User.Status.ACTIVE);
    assertThat(found.getIdentityDocument()).isEqualTo("DOC-1");
  }

  @Test
  @DisplayName("save updates an existing row instead of inserting a duplicate")
  void saveUpdatesExistingUser() {
    User saved = adapter.save(user("Alice", "alice@test.com", "DOC-1"));
    User updated =
        new User(
            saved.getId(),
            "Alice Updated",
            "alice.updated@test.com",
            "new-hash",
            User.Role.SELLER,
            User.Status.ACTIVE,
            "DOC-2");

    adapter.save(updated);

    User found = adapter.findById(saved.getId()).orElseThrow();
    assertThat(found.getFullName()).isEqualTo("Alice Updated");
    assertThat(found.getEmail()).isEqualTo("alice.updated@test.com");
    assertThat(found.getPasswordHash()).isEqualTo("new-hash");
    assertThat(found.getRole()).isEqualTo(User.Role.SELLER);
  }

  @Test
  @DisplayName("findByEmail returns the stored user")
  void findByEmailReturnsStoredUser() {
    adapter.save(user("Bob", "bob@test.com", "DOC-2"));

    assertThat(adapter.findByEmail("bob@test.com"))
        .isPresent()
        .get()
        .extracting(User::getFullName)
        .isEqualTo("Bob");
  }

  @Test
  @DisplayName("findByEmail returns empty for an unknown address")
  void findByEmailReturnsEmptyWhenMissing() {
    assertThat(adapter.findByEmail("missing@test.com")).isEmpty();
  }

  @Test
  @DisplayName("email and identity document existence queries reflect stored rows")
  void existenceQueriesReflectStoredRows() {
    adapter.save(user("Carol", "carol@test.com", "DOC-3"));

    assertThat(adapter.existsByEmail("carol@test.com")).isTrue();
    assertThat(adapter.existsByEmail("nobody@test.com")).isFalse();
    assertThat(adapter.existsByIdentityDocument("DOC-3")).isTrue();
    assertThat(adapter.existsByIdentityDocument("DOC-999")).isFalse();
  }

  @Test
  @DisplayName("findAll returns every persisted user")
  void findAllReturnsEveryUser() {
    adapter.save(user("Dave", "dave@test.com", "DOC-4"));
    adapter.save(user("Eve", "eve@test.com", "DOC-5"));

    assertThat(adapter.findAll()).hasSize(2);
  }

  @Test
  @DisplayName("the unique email constraint is enforced by the database")
  void duplicateEmailIsRejected() {
    repository.saveAndFlush(entity("user-1", "duplicated@test.com", "DOC-10"));

    assertThatThrownBy(
            () -> repository.saveAndFlush(entity("user-2", "duplicated@test.com", "DOC-11")))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("the not-null constraint on fullName is enforced by the database")
  void nullFullNameIsRejected() {
    UserJpaEntity entity = entity("user-1", "valid@test.com", "DOC-12");
    entity.setFullName(null);

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
