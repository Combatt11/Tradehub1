package application.domain.ports.in;

import application.domain.models.Order;

public interface GetCartUseCase {
  Order getOrCreateCart(String buyerId);
}
