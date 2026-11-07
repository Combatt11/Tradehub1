package application.adapters.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sellers")
@Getter
@Setter
@NoArgsConstructor
public class SellerJpaEntity {

    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private String businessName;

    private String taxId;

    @Column(nullable = false)
    private String status;

    private String contactPhone;
}
