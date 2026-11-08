package application.domain.ports.in;

import application.domain.models.Product;
import java.util.List;

public interface BrowseCatalogUseCase {
  List<Product> browsePublishedProducts();
}
