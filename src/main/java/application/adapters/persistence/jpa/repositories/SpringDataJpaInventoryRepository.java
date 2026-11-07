package application.adapters.persistence.jpa.repositories;

import application.adapters.persistence.jpa.entities.InventoryItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SpringDataJpaInventoryRepository extends JpaRepository<InventoryItemJpaEntity, String> {
    Optional<InventoryItemJpaEntity> findByProductIdAndWarehouseId(String productId, String warehouseId);
    List<InventoryItemJpaEntity> findByProductId(String productId);
    List<InventoryItemJpaEntity> findByWarehouseId(String warehouseId);
}
