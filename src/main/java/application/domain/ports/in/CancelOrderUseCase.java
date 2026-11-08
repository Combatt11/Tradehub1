package application.domain.ports.in;

import application.domain.models.Order;

/** Cancels an order owned by the authenticated buyer. */
public interface CancelOrderUseCase {
  Order cancelOrder(String orderId, String buyerId);
}
