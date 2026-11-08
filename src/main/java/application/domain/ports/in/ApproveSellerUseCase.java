package application.domain.ports.in;

import application.domain.models.Seller;

public interface ApproveSellerUseCase {
  Seller approveSeller(String sellerId);
}
