package application.domain.ports.out;

import application.domain.models.User;
import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
  User save(User user);

  Optional<User> findById(String id);

  Optional<User> findByEmail(String email);

  List<User> findAll();

  boolean existsByEmail(String email);

  boolean existsByIdentityDocument(String identityDocument);
}
