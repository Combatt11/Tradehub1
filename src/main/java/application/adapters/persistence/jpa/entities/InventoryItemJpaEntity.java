package application.adapters.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"productId", "warehouseId"})
})
@Getter
@Setter
@NoArgsConstructor
public class InventoryItemJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String productId;

    @Column(nullable = false)
    private String warehouseId;

    @Column(nullable = false)
    private int quantityAvailable;

    @Column(nullable = false)
    private int quantityReserved;

    @Column(nullable = false)
    private int quantityDamaged;
}
