package application.adapters.persistence.jpa;

import application.adapters.persistence.jpa.mappers.UserJpaMapper;
import application.adapters.persistence.jpa.repositories.SpringDataJpaUserRepository;
import application.domain.models.User;
import application.domain.ports.out.UserRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UserJpaAdapter implements UserRepositoryPort {

    private final SpringDataJpaUserRepository repository;
    private final UserJpaMapper mapper;

    public UserJpaAdapter(SpringDataJpaUserRepository repository, UserJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        return mapper.toDomain(repository.save(mapper.toEntity(user)));
    }

    @Override
    public Optional<User> findById(String id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public boolean existsByIdentityDocument(String identityDocument) {
        return repository.existsByIdentityDocument(identityDocument);
    }
}
