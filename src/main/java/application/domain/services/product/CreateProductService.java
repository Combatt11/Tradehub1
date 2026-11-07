package application.domain.services.product;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Product;
import application.domain.models.Seller;
import application.domain.ports.out.ProductRepositoryPort;
import application.domain.ports.out.SellerRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class CreateProductService {
    private final ProductRepositoryPort productRepository;
    private final SellerRepositoryPort sellerRepository;

    public CreateProductService(ProductRepositoryPort productRepository, SellerRepositoryPort sellerRepository) {
        this.productRepository = productRepository;
        this.sellerRepository = sellerRepository;
    }

    @Transactional
    public Product execute(String sellerId, String name, String description,
                           Product.ProductType type, BigDecimal price, String sku) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new UnauthorizedOperationException("Seller not found"));
        if (!seller.isActive()) throw new UnauthorizedOperationException("Seller is not active");
        return productRepository.save(Product.create(sellerId, name, description, type, price, sku));
    }
}
