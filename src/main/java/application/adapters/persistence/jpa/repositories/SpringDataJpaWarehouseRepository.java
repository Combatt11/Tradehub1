package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.WarehouseJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataJpaWarehouseRepository
    extends JpaRepository<WarehouseJpaEntity, String> {
  List<WarehouseJpaEntity> findBySellerId(String sellerId);
}
