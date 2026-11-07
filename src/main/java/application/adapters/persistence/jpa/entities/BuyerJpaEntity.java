package application.adapters.persistence.jpa.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "buyers")
@Getter
@Setter
@NoArgsConstructor
public class BuyerJpaEntity {

    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private String primaryAddress;

    @Column(length = 2000)
    private String additionalAddressesJson;

    @Column(nullable = false)
    private String commercialStatus;
}
