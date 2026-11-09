package application.domain.services;

import application.domain.exceptions.ProductNotFoundException;
import application.domain.models.Product;
import application.domain.ports.in.PublishProductUseCase;
import application.domain.ports.out.ProductRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublishProductService implements PublishProductUseCase {
  private final ProductRepositoryPort productRepository;

  public PublishProductService(ProductRepositoryPort productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  @Transactional
  public Product execute(String productId, String sellerId) {
    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    product.ensureOwnedBy(sellerId);
    product.publish();
    return productRepository.save(product);
  }
}
