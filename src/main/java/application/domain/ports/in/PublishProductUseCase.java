package application.domain.ports.in;

import application.domain.models.Product;

public interface PublishProductUseCase {
  Product execute(String productId, String sellerId);
}
