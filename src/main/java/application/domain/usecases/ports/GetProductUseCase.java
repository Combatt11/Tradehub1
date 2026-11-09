package application.domain.usecases.ports;

import application.domain.entities.Product;
import java.util.Optional;

public interface GetProductUseCase {
  Optional<Product> getPublishedProduct(String productId);
}
