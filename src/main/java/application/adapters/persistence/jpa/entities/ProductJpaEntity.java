package application.adapters.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class ProductJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String sellerId;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private String status;

    @Column(length = 1000)
    private String variantsJson;

    private String sku;
}
