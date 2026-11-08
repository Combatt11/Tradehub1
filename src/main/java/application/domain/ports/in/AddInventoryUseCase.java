package application.domain.ports.in;

import application.domain.models.InventoryItem;

public interface AddInventoryUseCase {
  InventoryItem execute(String productId, String warehouseId, int quantity, String sellerId);
}
