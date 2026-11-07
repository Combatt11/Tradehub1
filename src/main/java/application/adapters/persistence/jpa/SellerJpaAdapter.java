package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.mappers.SellerJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaSellerRepository;
import application.domain.models.Seller;
import application.domain.ports.out.SellerRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SellerJpaAdapter implements SellerRepositoryPort {

    private final SpringDataJpaSellerRepository repository;
    private final SellerJpaMapper mapper;

    public SellerJpaAdapter(SpringDataJpaSellerRepository repository, SellerJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Seller save(Seller seller) {
        return mapper.toDomain(repository.save(mapper.toEntity(seller)));
    }

    @Override
    public Optional<Seller> findById(String id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Seller> findByUserId(String userId) {
        return repository.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public List<Seller> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }
}
