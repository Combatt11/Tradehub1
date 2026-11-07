package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.mappers.WarehouseJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaWarehouseRepository;
import application.domain.models.Warehouse;
import application.domain.ports.out.WarehouseRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class WarehouseJpaAdapter implements WarehouseRepositoryPort {

    private final SpringDataJpaWarehouseRepository repository;
    private final WarehouseJpaMapper mapper;

    public WarehouseJpaAdapter(SpringDataJpaWarehouseRepository repository, WarehouseJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Warehouse save(Warehouse warehouse) {
        return mapper.toDomain(repository.save(mapper.toEntity(warehouse)));
    }

    @Override
    public Optional<Warehouse> findById(String id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Warehouse> findBySellerId(String sellerId) {
        return repository.findBySellerId(sellerId).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Warehouse> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }
}
