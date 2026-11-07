package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.OrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SpringDataJpaOrderRepository extends JpaRepository<OrderJpaEntity, String> {
    List<OrderJpaEntity> findByBuyerId(String buyerId);
    Optional<OrderJpaEntity> findFirstByBuyerIdAndStatus(String buyerId, String status);
}
