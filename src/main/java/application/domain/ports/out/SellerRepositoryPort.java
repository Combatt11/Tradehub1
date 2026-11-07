package application.domain.ports.out;

import application.domain.models.Seller;
import java.util.List;
import java.util.Optional;

public interface SellerRepositoryPort {
  Seller save(Seller seller);

  Optional<Seller> findById(String id);

  Optional<Seller> findByUserId(String userId);

  List<Seller> findAll();
}
