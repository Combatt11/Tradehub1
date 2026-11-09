package application.domain.usecases;

import application.domain.entities.Product;
import application.domain.entities.Seller;
import application.domain.exceptions.SellerNotFoundException;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.SellerRepositoryPort;
import application.domain.usecases.ports.CreateProductUseCase;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateProductService implements CreateProductUseCase {
  private final ProductRepositoryPort productRepository;
  private final SellerRepositoryPort sellerRepository;

  public CreateProductService(
      ProductRepositoryPort productRepository, SellerRepositoryPort sellerRepository) {
    this.productRepository = productRepository;
    this.sellerRepository = sellerRepository;
  }

  @Override
  @Transactional
  public Product execute(
      String sellerId,
      String name,
      String description,
      Product.ProductType type,
      BigDecimal price,
      String sku) {
    Seller seller =
        sellerRepository
            .findById(sellerId)
            .orElseThrow(() -> new SellerNotFoundException(sellerId));
    seller.ensureActive();
    return productRepository.save(Product.create(sellerId, name, description, type, price, sku));
  }
}
