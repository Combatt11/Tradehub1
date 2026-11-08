package application.domain.ports.in;

import application.domain.models.Order;

public interface MarkOrderDeliveredUseCase {
  Order markDelivered(String orderId);
}
