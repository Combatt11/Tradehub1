package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.mappers.ProductJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import application.domain.models.Product;
import application.domain.ports.out.ProductRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProductJpaAdapter implements ProductRepositoryPort {

    private final SpringDataJpaProductRepository repository;
    private final ProductJpaMapper mapper;

    public ProductJpaAdapter(SpringDataJpaProductRepository repository, ProductJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Product save(Product product) {
        return mapper.toDomain(repository.save(mapper.toEntity(product)));
    }

    @Override
    public Optional<Product> findById(String id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Product> findBySellerId(String sellerId) {
        return repository.findBySellerId(sellerId).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Product> findPublished() {
        return repository.findByStatus(Product.ProductStatus.PUBLISHED.name())
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Product> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }
}
