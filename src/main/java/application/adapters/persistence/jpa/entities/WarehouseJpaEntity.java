package application.adapters.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "warehouses")
@Getter
@Setter
@NoArgsConstructor
public class WarehouseJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String status;

    private String sellerId;
    private String operatorUserId;
}
