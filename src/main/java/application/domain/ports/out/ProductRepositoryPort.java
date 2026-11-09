package application.domain.ports.out;

import application.domain.entities.Product;
import java.util.List;
import java.util.Optional;

public interface ProductRepositoryPort {
  Product save(Product product);

  Optional<Product> findById(String id);

  List<Product> findBySellerId(String sellerId);

  List<Product> findPublished();

  List<Product> findAll();
}
