package application.domain.usecases.ports;

import application.domain.entities.Order;

public interface MarkOrderDeliveredUseCase {
  Order markDelivered(String orderId);
}
