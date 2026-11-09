package application.domain.services;

import application.domain.models.Product;
import application.domain.ports.in.GetProductUseCase;
import application.domain.ports.out.ProductRepositoryPort;
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
