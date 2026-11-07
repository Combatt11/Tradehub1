package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpringDataJpaUserRepository extends JpaRepository<UserJpaEntity, String> {
    Optional<UserJpaEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByIdentityDocument(String identityDocument);
}
