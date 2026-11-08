package application.domain.ports.in;

import application.domain.models.Product;
import java.math.BigDecimal;

public interface CreateProductUseCase {
  Product execute(
      String sellerId,
      String name,
      String description,
      Product.ProductType type,
      BigDecimal price,
      String sku);
}
