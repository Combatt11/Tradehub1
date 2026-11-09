package application.domain.usecases.ports;

import application.domain.entities.Product;

public interface PublishProductUseCase {
  Product execute(String productId, String sellerId);
}
