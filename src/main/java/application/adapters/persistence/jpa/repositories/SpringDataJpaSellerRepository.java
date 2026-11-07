package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.SellerJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataJpaSellerRepository extends JpaRepository<SellerJpaEntity, String> {
  Optional<SellerJpaEntity> findByUserId(String userId);
}
