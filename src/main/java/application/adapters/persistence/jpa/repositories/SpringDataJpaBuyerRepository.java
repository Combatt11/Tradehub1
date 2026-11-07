package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.BuyerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpringDataJpaBuyerRepository extends JpaRepository<BuyerJpaEntity, String> {
    Optional<BuyerJpaEntity> findByUserId(String userId);
}
