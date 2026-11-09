package application.domain.usecases.ports;

import application.domain.entities.Product;
import java.util.List;

public interface BrowseCatalogUseCase {
  List<Product> browsePublishedProducts();
}
