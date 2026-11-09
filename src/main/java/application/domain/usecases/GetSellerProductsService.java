package application.domain.usecases;

import application.domain.entities.Product;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.usecases.ports.GetSellerProductsUseCase;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetSellerProductsService implements GetSellerProductsUseCase {

  private final ProductRepositoryPort productRepository;

  public GetSellerProductsService(ProductRepositoryPort productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Product> getProducts(String sellerId) {
    return productRepository.findBySellerId(sellerId);
  }
}
