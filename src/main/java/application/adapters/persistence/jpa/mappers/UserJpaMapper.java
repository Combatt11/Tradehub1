package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.UserJpaEntity;
import application.domain.models.User;
import org.springframework.stereotype.Component;

@Component
public class UserJpaMapper {

    public User toDomain(UserJpaEntity e) {
        if (e == null) return null;
        return new User(e.getId(), e.getFullName(), e.getEmail(), e.getPasswordHash(),
                User.Role.valueOf(e.getRole()), User.Status.valueOf(e.getStatus()),
                e.getIdentityDocument());
    }

    public UserJpaEntity toEntity(User d) {
        if (d == null) return null;
        UserJpaEntity e = new UserJpaEntity();
        e.setId(d.getId());
        e.setFullName(d.getFullName());
        e.setEmail(d.getEmail());
        e.setPasswordHash(d.getPasswordHash());
        e.setRole(d.getRole().name());
        e.setStatus(d.getStatus().name());
        e.setIdentityDocument(d.getIdentityDocument());
        return e;
    }
}
