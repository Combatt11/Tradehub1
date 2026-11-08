package application.domain.services.seller;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Seller;
import application.domain.ports.in.ApproveSellerUseCase;
import application.domain.ports.out.SellerRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApproveSellerService implements ApproveSellerUseCase {

  private final SellerRepositoryPort sellerRepository;

  public ApproveSellerService(SellerRepositoryPort sellerRepository) {
    this.sellerRepository = sellerRepository;
  }

  @Override
  @Transactional
  public Seller approveSeller(String sellerId) {
    Seller seller =
        sellerRepository
            .findById(sellerId)
            .orElseThrow(() -> new UnauthorizedOperationException("Seller not found"));
    seller.approve();
    return sellerRepository.save(seller);
  }
}
