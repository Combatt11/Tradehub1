package application.domain.ports.in;

import application.domain.models.Order;

public interface MarkOrderPreparingUseCase {
  Order markPreparing(String orderId);
}
