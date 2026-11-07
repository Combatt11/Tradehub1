package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.InventoryItemJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataJpaInventoryRepository
    extends JpaRepository<InventoryItemJpaEntity, String> {
  Optional<InventoryItemJpaEntity> findByProductIdAndWarehouseId(
      String productId, String warehouseId);

  List<InventoryItemJpaEntity> findByProductId(String productId);

  List<InventoryItemJpaEntity> findByWarehouseId(String warehouseId);
}
