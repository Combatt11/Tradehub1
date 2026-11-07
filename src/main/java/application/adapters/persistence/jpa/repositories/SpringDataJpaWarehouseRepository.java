package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.WarehouseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SpringDataJpaWarehouseRepository extends JpaRepository<WarehouseJpaEntity, String> {
    List<WarehouseJpaEntity> findBySellerId(String sellerId);
}
