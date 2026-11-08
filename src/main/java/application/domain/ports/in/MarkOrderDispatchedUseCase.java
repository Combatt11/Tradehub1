package application.domain.ports.in;

import application.domain.models.Order;

public interface MarkOrderDispatchedUseCase {
  Order markDispatched(String orderId);
}
