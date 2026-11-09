package application.domain.usecases.ports;

import application.domain.entities.InventoryItem;

public interface AddInventoryUseCase {
  InventoryItem execute(String productId, String warehouseId, int quantity, String sellerId);
}
