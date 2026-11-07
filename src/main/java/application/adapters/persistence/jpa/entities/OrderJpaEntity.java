package application.adapters.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class OrderJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String buyerId;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    private String shippingAddress;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    private String paymentReference;

    @Column(length = 4000)
    private String itemsJson;
}
