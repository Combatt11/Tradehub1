package application.domain.usecases.ports;

import application.domain.entities.Order;

public interface FinalizeOrderUseCase {
  Order finalizeOrder(String orderId);
}
