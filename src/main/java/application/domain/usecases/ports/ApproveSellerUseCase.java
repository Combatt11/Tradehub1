package application.domain.usecases.ports;

import application.domain.entities.Seller;

public interface ApproveSellerUseCase {
  Seller approveSeller(String sellerId);
}
