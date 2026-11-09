package application.domain.usecases.ports;

import application.domain.entities.Order;

public interface AddItemToCartUseCase {
  Order addItem(String buyerId, String productId, int quantity, String warehouseId);
}
