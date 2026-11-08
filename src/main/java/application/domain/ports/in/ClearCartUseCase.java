package application.domain.ports.in;

import application.domain.models.Order;

public interface ClearCartUseCase {
  Order clearCart(String buyerId);
}
