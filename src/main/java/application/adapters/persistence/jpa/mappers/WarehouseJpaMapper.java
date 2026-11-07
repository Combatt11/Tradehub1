package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.WarehouseJpaEntity;
import application.domain.models.Warehouse;
import org.springframework.stereotype.Component;

@Component
public class WarehouseJpaMapper {

    public Warehouse toDomain(WarehouseJpaEntity e) {
        if (e == null) return null;
        return new Warehouse(e.getId(), e.getName(), e.getAddress(),
                Warehouse.WarehouseType.valueOf(e.getType()),
                Warehouse.WarehouseStatus.valueOf(e.getStatus()),
                e.getSellerId(), e.getOperatorUserId());
    }

    public WarehouseJpaEntity toEntity(Warehouse d) {
        if (d == null) return null;
        WarehouseJpaEntity e = new WarehouseJpaEntity();
        e.setId(d.getId());
        e.setName(d.getName());
        e.setAddress(d.getAddress());
        e.setType(d.getType().name());
        e.setStatus(d.getStatus().name());
        e.setSellerId(d.getSellerId());
        e.setOperatorUserId(d.getOperatorUserId());
        return e;
    }
}
