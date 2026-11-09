package application.domain.usecases.ports;

import application.domain.entities.Order;

public interface ConfirmPaymentUseCase {
  Order confirmPayment(String orderId, String buyerId, String paymentReference);
}
