package application.domain.ports.in;

import application.domain.models.Order;

public interface FinalizeOrderUseCase {
  Order finalizeOrder(String orderId);
}
