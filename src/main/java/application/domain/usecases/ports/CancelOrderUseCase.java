package application.domain.usecases.ports;

import application.domain.entities.Order;

/** Cancels an order owned by the authenticated buyer. */
public interface CancelOrderUseCase {
  Order cancelOrder(String orderId, String buyerId);
}
