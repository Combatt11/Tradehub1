package application.domain.services.product;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Product;
import application.domain.ports.out.ProductRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublishProductService {
    private final ProductRepositoryPort productRepository;

    public PublishProductService(ProductRepositoryPort productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public Product execute(String productId, String sellerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new UnauthorizedOperationException("Product not found"));
        if (!product.getSellerId().equals(sellerId))
            throw new UnauthorizedOperationException("Product does not belong to this seller");
        product.publish();
        return productRepository.save(product);
    }
}
