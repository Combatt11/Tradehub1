package application.adapters.persistence.jpa.mappers;

import application.adapters.persistence.jpa.entities.InventoryItemJpaEntity;
import application.domain.models.InventoryItem;
import org.springframework.stereotype.Component;

@Component
public class InventoryJpaMapper {

    public InventoryItem toDomain(InventoryItemJpaEntity e) {
        if (e == null) return null;
        return new InventoryItem(e.getId(), e.getProductId(), e.getWarehouseId(),
                e.getQuantityAvailable(), e.getQuantityReserved(), e.getQuantityDamaged());
    }

    public InventoryItemJpaEntity toEntity(InventoryItem d) {
        if (d == null) return null;
        InventoryItemJpaEntity e = new InventoryItemJpaEntity();
        e.setId(d.getId());
        e.setProductId(d.getProductId());
        e.setWarehouseId(d.getWarehouseId());
        e.setQuantityAvailable(d.getQuantityAvailable());
        e.setQuantityReserved(d.getQuantityReserved());
        e.setQuantityDamaged(d.getQuantityDamaged());
        return e;
    }
}
