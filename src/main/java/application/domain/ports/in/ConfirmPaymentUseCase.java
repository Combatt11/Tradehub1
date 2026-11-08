package application.domain.ports.in;

import application.domain.models.Order;

public interface ConfirmPaymentUseCase {
  Order confirmPayment(String orderId, String buyerId, String paymentReference);
}
