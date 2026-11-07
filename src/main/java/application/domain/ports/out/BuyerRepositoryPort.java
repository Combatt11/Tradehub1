package application.domain.ports.out;

import application.domain.models.Buyer;
import java.util.List;
import java.util.Optional;

public interface BuyerRepositoryPort {
  Buyer save(Buyer buyer);

  Optional<Buyer> findById(String id);

  Optional<Buyer> findByUserId(String userId);

  List<Buyer> findAll();
}
