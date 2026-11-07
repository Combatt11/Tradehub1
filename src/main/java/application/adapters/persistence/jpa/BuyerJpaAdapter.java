package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.mappers.BuyerJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaBuyerRepository;
import application.domain.models.Buyer;
import application.domain.ports.out.BuyerRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class BuyerJpaAdapter implements BuyerRepositoryPort {

    private final SpringDataJpaBuyerRepository repository;
    private final BuyerJpaMapper mapper;

    public BuyerJpaAdapter(SpringDataJpaBuyerRepository repository, BuyerJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Buyer save(Buyer buyer) {
        return mapper.toDomain(repository.save(mapper.toEntity(buyer)));
    }

    @Override
    public Optional<Buyer> findById(String id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Buyer> findByUserId(String userId) {
        return repository.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public List<Buyer> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }
}
