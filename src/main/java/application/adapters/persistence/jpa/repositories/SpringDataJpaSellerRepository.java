package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.SellerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpringDataJpaSellerRepository extends JpaRepository<SellerJpaEntity, String> {
    Optional<SellerJpaEntity> findByUserId(String userId);
}
