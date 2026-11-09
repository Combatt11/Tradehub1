package application.domain.services;

import application.domain.exceptions.SellerNotFoundException;
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
            .orElseThrow(() -> new SellerNotFoundException(sellerId));
    seller.approve();
    return sellerRepository.save(seller);
  }
}
