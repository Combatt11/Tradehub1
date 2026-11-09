package application.domain.usecases;

import application.domain.entities.Product;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.usecases.ports.GetProductUseCase;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetProductService implements GetProductUseCase {

  private final ProductRepositoryPort productRepository;

  public GetProductService(ProductRepositoryPort productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Product> getPublishedProduct(String productId) {
    return productRepository.findById(productId).filter(Product::isAvailableForSale);
  }
}
