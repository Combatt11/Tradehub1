package application.domain.ports.out;

import application.domain.models.InventoryItem;
import java.util.List;
import java.util.Optional;

public interface InventoryRepositoryPort {
  InventoryItem save(InventoryItem item);

  Optional<InventoryItem> findById(String id);

  Optional<InventoryItem> findByProductAndWarehouse(String productId, String warehouseId);

  List<InventoryItem> findByProductId(String productId);

  List<InventoryItem> findByWarehouseId(String warehouseId);
}
