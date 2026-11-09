package application.domain.usecases.ports;

import application.domain.entities.Order;

public interface ClearCartUseCase {
  Order clearCart(String buyerId);
}
