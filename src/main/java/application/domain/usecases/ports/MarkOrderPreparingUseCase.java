package application.domain.usecases.ports;

import application.domain.entities.Order;

public interface MarkOrderPreparingUseCase {
  Order markPreparing(String orderId);
}
