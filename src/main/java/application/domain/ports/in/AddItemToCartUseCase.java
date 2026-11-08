package application.domain.ports.in;

import application.domain.models.Order;

public interface AddItemToCartUseCase {
  Order addItem(String buyerId, String productId, int quantity, String warehouseId);
}
