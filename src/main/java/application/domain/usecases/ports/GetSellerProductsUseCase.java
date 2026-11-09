package application.domain.usecases.ports;

import application.domain.entities.Product;
import java.util.List;

public interface GetSellerProductsUseCase {
  List<Product> getProducts(String sellerId);
}
