package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.mappers.InventoryJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaInventoryRepository;
import application.domain.models.InventoryItem;
import application.domain.ports.out.InventoryRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class InventoryJpaAdapter implements InventoryRepositoryPort {

    private final SpringDataJpaInventoryRepository repository;
    private final InventoryJpaMapper mapper;

    public InventoryJpaAdapter(SpringDataJpaInventoryRepository repository, InventoryJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public InventoryItem save(InventoryItem item) {
        return mapper.toDomain(repository.save(mapper.toEntity(item)));
    }

    @Override
    public Optional<InventoryItem> findById(String id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<InventoryItem> findByProductAndWarehouse(String productId, String warehouseId) {
        return repository.findByProductIdAndWarehouseId(productId, warehouseId).map(mapper::toDomain);
    }

    @Override
    public List<InventoryItem> findByProductId(String productId) {
        return repository.findByProductId(productId).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<InventoryItem> findByWarehouseId(String warehouseId) {
        return repository.findByWarehouseId(warehouseId).stream().map(mapper::toDomain).collect(Collectors.toList());
    }
}
