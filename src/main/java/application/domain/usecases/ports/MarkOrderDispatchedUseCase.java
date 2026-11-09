package application.domain.usecases.ports;

import application.domain.entities.Order;

public interface MarkOrderDispatchedUseCase {
  Order markDispatched(String orderId);
}
