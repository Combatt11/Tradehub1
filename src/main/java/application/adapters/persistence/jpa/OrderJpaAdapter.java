package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.mappers.OrderJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaOrderRepository;
import application.domain.models.Order;
import application.domain.ports.out.OrderRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OrderJpaAdapter implements OrderRepositoryPort {

    private final SpringDataJpaOrderRepository repository;
    private final OrderJpaMapper mapper;

    public OrderJpaAdapter(SpringDataJpaOrderRepository repository, OrderJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Order save(Order order) {
        return mapper.toDomain(repository.save(mapper.toEntity(order)));
    }

    @Override
    public Optional<Order> findById(String id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Order> findByBuyerId(String buyerId) {
        return repository.findByBuyerId(buyerId).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Order> findActiveCartByBuyerId(String buyerId) {
        return repository.findFirstByBuyerIdAndStatus(buyerId, Order.OrderStatus.CART.name())
                .map(mapper::toDomain);
    }

    @Override
    public List<Order> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }
}
