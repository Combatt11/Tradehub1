package application.domain.ports.in;

import application.domain.models.Product;
import java.util.Optional;

public interface GetProductUseCase {
  Optional<Product> getPublishedProduct(String productId);
}
