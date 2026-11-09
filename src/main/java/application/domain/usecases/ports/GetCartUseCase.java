package application.domain.usecases.ports;

import application.domain.entities.Order;

public interface GetCartUseCase {
  Order getOrCreateCart(String buyerId);
}
