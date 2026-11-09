package application.domain.usecases;

import application.domain.entities.Product;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.usecases.ports.BrowseCatalogUseCase;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrowseCatalogService implements BrowseCatalogUseCase {

  private final ProductRepositoryPort productRepository;

  public BrowseCatalogService(ProductRepositoryPort productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Product> browsePublishedProducts() {
    return productRepository.findPublished();
  }
}
