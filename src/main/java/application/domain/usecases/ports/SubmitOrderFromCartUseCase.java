package application.domain.usecases.ports;

import application.domain.entities.Order;

public interface SubmitOrderFromCartUseCase {
  Order submitOrder(String buyerId, String shippingAddress);
}
